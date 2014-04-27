package com.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class BluetoothControl extends Application{
	private final static String TAG = "MissionControl";
	// Debug flag
	public final static boolean D = true;

	// Time between sending the idle filler to confirm communication, must be smaller than the timeout constant.
	private final int minCommInterval = 900;
	// Time after which the communication is deemed dead
	private final int timeout = 3000;
	private long lastComm;

	// Member fields
	private BluetoothThread bluetoothThread;
	private TimeoutThread timeoutThread;
	private Handler activityHandler;
	private int state;
	private boolean busy, stoppingConnection;

	// Constants to indicate message contents
	public static final int MSG_OK = 0;
	public static final int MSG_READ = 1;
	public static final int MSG_WRITE = 2;
	public static final int MSG_CANCEL = 3;
	public static final int MSG_CONNECTED = 4;

	// General purpose constants to be used inside activities as callback values
	public static final int MSG_1 = 10;
	public static final int MSG_2 = 11;
	public static final int MSG_3 = 12;

	// Constants that indicate the current connection state
	private static final int STATE_NONE = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	
	public BluetoothControl()
	{
		state = STATE_NONE;
		activityHandler = null;
	}
	public void setActivityHandler(Handler handler)
	{
		activityHandler = handler;
	}
	private synchronized void sendMessage(int type, Object value)
	{
		if(activityHandler != null)
		{
			activityHandler.obtainMessage(type, value).sendToTarget();
		}
	}
	private synchronized void setState(int newState)
	{
		if(D)
			Log.i(TAG, "Connection status: " + state + " -> " + newState);
		state = newState;
	}
	private synchronized void updateLastComm()
	{
		lastComm = System.currentTimeMillis();
	}
	public synchronized void connect(BluetoothDevice device)
	{
		if(D)
			Log.i(TAG, "Connecting to " + device.getName());
		stoppingConnection = false;
		busy = false;

		// Cancel any thread currently running a connection
		if(bluetoothThread != null)
		{
			bluetoothThread.cancel();
			bluetoothThread = null;
		}

		setState(STATE_CONNECTING);

		// Start the thread to connect with the given device
		bluetoothThread = new BluetoothThread(device);
		bluetoothThread.start();

		// Start the timeout thread to check the connecting status
		timeoutThread = new TimeoutThread();
		timeoutThread.start();
	}
	public synchronized void disconnect()
	{
		// Do not stop twice
		if(!stoppingConnection)
		{
			stoppingConnection = true;
			if(D)
				Log.i(TAG, "Stop");
			if(bluetoothThread != null)
			{
				bluetoothThread.cancel();
				bluetoothThread = null;
			}
			setState(STATE_NONE);
			sendMessage(MSG_CANCEL, "Connection ended");
		}
	}
	private class BluetoothThread extends Thread
	{
		private final BluetoothSocket socket;
		private InputStream inStream;
		private OutputStream outStream;

		public BluetoothThread(BluetoothDevice device)
		{
			BluetoothSocket tmp = null;
			try
			{
				// General purpose UUID
				tmp = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			socket = tmp;
		}

		public void run()
		{
			// Connect to the socket
			try
			{
				// Blocking function, needs the timeout
				if(D)
					Log.i(TAG, "Connecting to socket");
				socket.connect();
			}
			catch(IOException e)
			{
				// If the user didn't cancel the connection then it has failed (timeout)
				if(!stoppingConnection)
				{
					if(D)
						Log.e(TAG, "Cound not connect to socket");
					e.printStackTrace();
					try
					{
						socket.close();
					}
					catch(IOException e1)
					{
						if(D)
							Log.e(TAG, "Cound not close the socket");
						e1.printStackTrace();
					}
					disconnect();
				}
				return;
			}

			// Connected
			setState(STATE_CONNECTED);
			// Send message to activity to inform of success
			sendMessage(MSG_CONNECTED, null);

			// Get the BluetoothSocket input and output streams
			try
			{
				inStream = socket.getInputStream();
				outStream = socket.getOutputStream();
			}
			catch(IOException e)
			{
				// Failed to get the streams
				disconnect();
				e.printStackTrace();
				return;
			}

			byte[] buffer = new byte[1024];
			byte ch;
			int bytes;
			String input;

			// Keep listening to the InputStream while connected
			while(true)
			{
				try
				{
					// Make a packet, use \n (new line or NL) as packet end
					// println() used in Arduino code adds \r\n to the end of the stream
					bytes = 0;
					while((ch = (byte) inStream.read()) != '\n')
					{
						buffer[bytes++] = ch;
					}
					// Prevent read errors (if you mess enough with it)
					if(bytes > 0)
					{
						// The carriage return (\r) character has to be removed
						input = new String(buffer, "UTF-8").substring(0, bytes - 1);

						if(D)
							Log.v(TAG, "Read: " + input);

						// Empty character is considered as a filler to keep the connection alive, don't forward that to the activity
						if(!input.equals("0"))
						{
							// Send the obtained bytes to the UI Activity if any
							sendMessage(MSG_READ, input);
						}
					}
					busy = false;
					// Update last communication time to prevent timeout
					updateLastComm();

				}
				catch(IOException e)
				{
					// read() will inevitably throw an error, even when just disconnecting
					if(!stoppingConnection)
					{
						if(D)
							Log.e(TAG, "Failed to read");
						e.printStackTrace();
						disconnect();
					}
					break;
				}
			}
		}

		public boolean write(String out)
		{
			if(outStream == null)
			{
				return false;
			}

			if(D)
				Log.v(TAG, "Write: " + out);
			try
			{
				if(out != null)
				{
					// Show sent message to the active activity
					sendMessage(MSG_WRITE, out);
					outStream.write(out.getBytes());
				}
				else
				{
					// This is a special case for the filler
					outStream.write(0);
				}
				// End packet with a new line
				outStream.write('\n');
				return true;
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			return false;
		}

		public void cancel()
		{
			try
			{
				if(inStream != null)
				{
					inStream.close();
				}
				if(outStream != null)
				{
					outStream.close();
				}
				if(socket != null)
				{
					socket.close();
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class TimeoutThread extends Thread
	{
		public TimeoutThread()
		{
			// Set the time before first check
			if(D)
				Log.i(TAG, "Started timeout thread");
			updateLastComm();
		}

		public void run()
		{
			while(state == STATE_CONNECTING || state == STATE_CONNECTED)
			{
				// I'm not sure that it's needed here, but it works
				synchronized(BluetoothControl.this)
				{
					// Filler hash to confirm communication with device when idle
					if(System.currentTimeMillis() - lastComm > minCommInterval && !busy && state == STATE_CONNECTED)
					{
						write(null);
					}

					// Communication timed out
					if(System.currentTimeMillis() - lastComm > timeout)
					{
						if(D)
							Log.e(TAG, "Timeout");
						disconnect();
						break;
					}
				}

				// This thread should not run all the time
				try
				{
					Thread.sleep(50);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public boolean write(String out)
	{
		// The device hasn't finished processing last command, reset commands ("r") it always get sent
		if(busy && !out.equals(out))
		{
			if(D)
				Log.v(TAG, "Busy");
			return false;
		}
		busy = true;

		// Create temporary object
		BluetoothThread r;
		// Synchronize a copy of the BluetoothThread
		synchronized(this)
		{
			// Make sure the connection is live
			if(state != STATE_CONNECTED)
			{
				return false;
			}
			r = bluetoothThread;
		}
		// Perform the write unsynchronized
		return r.write(out);
	}
}

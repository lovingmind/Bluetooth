package com.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;

public class BluetoothActivity extends Activity implements Handler.Callback{
	
	private static BluetoothControl appState;
	// When launching a new activity and this one stops it doesn't mean something bad (no connection loss)
	protected boolean preventCancel;
	private static String TAG;
	protected void onCreate(Bundle savedInstanceState)
	{
		// Launched when the activity is created
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		appState = (BluetoothControl) getApplicationContext();
	}

	protected boolean write(String message)
	{
		// Send command to the Bluetooth device
		return appState.write(message);
	}

	protected void disconnect()
	{
		// Disconnect from the Bluetooth device
		if(BluetoothControl.D) Log.i(TAG, "Connection end request");
		appState.disconnect();
	}

	public boolean handleMessage(Message msg)
	{
		switch(msg.what)
		{
		case BluetoothControl.MSG_OK:
			// When a child activity returns safely
			if(BluetoothControl.D) Log.i(TAG, "Result of child activity OK");
			break;
		case BluetoothControl.MSG_CANCEL:
			// When a child activity returns after being canceled (ex: if the connection is lost) cancel this activity
			if(BluetoothControl.D) Log.e(TAG, "Got canceled");
			setResult(BluetoothControl.MSG_CANCEL, new Intent());
			finish();
			break;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// Send activity result messages to the handler
		Message.obtain(new Handler(this), resultCode).sendToTarget();
	}

	@Override
	protected void onResume()
	{
		// This is called when the activity resumes
		TAG = getLocalClassName();
		if(BluetoothControl.D) Log.i(TAG, "Set handler");
		// Set the handler to receive messages from the main application class
		appState.setActivityHandler(new Handler(this));
		preventCancel = false;
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// When the user clicks the application icon on the top left
		if(item.getItemId() == android.R.id.home)
		{
			// Behave as if the back button was clicked
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		// Pressing the back button quits the activity and informs the parent activity
		if(BluetoothControl.D) Log.i(TAG, "Back pressed");
		setResult(BluetoothControl.MSG_OK, new Intent());
		finish();
	}
	
	@Override
	public void finish()
	{
		// Remove the handler from the main application class
		appState.setActivityHandler(null);
		super.finish();
	}

	@Override
	protected void onPause()
	{
		// Pausing an activity isn't allowed, unless it has been prevented
		if(!preventCancel)
		{
			// Tell itself to cancel
			Message.obtain(new Handler(this), BluetoothControl.MSG_CANCEL).sendToTarget();
		}
		super.onPause();
	}
}

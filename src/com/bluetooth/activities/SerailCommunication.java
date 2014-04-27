package com.bluetooth.activities;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.bluetooth.BluetoothActivity;
import com.bluetooth.BluetoothControl;
import com.bluetooth.R;

public class SerailCommunication extends BluetoothActivity{
	private LogView tvData;
	private EditText etCommand;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.serial);

		// Prepare the UI
		tvData = (LogView) findViewById(R.id.tvData);

		etCommand = (EditText) findViewById(R.id.etCommand);
		etCommand.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				// When the enter button of the keyboard is pressed the message is sent
				if(actionId == EditorInfo.IME_ACTION_SEND)
				{
					// Sending a command clears the input
					String msg = etCommand.getText().toString();
					if(!msg.equals(""))
					{
						write(msg);
						etCommand.setText("");
					}
				}
				// Must return true or the keyboard will be hidden
				return true;
			}
		});
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		super.handleMessage(msg);
		switch(msg.what)
		{
		case BluetoothControl.MSG_READ:
			tvData.append("R: " + msg.obj + "\n");
			break;
		case BluetoothControl.MSG_WRITE:
			tvData.append("S: " + msg.obj + "\n");
			break;
		}
		return false;
	}
	
}

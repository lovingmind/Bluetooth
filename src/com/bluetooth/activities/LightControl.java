package com.bluetooth.activities;

import android.os.Bundle;
import android.os.Message;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.bluetooth.BluetoothActivity;
import com.bluetooth.BluetoothControl;
import com.bluetooth.R;

public class LightControl extends BluetoothActivity implements CompoundButton.OnCheckedChangeListener{
	private Switch lightsw1,lightsw2,lightsw3,lightsw4,lightsw5,lightsw6;
	private LogView tvData1;
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.lightcontrol);
		tvData1 = (LogView) findViewById(R.id.tvData);
		
		lightsw1 = (Switch)findViewById(R.id.light1);
		lightsw2 = (Switch)findViewById(R.id.light2);
		lightsw3 = (Switch)findViewById(R.id.light3);
		lightsw4 = (Switch)findViewById(R.id.light4);
		lightsw5 = (Switch)findViewById(R.id.light5);
		lightsw6 = (Switch)findViewById(R.id.light6);
		
		
			lightsw1.setOnCheckedChangeListener(this);
			lightsw2.setOnCheckedChangeListener(this);
			lightsw3.setOnCheckedChangeListener(this);
			lightsw4.setOnCheckedChangeListener(this);
			lightsw5.setOnCheckedChangeListener(this);
			lightsw6.setOnCheckedChangeListener(this);
		}
	@Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		switch(buttonView.getId()){
		case R.id.light1:
			Toast.makeText(this, "light 1 is " + (isChecked ? "ON" : "OFF"),
	        Toast.LENGTH_SHORT).show();
			
			if (isChecked) {
				write("L1N");
            } else {
            	write("L1F");
            }
			break;
		case R.id.light2:
			Toast.makeText(this, "light 2 is " + (isChecked ? "ON" : "OFF"),
	        Toast.LENGTH_SHORT).show();

			if (isChecked) {
				write("L2N");
            } else {
            	write("L2F");
            }
			break;
		case R.id.light3:
			Toast.makeText(this, "light 3 is " + (isChecked ? "ON" : "OFF"),
	        Toast.LENGTH_SHORT).show();

			if (isChecked) {
				write("L3N");
            } else {
            	write("L3F");
            }
			break;
		case R.id.light4:
			Toast.makeText(this, "light 4 is " + (isChecked ? "ON" : "OFF"),
	        Toast.LENGTH_SHORT).show();

			if (isChecked) {
				write("L4N");
            } else {
            	write("L4F");
            }
			break;
		case R.id.light5:
			Toast.makeText(this, "light 5 is " + (isChecked ? "ON" : "OFF"),
	        Toast.LENGTH_SHORT).show();

			if (isChecked) {
				write("L5N");
            } else {
            	write("L5F");
            }
			break;
		case R.id.light6:
			Toast.makeText(this, "light 6 is " + (isChecked ? "ON" : "OFF"),
	        Toast.LENGTH_SHORT).show();

			if (isChecked) {
				write("L6N");
            } else {
            	write("L6F");
            }
			break;
		}
    }
	
	@Override
	public boolean handleMessage(Message msg)
	{
		super.handleMessage(msg);
		switch(msg.what)
		{
		case BluetoothControl.MSG_READ:
			tvData1.append("R: " + msg.obj + "\n");
			break;
		case BluetoothControl.MSG_WRITE:
			tvData1.append("S: " + msg.obj + "\n");
			break;
		}
		return false;
	}

}
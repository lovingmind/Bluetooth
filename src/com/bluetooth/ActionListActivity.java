package com.bluetooth;

import java.util.ArrayList;

import com.bluetooth.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ActionListActivity extends BluetoothActivity {
	private ArrayList<Action> activityList = new ArrayList<Action>();
	private ListView lvActionList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.action_select);
		lvActionList = (ListView) findViewById(R.id.lvActionList);

		activityList.add(new Action("Wi-Fi Control", "Use a computer to remotely control from a browser", "WiFiControl"));
		activityList.add(new Action("SerailCommunication", "Send custom commands to robot", "SerailCommunication"));
		activityList.add(new Action("LightControl", "lights control", "LightControl"));
		lvActionList.setAdapter(new ActionListBaseAdapter(this, activityList));
		lvActionList.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
			{
				String activity = activityList.get(position).getClassName();

				try
				{
					// Start the selected activity and prevent quitting
					preventCancel = true;
					Class<?> activityClass = Class.forName("com.bluetooth.activities." + activity);
					Intent intent = new Intent(ActionListActivity.this, activityClass);
					startActivityForResult(intent, 0);
				}
				catch(ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		super.handleMessage(msg);
		// When a child activity returns it passes ok or cancel message
		if(msg.what == BluetoothControl.MSG_OK)
		{
			// When quitting an activity automatically reset the robot
			write("r");
		}
		return false;
	}

	@Override
	public void onBackPressed()
	{
		// When quitting the activity select reset and disconnect from the device
		disconnect();
		super.onBackPressed();
	}
}


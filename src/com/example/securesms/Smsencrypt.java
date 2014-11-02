package com.example.securesms;

import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Smsencrypt extends ActionBarActivity {

	private Button sendSMS;
	private EditText msgTxt;
	private EditText numTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smsencrypt);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3fa9f5")));
		sendSMS = (Button) findViewById(R.id.sendbtn);
		msgTxt = (EditText) findViewById(R.id.message);
		numTxt = (EditText) findViewById(R.id.number);
		sendSMS.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSMSMessage();
			}

		});
	}

	protected void sendSMSMessage() {

		String phoneNo = numTxt.getText().toString();
		String message = msgTxt.getText().toString();
		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(phoneNo, null, message, null, null);
			Toast.makeText(getApplicationContext(), "SMS sent.",
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"SMS faild, please try again.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.smsencrypt, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

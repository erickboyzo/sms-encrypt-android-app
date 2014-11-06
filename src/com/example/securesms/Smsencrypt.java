package com.example.securesms;

import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Smsencrypt extends ActionBarActivity {

	private Button sendSMS;
	EditText msgTxt;
	EditText numTxt;

	IntentFilter intentFilter;

	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			TextView inTxt = (TextView) findViewById(R.id.textMsg);
			inTxt.setText(intent.getExtras().getString("sms"));

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smsencrypt);
		/*
		 * Remove ActionBar code and add permanent xml fix as this will cause
		 * issues on devices running Froyo 2.2
		 */
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3fa9f5")));

		intentFilter = new IntentFilter();
		intentFilter.addAction("SMS_RECEIVED_ACTION");

		sendSMS = (Button) findViewById(R.id.sendbtn);
		msgTxt = (EditText) findViewById(R.id.message);
		numTxt = (EditText) findViewById(R.id.number);

		sendSMS.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String myMsg = msgTxt.getText().toString();
				String theNumber = numTxt.getText().toString();
				sendMsg(theNumber, myMsg);
				

			}
		});

	}

	protected void sendMsg(String theNumber, final String myMsg) {
		String SENT = "Message Sent";
		String DELIVERED = "Message Delivered";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS Sent",
							Toast.LENGTH_LONG).show();
					TextView sentText = (TextView) findViewById(R.id.txtSent);
					sentText.setText(myMsg);
					numTxt.setText("");
					msgTxt.setText("");
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Generic Failure",
							Toast.LENGTH_LONG).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "No Service",
							Toast.LENGTH_LONG).show();
					break;
				}
			}
		}, new IntentFilter(SENT));

		registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context arg0, Intent arg1) {

				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS delivered",
							Toast.LENGTH_LONG).show();

					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), "SMS not Delivered",
							Toast.LENGTH_LONG).show();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(theNumber, null, myMsg, sentPI, deliveredPI);

	}

	@Override
	protected void onResume() {
		// register the receiver
		registerReceiver(intentReceiver, intentFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// unregister the receiver
		unregisterReceiver(intentReceiver);
		super.onPause();
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

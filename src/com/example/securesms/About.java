package com.example.securesms;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class About extends Activity {
	String about = "SecureSMS helps you send sensitive information with the ease of mind knowing it can only be seen by the intended party."
			+ " Start by entering the password that you will use to encrypt message. "
			+ " Enter number and message to be sent and hit send. "
			+ " Upon recieval of the secure message the recieving party simply enters password via NFC or by typing in the password and decrypts the message";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView aboutText = (TextView) findViewById(R.id.about);
		aboutText.setText(about);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3fa9f5")));
		Button startNfc = (Button) findViewById(R.id.start);
		startNfc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(About.this, WritePassword.class));

			}

		});

	}

}

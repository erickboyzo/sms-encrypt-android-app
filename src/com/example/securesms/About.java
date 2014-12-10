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
	String about = "SecureSMS encrypts and decrypts text messages you wish to send.  "
			+ "When a text message is sent the text message is automatically encrypted.  "
			+ "The receiving party will then receive the encrypted text message and enter the password to view the decrypted message.  "
			+ "Passwords can also be saved to nfc tags for easier storage and remembrance of the password."
			+ " Simply start by entering password following while placing phone over NFC tag where password will be saved  and tap save button. All Done! Your all set to start sendig secure messages.";

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

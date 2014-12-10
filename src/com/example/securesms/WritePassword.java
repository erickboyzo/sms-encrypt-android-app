package com.example.securesms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WritePassword extends ActionBarActivity {
	NfcAdapter adapter;
	PendingIntent pendingIntent;
	IntentFilter writeTagFilters[];
	boolean writeMode;
	Tag mytag;
	Context ctx;
	Tag detectedTag;
	NfcAdapter nfcAdapter;
	IntentFilter[] readTagFilters;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_write_password);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3fa9f5")));
		ctx = this;
		Button btnWrite = (Button) findViewById(R.id.button);
		final TextView message = (TextView) findViewById(R.id.password);

		btnWrite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (mytag == null) {
						Toast.makeText(ctx,
								ctx.getString(R.string.error_detected),
								Toast.LENGTH_LONG).show();
					} else {
						write(message.getText().toString(), mytag);
						Toast.makeText(ctx, ctx.getString(R.string.ok_writing),
								Toast.LENGTH_LONG).show();
					}
				} catch (IOException e) {
					Toast.makeText(ctx, ctx.getString(R.string.error_writing),
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (FormatException e) {
					Toast.makeText(ctx, ctx.getString(R.string.error_writing),
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
		});

		adapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFilters = new IntentFilter[] { tagDetected };

	}

	private void write(String text, Tag tag) throws IOException,
			FormatException {

		NdefRecord[] records = { createRecord(text) };
		NdefMessage message = new NdefMessage(records);
	
		Ndef ndef = Ndef.get(tag);
		
		ndef.connect();
		
		ndef.writeNdefMessage(message);
		
		ndef.close();
	}

	private NdefRecord createRecord(String text)
			throws UnsupportedEncodingException {
		String lang = "en";
		byte[] textBytes = text.getBytes();
		byte[] langBytes = lang.getBytes("US-ASCII");
		int langLength = langBytes.length;
		int textLength = textBytes.length;
		byte[] payload = new byte[1 + langLength + textLength];

		payload[0] = (byte) langLength;
		System.arraycopy(langBytes, 0, payload, 1, langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

		NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_TEXT, new byte[0], payload);

		return recordNFC;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Toast.makeText(this,
					this.getString(R.string.ok_detection) + mytag.toString(),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		WriteModeOff();
	}

	@Override
	public void onResume() {
		super.onResume();
		WriteModeOn();
	}

	private void WriteModeOn() {
		writeMode = true;
		adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters,
				null);
	}

	private void WriteModeOff() {
		writeMode = false;
		adapter.disableForegroundDispatch(this);
	}

}
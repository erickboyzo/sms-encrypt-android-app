package com.example.securesms;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.SecretKey;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Smsencrypt extends ActionBarActivity {

	private static final String TAG = MainActivity.class.getSimpleName();
	private Button sendSMS;
	private static EditText msgTxt;
	IntentFilter intentFilter;
	private ArrayList<Map<String, String>> mPeopleList;
	private SimpleAdapter mAdapter;
	private AutoCompleteTextView numTxt;
	public static final String MIME_TEXT_PLAIN = "text/plain";
	private EditText passwordText;
	private TextView encryptedText;
	private TextView decryptedText;
	private Button decryptButton;
	private Encryptor encryptor;
	String myMsg;

	abstract class Encryptor {
		SecretKey key;

		abstract SecretKey deriveKey(String passpword, byte[] salt);

		abstract String encrypt(String plaintext, String password);

		abstract String decrypt(String ciphertext, String password);

		String getRawKey() {
			if (key == null) {
				return null;
			}

			return Encryption.toHex(key.getEncoded());
		}
	}

	private final Encryptor PADDING_ENCRYPTOR = new Encryptor() {

		@Override
		public SecretKey deriveKey(String password, byte[] salt) {
			return Encryption.deriveKeyPad(password);
		}

		@Override
		public String encrypt(String plaintext, String password) {
			key = deriveKey(password, null);
			Log.d(TAG, "Generated key: " + getRawKey());

			return Encryption.encrypt(plaintext, key, null);
		}

		@Override
		public String decrypt(String ciphertext, String password) {
			SecretKey key = deriveKey(password, null);

			return Encryption.decryptNoSalt(ciphertext, key);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smsencrypt);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3fa9f5")));

		intentFilter = new IntentFilter();
		intentFilter.addAction("SMS_RECEIVED_ACTION");
		sendSMS = (Button) findViewById(R.id.sendbtn);
		msgTxt = (EditText) findViewById(R.id.message);
		numTxt = (AutoCompleteTextView) findViewById(R.id.number);
		encryptor = PADDING_ENCRYPTOR;
		passwordText = findById(R.id.password);
		encryptedText = findById(R.id.textMsg);
		decryptedText = findById(R.id.txtSent);
		decryptButton = findById(R.id.decrypt_button);

		// AutoComplete();

		sendSMS.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				final String password = passwordText.getText().toString()
						.trim();
				String myMsg = msgTxt.getText().toString();
				if (password.length() == 0) {
					Toast.makeText(getApplicationContext(),
							"Please enter a password.", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				final String plaintext = myMsg.toString();
				final String theNumber = numTxt.getText().toString();

				CryptoTask myTask = (CryptoTask) new CryptoTask() {
					@Override
					protected String doCrypto() {
						return encryptor.encrypt(plaintext, password);
					}

					@Override
					protected void updateUi(String ciphertext) {
						encryptedText.setText(ciphertext);
						msgTxt.setText("");
					}

					@Override
					protected void sendMessage(String ciphertext) {
						Log.d("secureSMS",
								"Sending the encrypted message via SMS");
						sendMsg(theNumber, ciphertext);
					}
				}.execute();

			}
		});

		decryptButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String message = getMessageText();
				String pass = getPass();
				// message = extractMessage(message);

				decryptMessage(message, pass);
			}
		});
		handleIntent(getIntent());

	}

	private void decryptMessage(String message, String pass) {
		final String password = pass;
		final String ciphertext = message;
		new CryptoTask() {

			@Override
			protected String doCrypto() {
				return encryptor.decrypt(ciphertext, password);
			}

			protected void updateUi(String plaintext) {
				setMessage(plaintext);
			}

			@Override
			protected void sendMessage(String stuff) {

			}

			private void setMessage(String plaintext) {
				decryptedText.setText(plaintext);
			}
		}.execute();
	}

	private String extractMessage(String message) {
		String extractedMessage = "";
		String[] stringArray = message.split(":");
		extractedMessage = stringArray[1];
		return extractedMessage;
	}

	private String getMessageText() {
		return encryptedText.getText().toString();
	}

	private String getPass() {
		return passwordText.getText().toString();
	}

	public static void setIncomingMessage(String aMessage) {
		msgTxt.setText(aMessage);
	}

	@Override
	protected void onNewIntent(Intent intent) {

		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {

		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

			String type = intent.getType();
			if (MIME_TEXT_PLAIN.equals(type)) {

				Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				new NdefReaderTask().execute(tag);

			} else {
				Log.d(TAG, "Wrong mime type: " + type);
			}
		} else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

			// In case we would still use the Tech Discovered Intent
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String[] techList = tag.getTechList();
			String searchedTech = Ndef.class.getName();

			for (String tech : techList) {
				if (searchedTech.equals(tech)) {
					new NdefReaderTask().execute(tag);
					break;
				}
			}
		}
	}

	public static void setupForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
		final Intent intent = new Intent(activity.getApplicationContext(),
				activity.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		final PendingIntent pendingIntent = PendingIntent.getActivity(
				activity.getApplicationContext(), 0, intent, 0);

		IntentFilter[] filters = new IntentFilter[1];
		String[][] techList = new String[][] {};

		filters[0] = new IntentFilter();
		filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
		filters[0].addCategory(Intent.CATEGORY_DEFAULT);
		try {
			filters[0].addDataType(MIME_TEXT_PLAIN);
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("Check your mime type.");
		}

		adapter.enableForegroundDispatch(activity, pendingIntent, filters,
				techList);
	}

	public static void stopForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

	public void AutoComplete() {
		mPeopleList = new ArrayList<Map<String, String>>();
		PopulatePeopleList();
		mAdapter = new SimpleAdapter(this, mPeopleList, R.layout.custcontview,
				new String[] { "Name", "Phone" }, new int[] { R.id.ccontName,
						R.id.ccontNo });
		numTxt.setAdapter(mAdapter);
		numTxt.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View arg1, int index,
					long arg3) {
				Map<String, String> map = (Map<String, String>) av
						.getItemAtPosition(index);
				Iterator<String> myVeryOwnIterator = map.keySet().iterator();
				while (myVeryOwnIterator.hasNext()) {
					String key = (String) myVeryOwnIterator.next();
					String value = (String) map.get(key);
					numTxt.setText(value);
				}
			}
		});
	}

	/*
	 * PopulatePeople List needs to be updated to new Loader class a solution is
	 * in the works
	 */

	public void PopulatePeopleList() {
		mPeopleList.clear();

		Cursor people = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		while (people.moveToNext()) {
			String contactName = people.getString(people
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

			String contactId = people.getString(people
					.getColumnIndex(ContactsContract.Contacts._ID));
			String hasPhone = people
					.getString(people
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if ((Integer.parseInt(hasPhone) > 0)) {

				// You know have the number so now query it like this
				Cursor phones = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = " + contactId, null, null);
				while (phones.moveToNext()) {

					// NEEDS WORK
					String phoneNumber = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

					String numberType = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

					Map<String, String> NamePhoneType = new HashMap<String, String>();

					NamePhoneType.put("Name", contactName);
					NamePhoneType.put("Phone", phoneNumber);

					if (numberType.equals("0"))
						NamePhoneType.put("Type", "Work");
					else if (numberType.equals("1"))
						NamePhoneType.put("Type", "Home");
					else if (numberType.equals("2"))
						NamePhoneType.put("Type", "Mobile");
					else
						NamePhoneType.put("Type", "Other");

					// Then add this map to the list.
					mPeopleList.add(NamePhoneType);
				}
				phones.close();
			}
		}
		people.close();

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
				int resultCode = getResultCode();
				switch (resultCode) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS Sent",
							Toast.LENGTH_LONG).show();
					TextView sentText = (TextView) findViewById(R.id.txtSent);
					sentText.setText(myMsg);
					msgTxt.setText("");
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Generic Failure",
							Toast.LENGTH_LONG).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Log.d("secureSMS", "The result code is " + resultCode);
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
							Toast.LENGTH_SHORT).show();

					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), "SMS not Delivered",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();

		Log.d("secureSMS: debug", "The number is " + theNumber);
		Log.d("secureSMS: debug", "The message is " + myMsg);

		try {
			sms.sendTextMessage(theNumber, null, myMsg, sentPI, deliveredPI);
		} catch (IllegalArgumentException e) {
			Log.e("secureSMS: error", "sendTextMessage() Failed");
			e.printStackTrace();
		}

	}

	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			TextView inTxt = (TextView) findViewById(R.id.textMsg);
			inTxt.setText(intent.getExtras().getString("sms"));

		}
	};

	@Override
	protected void onResume() {
		registerReceiver(intentReceiver, intentFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
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
		switch (item.getItemId()) {
		case R.id.action_nfc:
			// Single menu item is selected do something
			// Ex: launching new activity/screen or show alert message
			Toast.makeText(Smsencrypt.this, "NFC is Selected",
					Toast.LENGTH_SHORT).show();
			startActivity(new Intent(Smsencrypt.this, WritePassword.class));
			return true;

		case R.id.action_about:
			startActivity(new Intent(Smsencrypt.this, About.class));
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("unchecked")
	private <T> T findById(int id) {
		return (T) findViewById(id);
	}

	abstract class CryptoTask extends AsyncTask<Void, Void, String> {

		Exception error;

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);

		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				return doCrypto();
			} catch (Exception e) {
				error = e;
				Log.e(TAG, "Error: " + e.getMessage(), e);

				return null;
			}
		}

		protected abstract String doCrypto();

		@Override
		protected void onPostExecute(String result) {
			setProgressBarIndeterminateVisibility(false);

			if (error != null) {
				Toast.makeText(Smsencrypt.this, "Error: " + error.getMessage(),
						Toast.LENGTH_LONG).show();

				return;
			}

			updateUi(result);
			sendMessage(result);
		}

		protected abstract void updateUi(String result);

		protected abstract void sendMessage(String result);

	}

	private void clear() {
		encryptedText.setText("");
		decryptedText.setText("");

	}

	private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
		@Override
		protected String doInBackground(Tag... params) {
			Log.d("", "start doInBackground");
			Tag tag = params[0];
			Log.d("", "get the tag");
			Ndef ndef = Ndef.get(tag);
			Log.d("", "tag gotten");
			if (ndef == null) {
				Log.d("", "tag is null");
				return null;
			}

			NdefMessage ndefMessage = ndef.getCachedNdefMessage();

			NdefRecord[] records = ndefMessage.getRecords();
			for (NdefRecord ndefRecord : records) {
				if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN
						&& Arrays.equals(ndefRecord.getType(),
								NdefRecord.RTD_TEXT)) {
					try {
						Log.d("", "try to read the text");
						return readText(ndefRecord);
					} catch (UnsupportedEncodingException e) {
						Log.e(TAG, "Unsupported Encoding", e);
					}
				}
			}

			return null;
		}

		private String readText(NdefRecord record)
				throws UnsupportedEncodingException {

			Log.d("", "start reading");

			byte[] payload = record.getPayload();
			Log.d("", "got the payload");

			String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8"
					: "UTF-16";
			Log.d("", "got the encoding");

			int languageCodeLength = payload[0] & 0063;
			Log.d("", "get codelength");

			Log.d("", "Try to get the string now");
			return new String(payload, languageCodeLength + 1, payload.length
					- languageCodeLength - 1, textEncoding);
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				Log.d("", "Read contant: " + result);
				passwordText.setText(result);
			}
		}
	}

}

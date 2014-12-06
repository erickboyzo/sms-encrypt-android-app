package com.example.securesms;

import java.util.Timer;
import java.util.TimerTask;


import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends ActionBarActivity {
	AnimationDrawable faceAnimation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		ImageView imgFrame=(ImageView) findViewById(R.id.imageView1);
		imgFrame.setBackgroundResource(R.drawable.animation);
		faceAnimation=(AnimationDrawable) imgFrame.getBackground();
		faceAnimation.start();
		TimerTask ttask = new TimerTask() {

			@Override
			public void run() {
				finish();
				startActivity(new Intent(MainActivity.this, Smsencrypt.class));
			}
		};

		// schedule task
		Timer timer = new Timer();
		timer.schedule(ttask, 3000);
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

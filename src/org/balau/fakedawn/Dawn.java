/**
 *   Copyright 2012 Francesco Balducci
 *
 *   This file is part of FakeDawn.
 *
 *   FakeDawn is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   FakeDawn is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with FakeDawn.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.balau.fakedawn;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Dawn extends Activity implements OnClickListener {

	private long m_alarm_start_millis;
	private long m_alarm_end_millis;
	private boolean m_use_brightness = false;
	private Timer m_timer = new Timer();
	private int m_timer_tick_seconds = 10;

	private int m_brightnessMode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dawn);

		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN|
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		findViewById(R.id.dawn_background).setOnClickListener(this);
	}

	public void onClick(View v) {
		finish();
	}

	private void updateBrightness()
	{
		float brightnessStep = 0.01F;
		float brightness; 
		long level_percent;
		int grey_level;
		int grey_rgb;

		level_percent = 
				(100 * (System.currentTimeMillis() - m_alarm_start_millis))
				/ (m_alarm_end_millis - m_alarm_start_millis);
		if(level_percent < 1) { level_percent = 1; }
		else if(level_percent > 100) { level_percent = 100; }

		brightness = brightnessStep * level_percent;
		Log.d("HelloAndroid", String.format("b = %f", brightness));
		if(m_use_brightness)
		{
			WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
			layoutParams.screenBrightness = brightness;
			getWindow().setAttributes(layoutParams);
		}

		grey_level = (int)(brightness * (float)0xFF);
		if(grey_level > 0xFF) grey_level = 0xFF;
		grey_rgb = 0xFF000000 + (grey_level * 0x010101);
		findViewById(R.id.dawn_background).setBackgroundColor(grey_rgb);
		Log.d("FakeDawn", "Brightness updated.");		
		if(System.currentTimeMillis() >= m_alarm_end_millis)
		{
			m_timer.cancel();
			Log.d("FakeDawn", "Timer stopped.");
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
		String day;
		Calendar rightNow = Calendar.getInstance();

		switch (rightNow.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			day = "mondays";
			break;
		case Calendar.TUESDAY:
			day = "tuesdays";
			break;
		case Calendar.WEDNESDAY:
			day = "wednesdays";
			break;
		case Calendar.THURSDAY:
			day = "thursdays";
			break;
		case Calendar.FRIDAY:
			day = "fridays";
			break;
		case Calendar.SATURDAY:
			day = "saturdays";
			break;
		case Calendar.SUNDAY:
			day = "sundays";
			break;
		default:
			day = "NON_EXISTING_WEEKDAY";
			break;
		}
		if(!pref.getBoolean(day, false))
		{
			this.finish();
		}
		else
		{
			m_alarm_start_millis = rightNow.getTimeInMillis();
			m_alarm_end_millis = m_alarm_start_millis + (1000*60*pref.getInt("duration", 15));
			try {
				if(m_use_brightness)
				{
					m_brightnessMode = 
							Settings.System.getInt(
									getContentResolver(), 
									Settings.System.SCREEN_BRIGHTNESS_MODE);
					if (m_brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
						Settings.System.putInt(
								getContentResolver(),
								Settings.System.SCREEN_BRIGHTNESS_MODE,
								Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
					}
				}
				updateBrightness();

				m_timer.schedule(
						new TimerTask() {

							@Override
							public void run() {
								runOnUiThread(
										new Runnable() {
											public void run() {
												updateBrightness();
											}
										});
							}
						}, m_timer_tick_seconds*1000, m_timer_tick_seconds*1000);
			} catch (SettingNotFoundException e) {
				e.printStackTrace();
				this.finish();
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d("FakeDawn", "Dawn Paused.");		
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		m_timer.cancel();
		Log.d("FakeDawn", "Dawn Stopped.");
	}
}

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

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Dawn extends Activity implements OnClickListener, OnPreparedListener, OnCompletionListener, OnErrorListener {

	private static int TIMER_TICK_SECONDS = 10;
	private static final String ALARM_START_MILLIS = "ALARM_START_MILLIS";

	private long m_alarmStartMillis;
	private long m_alarmEndMillis;
	private Timer m_timer;

	private long m_soundStartMillis;
	private MediaPlayer m_player = new MediaPlayer();
	private boolean m_soundInitialized = false;

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
			m_alarmStartMillis = rightNow.getTimeInMillis();
			if(savedInstanceState != null)
			{
				if(savedInstanceState.containsKey(ALARM_START_MILLIS))
				{
					m_alarmStartMillis = savedInstanceState.getLong(ALARM_START_MILLIS);
				}
			}
			m_alarmEndMillis = m_alarmStartMillis + (1000*60*pref.getInt("duration", 15));

			m_player.setOnPreparedListener(this);
			m_player.setOnCompletionListener(this);
			m_player.setOnErrorListener(this);
			m_player.setAudioStreamType(AudioManager.STREAM_ALARM);
			m_player.reset();
			m_soundStartMillis = m_alarmEndMillis;

			String sound = pref.getString("sound", "");
			if(sound.isEmpty())
			{
				Log.d("FakeDawn", "Silent.");
			}
			else
			{
				Uri soundUri = Uri.parse(sound);

				if(soundUri != null)
				{
					AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
					int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM); 
					int volume = pref.getInt("volume", maxVolume/2);
					if(volume < 0) volume = 0;
					if(volume > maxVolume) volume = maxVolume;
					am.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
					try {
						m_player.setDataSource(this, soundUri);
						m_soundInitialized = true;
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				Log.d("FakeDawn", "Sound scheduled.");
			}

			updateBrightness();
			updateSound();

			m_timer = new Timer();
			m_timer.schedule(
					new TimerTask() {

						@Override
						public void run() {
							runOnUiThread(
									new Runnable() {
										public void run() {
											updateBrightness();
											updateSound();
										}
									});
						}
					}, TIMER_TICK_SECONDS*1000, TIMER_TICK_SECONDS*1000);

		}
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
				(100 * (System.currentTimeMillis() - m_alarmStartMillis))
				/ (m_alarmEndMillis - m_alarmStartMillis);
		if(level_percent < 1) { level_percent = 1; }
		else if(level_percent > 100) { level_percent = 100; }

		brightness = brightnessStep * level_percent;
		Log.d("HelloAndroid", String.format("b = %f", brightness));

		grey_level = (int)(brightness * (float)0xFF);
		if(grey_level > 0xFF) grey_level = 0xFF;
		grey_rgb = 0xFF000000 + (grey_level * 0x010101);
		findViewById(R.id.dawn_background).setBackgroundColor(grey_rgb);
	}

	private void updateSound()
	{
		if(m_soundInitialized)
		{
			if(System.currentTimeMillis() >= m_soundStartMillis)
			{
				if(!m_player.isPlaying())
				{
					m_player.prepareAsync();
				}
			}			
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		m_player.setLooping(true);
		mp.start();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		m_timer.cancel();
		if(m_soundInitialized)
		{
			if(m_player.isPlaying())
			{
				m_player.stop();
			}
			m_soundInitialized = false;
		}
		Log.d("FakeDawn", "Dawn Stopped.");
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e("FakeDawn", String.format("MediaPlayer error. what: %d, extra: %d", what, extra));
		m_player.reset();
		m_soundInitialized = false;
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.w("FakeDawn", "Sound completed even if looping.");
		m_player.stop();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ALARM_START_MILLIS, m_alarmStartMillis);
	}
}

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class Alarm extends Service {

	public static final String EXTRA_SHOW_TOAST = "org.balau.fakedawn.Alarm.EXTRA_SHOW_TOAST";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		boolean showToast;
		if(intent != null)
		{
			showToast = intent.getBooleanExtra(EXTRA_SHOW_TOAST, false);
		}
		else // intent is null when Service is restarted
		{
			showToast = false;
		}

		this.cancel();
		String message;
		if(this.getPreferences().getBoolean("enabled", false))
		{
			Calendar nextAlarmTime = this.set();
			message = this.nextAlarmMessage(nextAlarmTime);
		}
		else
		{
			message = "Fake Dawn Alarm Disabled.";
		}
		Log.d("FakeDawn", message);
		if(showToast)
		{
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
		}
		// If we get killed, after returning from here, restart
		return START_STICKY;
	}
	
	private PendingIntent getOpenDawnPendingIntent()
	{
		Intent openDawn = new Intent(AlarmReceiver.ACTION_START_ALARM);
		PendingIntent openDawnPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 
				0, 
				openDawn,
				0);
		return openDawnPendingIntent;
	}
	
	private AlarmManager getAlarmManager()
	{
		return (AlarmManager) getSystemService(ALARM_SERVICE);
	}
	
	private SharedPreferences getPreferences()
	{
		return getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
	}
	
	private void cancel()
	{
		this.getAlarmManager().cancel(
				this.getOpenDawnPendingIntent());
	}
	
	private Calendar set()
	{
		SharedPreferences pref = this.getPreferences();
		Calendar nextAlarmTime = Calendar.getInstance();
		nextAlarmTime.set(Calendar.HOUR_OF_DAY, pref.getInt("dawn_start_hour", 8));
		nextAlarmTime.set(Calendar.MINUTE, pref.getInt("dawn_start_minute", 0));
		nextAlarmTime.set(Calendar.SECOND, 0);
		long toleranceMillis = 1000*10; //10s
		if(nextAlarmTime.getTimeInMillis() < System.currentTimeMillis() + toleranceMillis)
		{
			nextAlarmTime.add(Calendar.DAY_OF_YEAR, 1);
			//TODO: check if enough?
		}
		this.set(nextAlarmTime);
		return nextAlarmTime;
	}
	
	private void set(Calendar nextAlarmTime)
	{
		AlarmManager alarmManager = this.getAlarmManager();
		PendingIntent openDawnIntent = this.getOpenDawnPendingIntent();
    	// API 19 changed set() behaviour and added setExact
		// https://developer.android.com/reference/android/app/AlarmManager.html#set(int, long, android.app.PendingIntent)
		// Using setExact if it exists, otherwise fall back to set.
	    try {
	        Method setExact = AlarmManager.class.getDeclaredMethod(
	            "setExact", int.class, long.class, PendingIntent.class);
	        setExact.invoke(alarmManager, AlarmManager.RTC_WAKEUP,
	        		nextAlarmTime.getTimeInMillis(), openDawnIntent);
	      } catch (NoSuchMethodException e) {
	        alarmManager.set(AlarmManager.RTC_WAKEUP,
	        		nextAlarmTime.getTimeInMillis(), openDawnIntent);
	      } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	      } catch (IllegalArgumentException e) {
	        throw new RuntimeException(e);
	      } catch (InvocationTargetException e) {
	        throw new RuntimeException(e);
	      }
	}
	
	private String nextAlarmMessage(Calendar nextAlarmTime)
	{
		long elapsed = nextAlarmTime.getTimeInMillis() - System.currentTimeMillis();
		long dayMillis = 1000*60*60*24;
		long elapsedDays = elapsed / dayMillis;
		elapsed -= elapsedDays * dayMillis;
		long hourMillis = 1000*60*60;
		long elapsedHours = elapsed / hourMillis;
		String message;
		if (elapsedDays > 0)
		{
			message = String.format(
					"Fake Dawn starting in %d days and %d hours.",
					elapsedDays, elapsedHours);
		}
		else
		{
			elapsed -= elapsedHours * hourMillis;
			long minuteMillis = 1000*60;
			long elapsedMinutes = elapsed / minuteMillis;
			if (elapsedHours > 0)
			{
				message = String.format(
						"Fake Dawn starting in %d hours and %d minutes.",
						elapsedHours, elapsedMinutes);
			}
			else
			{
				elapsed -= elapsedMinutes * minuteMillis;
				long secondMillis = 1000;
				long elapsedSeconds = elapsed / secondMillis;
				message = String.format(
						"Fake Dawn starting in %d minutes and %d seconds.",
						elapsedMinutes, elapsedSeconds);
			}
		}
		return message;
	}
}

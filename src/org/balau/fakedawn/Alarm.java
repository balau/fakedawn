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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class Alarm extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private PendingIntent getDawnPendingIntent()
	{
		Intent openDawn = new Intent(getApplicationContext(), Dawn.class);
		openDawn.setFlags(
				Intent.FLAG_ACTIVITY_NEW_TASK|
				Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|
				Intent.FLAG_FROM_BACKGROUND);
		return PendingIntent.getActivity(
				getApplicationContext(), 
				0, 
				openDawn,
				0);	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

		am.cancel(getDawnPendingIntent());

		if(pref.getBoolean("enabled", false))
		{
			Calendar nextAlarmTime = Calendar.getInstance();
			nextAlarmTime.set(Calendar.HOUR_OF_DAY, pref.getInt("hour", 8));
			nextAlarmTime.set(Calendar.MINUTE, pref.getInt("minute", 0));
			nextAlarmTime.set(Calendar.SECOND, 0);
			if(nextAlarmTime.getTimeInMillis() < System.currentTimeMillis())
			{
				nextAlarmTime.add(Calendar.DAY_OF_YEAR, 1);
				//TODO: check if enough?
			}

			am.setRepeating(
					AlarmManager.RTC_WAKEUP, 
					nextAlarmTime.getTimeInMillis(),
					AlarmManager.INTERVAL_DAY,
					getDawnPendingIntent());
			Log.d("FakeDawn", String.format("Alarm set for %s.", nextAlarmTime.toString()));	

		}
		// If we get killed, after returning from here, restart
		return START_STICKY;
	}
}

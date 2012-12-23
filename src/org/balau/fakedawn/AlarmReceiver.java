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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * @author francesco
 *
 */
public class AlarmReceiver extends BroadcastReceiver {
	static final String ACTION_START_ALARM = "org.balau.fakedawn.AlarmReceiver.ACTION_START_ALARM";
	static final String ACTION_STOP_ALARM = "org.balau.fakedawn.AlarmReceiver.ACTION_STOP_ALARM";

	private static WakeLock m_alarmWakeLock = null;

	private void releaseWakeLock(boolean expectedHeld) {
		if(AlarmReceiver.m_alarmWakeLock != null)
		{
			if(!expectedHeld)
				Log.w("FakeDawn", "ACTION_START_ALARM received but WakeLock already present.");
			if(AlarmReceiver.m_alarmWakeLock.isHeld())
			{
				if(!expectedHeld)
					Log.w("FakeDawn", "ACTION_START_ALARM received but WakeLock already held.");
				AlarmReceiver.m_alarmWakeLock.release();
				AlarmReceiver.m_alarmWakeLock = null;
			}
			else
			{
				if(expectedHeld)
					Log.w("FakeDawn", "ACTION_STOP_ALARM received but no WakeLock held.");
			}
		}
		else
		{
			if(expectedHeld)
				Log.w("FakeDawn", "ACTION_STOP_ALARM received but no WakeLock present.");
		}
	}
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		if(intent.getAction().equals(ACTION_START_ALARM))
		{
			Log.d("FakeDawn", "ACTION_START_ALARM received.");
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			releaseWakeLock(false);
			AlarmReceiver.m_alarmWakeLock = pm.newWakeLock(
					PowerManager.FULL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP,
					"FakeDawn.AlarmReceiver");
			AlarmReceiver.m_alarmWakeLock.acquire();
			Intent openDawn = new Intent(context, Dawn.class);
			openDawn.setFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK|
					Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|
					Intent.FLAG_FROM_BACKGROUND);
			context.startActivity(openDawn);
			//TODO: start sound service?
		}
		else if(intent.getAction().equals(ACTION_STOP_ALARM))
		{
			Log.d("FakeDawn", "ACTION_STOP_ALARM received.");
			releaseWakeLock(true);
			//TODO: stop service and activity?
		}
	}

}

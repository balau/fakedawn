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
import android.util.Log;

public class StartAtBoot extends BroadcastReceiver {

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean mustStart = false;
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
			mustStart = true;
		}
		else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
			if (intent.getData().getSchemeSpecificPart().equals(context.getPackageName())) {
				mustStart = true;
				Log.d("FakeDawn", "Package Replaced.");
			}
		}
		if(mustStart)
		{
			Intent startService = new Intent(context, Alarm.class);			
			context.startService(startService);
			Log.d("FakeDawn", "Alarm started.");		
		}
	}

}

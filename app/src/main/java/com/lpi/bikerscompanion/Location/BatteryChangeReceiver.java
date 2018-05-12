package com.lpi.bikerscompanion.Location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.lpi.bikerscompanion.MainActivity;
import com.lpi.bikerscompanion.TextToSpeech.TextToSpeechManager;
import com.lpi.bikerscompanion.Utils.PersistentData;

/**
 * Created by lucien on 10/05/2018.
 */

public class BatteryChangeReceiver extends BroadcastReceiver {
	/***
	 * Reception d'un changement d'etat de la batterie
	 * @param context
	 * @param intent
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//Log.debug( "BatteryChangeReceiver:" +  intent.getAction()) ;
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BATTERY_CHANGED))
			onBatterie(context, intent);
	}

	/***
	 * Surveillance du niveau de la batterie
	 * @param context
	 */
	private void onBatterie(Context context, Intent intent)
	{
		PersistentData data = PersistentData.getInstance(context);
		if (data.modeTrajet == PersistentData.MODE_TRAJET_ARRET)
			return;

		int rawlevel = intent.getIntExtra("level", -1);
		int scale = intent.getIntExtra("scale", -1);
		int level = -1;
		if (rawlevel >= 0 && scale > 0)
		{
			level = (rawlevel * 100) / scale;
		}

		int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
		if (status == BatteryManager.BATTERY_STATUS_DISCHARGING)
		{
			if (level <= 10 && !data.annonceBatterie10)
			{
				data.annonceBatterie10 = true;
				TextToSpeechManager.getInstance(context).annonce("Batterie à 10%", TextToSpeechManager.REPETITION_JAMAIS, 0, MainActivity.CATEGORIE_ANNONCE_BATTERIE_10);
				data.flush();
			}

			else if (level <= 25 && !data.annonceBatterie25)
			{
				data.annonceBatterie25 = true;
				TextToSpeechManager.getInstance(context).annonce("Batterie à 25%", TextToSpeechManager.REPETITION_JAMAIS, 0, MainActivity.CATEGORIE_ANNONCE_BATTERIE_25);
				data.flush();
			}

			else if (level <= 50 && !data.annonceBatterie50)
			{
				data.annonceBatterie50 = true;
				TextToSpeechManager.getInstance(context).annonce("Batterie à 50%", TextToSpeechManager.REPETITION_JAMAIS, 0, MainActivity.CATEGORIE_ANNONCE_BATTERIE_50);
				data.flush();
			}
		}
	}
}

package com.lpi.bikerscompanion.Location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.lpi.bikerscompanion.MainActivity;
import com.lpi.bikerscompanion.R;
import com.lpi.bikerscompanion.TextToSpeech.TextToSpeechManager;
import com.lpi.bikerscompanion.Utils.PersistentData;
import com.lpi.bikerscompanion.Utils.Preferences;

public class LocationBroadcastReceiver extends BroadcastReceiver {

	public static final String ACTION_KILOMETRE = "com.lpi.bikerscompanion.LocationBroadcastReceiver.kilometre";
	public static final String EXTRA_KILOMETRE = "com.lpi.bikerscompanion.LocationBroadcastReceiver.kilometre";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		if (PositionService.ACTION_NEW_LOCATION.equals(action))
		{
			//TextToSpeechManager.getInstance(context).playTone(ToneGenerator.TONE_DTMF_2);
			Location location = intent.getParcelableExtra(PositionService.PARAM_LOCATION);
			onNewLocation(context, location);
		}
		else if (HourAlarms.TIME_UPDATE.equals(action))
			HourAlarms.getInstance(context).annonceHeure(intent);
		else if (HourAlarms.PAUSE_UPDATE.equals(action))
			HourAlarms.getInstance(context).pause(intent);
	}


	/***
	 * Reception d'une nouvelle position
	 * @param ici Nouvelle position
	 */
	private void onNewLocation(Context _context, Location ici)
	{
		TextToSpeechManager tts = TextToSpeechManager.getInstance(_context);
		PersistentData data = PersistentData.getInstance(_context);
		if (data.dernierePosition == null)
		{
			// Premiere position recue
			data.dernierePosition = ici;
		}
		else
		{
			if (!isBetterLocation(ici, data.dernierePosition))
				// Pas une meilleure localisation
				return;

			switch (data.modeTrajet)
			{
				case PersistentData.MODE_TRAJET_ENROUTE:
				{
					Preferences prefs = Preferences.getInstance(_context);

					// Vitesse
					if ((ici.getSpeed() * 3.6f) > prefs.vitesseMaxKmH)
					{
						int vitesse = Math.round(ici.getSpeed() * 3.6f);
						tts.annonce(String.format(_context.getResources().getString(R.string.trop_vite), vitesse), TextToSpeechManager.REPETITION_DELAI, 60, MainActivity.CATEGORIE_VITESSE);
					}

					// Distance parcourue
					final float distance = data.dernierePosition.distanceTo(ici);
					int distanceParcourueKM = (int) Math.floor(distance) / 1000;
					if (distanceParcourueKM != data.dernierKilometre)
					{
						data.dernierKilometre = distanceParcourueKM;

						// Mettre a jour l'interface
						Intent intent = new Intent(ACTION_KILOMETRE);
						intent.putExtra(EXTRA_KILOMETRE, distanceParcourueKM);
						_context.sendBroadcast(intent);

						// Annonce dizaine de kilometre
						int dizaine = (int) distance / 10;
						if (dizaine != data.derniereDizaine)
						{
							tts.annonce(String.format("%1$d kilomètres parcourus", dizaine), TextToSpeechManager.REPETITION_DELAI, 10 * 60, MainActivity.CATEGORIE_DIZAINE);
							data.derniereDizaine = dizaine;
						}
					}

					data.distanceParcourueM += distance;
					data.autonomieRestanteM -= distance;

					if (prefs.alerteAutonomie)
					{
						if (prefs.alerteDemiReservoir)
							if (data.autonomieRestanteM <= (prefs.autonomieMaxMetres / 2))
							{
								// Alerte demi reservoir
								tts.annonce("Attention, réservoir à moitié vide", TextToSpeechManager.REPETITION_DELAI, 10 * 60, MainActivity.CATEGORIE_DEMIRESERVOIR);
							}

						if (prefs.alerteQuartReservoir)
							if (data.autonomieRestanteM <= (prefs.autonomieMaxMetres / 4))
							{
								// Alerte quart reservoir
								tts.annonce("Attention, plus que le quart du réservoir", TextToSpeechManager.REPETITION_DELAI, 5 * 60, MainActivity.CATEGORIE_QUARTRESERVOIR);
							}
					}
					break;

				}
				default:
					break;
			}
		}

		data.dernierePosition = ici;
		data.flush();
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 *
	 * @param location            The new Location that you want to evaluate
	 * @param currentBestLocation The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation)
	{
		if (currentBestLocation == null)
		{
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > PositionService.TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -PositionService.TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer)
		{
			return true;
			// If the new location is more than two minutes older, it must be worse
		}
		else if (isSignificantlyOlder)
		{
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate)
		{
			return true;
		}
		else if (isNewer && !isLessAccurate)
		{
			return true;
		}
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
		{
			return true;
		}
		return false;
	}

	/**
	 * Checks whether two providers are the same
	 */
	private boolean isSameProvider(String provider1, String provider2)
	{
		if (provider1 == null)
		{
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}


}

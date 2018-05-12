package com.lpi.bikerscompanion.Location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.lpi.bikerscompanion.TextToSpeech.TextToSpeechManager;
import com.lpi.bikerscompanion.Utils.PersistentData;
import com.lpi.bikerscompanion.Utils.Preferences;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by lucien on 05/05/2018.
 */

public class HourAlarms {
	public static final int TYPEALARME_RIEN = 0;
	public static final int TYPEALARME_PAUSE = 1;
	public static final int TYPEALARME_HEURE = 2;
	public static final String TIME_UPDATE = "lpi.BikersCompanion.Alarme";
	public static final String PAUSE_UPDATE = "lpi.BikersCompanion.Pause";

	private static HourAlarms INSTANCE = null;
	private Context _context;
	private PendingIntent pendingIntentAlarme;

	/**
	 * Point d'accès pour l'instance unique du singleton
	 */
	public static synchronized HourAlarms getInstance(Context context)
	{
		if (INSTANCE == null)
		{
			INSTANCE = new HourAlarms(context);
		}
		return INSTANCE;
	}

	@Override
	public void finalize()
	{
		try
		{
			super.finalize();
		} catch (Throwable throwable)
		{
			throwable.printStackTrace();
		}
	}

	private HourAlarms(Context context)
	{
		_context = context;
	}

	/***
	 * Plannifier la prochaine alarme: carillon ou pause
	 */
	public void setNextAlarm()
	{
		PersistentData data = PersistentData.getInstance(_context);
		if (data.modeTrajet != PersistentData.MODE_TRAJET_ENROUTE)
			return;

		Preferences pref = Preferences.getInstance(_context);
		long ProchaineAlarme = Long.MAX_VALUE;

		int type = TYPEALARME_RIEN;

		// Annonce des pauses
		final long minutesEntrePauses = pref.getMinutesEntrePauses();
		if (minutesEntrePauses > 0)
		{
			type = TYPEALARME_PAUSE;
			ProchaineAlarme = data.derniereHeurePause + (minutesEntrePauses * 60L * 1000L);
		}

		// Annonce de l'heure
		final long minutesEntreAnnonceHeure = pref.getMinutesEntreAnnonceHeure();
		if (minutesEntreAnnonceHeure > 0)
		{
			long prochainCarillon = getHeureProchainCarillon(Calendar.getInstance(), pref.annonceHeure).getTimeInMillis();
			if (prochainCarillon < ProchaineAlarme)
			{
				type = TYPEALARME_HEURE;
				ProchaineAlarme = prochainCarillon;
			}
		}

		// Configurer l'alarme
		if (type != TYPEALARME_RIEN)
		{
			Intent intent = new Intent(type == TYPEALARME_HEURE ? TIME_UPDATE : PAUSE_UPDATE);
			PositionService.updateNotification(_context, "Prochaine alarme: " + toHourString(ProchaineAlarme) + " (" + (type == TYPEALARME_HEURE ? "carillon)" : "pause)"));

			pendingIntentAlarme = PendingIntent.getBroadcast(_context, 0, intent, 0);
			AlarmManager alarmManager = (AlarmManager) _context.getSystemService(ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, ProchaineAlarme, pendingIntentAlarme);
			data.derniereHeureAlarme = ProchaineAlarme;
		}

		data.flush();
	}

	/**
	 * Calcule une representation textuel de l'heure
	 *
	 * @param prochaineAlarme
	 * @return
	 */
	@SuppressWarnings("nls")
	public static String toHourString(long prochaineAlarme)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(prochaineAlarme);
		return String.valueOf(c.get(Calendar.HOUR_OF_DAY)) + ":" + c.get(Calendar.MINUTE) + ":"
				       + c.get(Calendar.SECOND);
	}

	/**
	 * Calcule quand on doit annoncer la prochaine heure pleine, demi heure ou quart d'heure
	 *
	 * @return
	 */
	private long getNextHourAlarm(long minutesEntreAnnonce, PersistentData data)
	{

		Calendar depart = Calendar.getInstance();
		depart.setTimeInMillis(data.derniereHeureAlarme);

		// Au moins une minute plus tard
		depart.roll(Calendar.MILLISECOND, true);
		depart.set(Calendar.SECOND, 0);

		int Minutes = depart.get(Calendar.MINUTE);
		Minutes -= (Minutes % minutesEntreAnnonce);
		Minutes += minutesEntreAnnonce;
		while (Minutes > 60)
		{
			Minutes -= 60;
			depart.roll(Calendar.HOUR_OF_DAY, 1);
		}

		depart.set(Calendar.MINUTE, Minutes);
		return depart.getTimeInMillis();
	}

	/***
	 * Annonce l'heure
	 * @param intent
	 */
	public void annonceHeure(Intent intent)
	{
		PersistentData data = PersistentData.getInstance(_context);
		if (data.modeTrajet == PersistentData.MODE_TRAJET_ENROUTE)
		{
			pendingIntentAlarme = null;

			Calendar c = Calendar.getInstance();
			TextToSpeechManager.getInstance(_context).annonce(String.format("Il est %1$d heures %2$d minutes", Integer.valueOf(c.get(Calendar.HOUR_OF_DAY)),
					Integer.valueOf(c.get(Calendar.MINUTE))), TextToSpeechManager.REPETITION_TOUJOURS, 0, -1);

			c.set(Calendar.SECOND, 0);
			data.derniereHeureAlarme = Calendar.getInstance().getTimeInMillis();
			data.flush();

			setNextAlarm();
		}
	}

	/***
	 * Annonce qu'il est temps de faire une pause
	 * @param intent
	 */
	public void pause(Intent intent)
	{
		PersistentData data = PersistentData.getInstance(_context);
		if (data.modeTrajet == PersistentData.MODE_TRAJET_ENROUTE)
		{
			pendingIntentAlarme = null;
			data.derniereHeureAlarme = Calendar.getInstance().getTimeInMillis();
			TextToSpeechManager.getInstance(_context).annonce("Il est temps de faire une pause", TextToSpeechManager.REPETITION_TOUJOURS, 0, -1);
			data.flush();

			setNextAlarm();
		}
	}


	public Calendar getHeureProchainCarillon(Calendar actuel, int typeAnnonceHeures)
	{
		Calendar prochaine = (Calendar) actuel.clone();
		prochaine.set(Calendar.SECOND, 0);
		prochaine.roll(Calendar.MINUTE, 1);

		switch (typeAnnonceHeures)
		{
			case Preferences.ANNONCE_HEURE_HEURES:
				// Carillon a la prochaine heure
				prochaine.set(Calendar.MINUTE, 0);
				prochaine.roll(Calendar.HOUR, 1);
				break;


			case Preferences.ANNONCE_HEURE_DEMI:
				// Carillon a la prochaine demi heure
			{
				int minute = prochaine.get(Calendar.MINUTE);
				if (minute < 30)
					prochaine.set(Calendar.MINUTE, 30);
				else
				{
					prochaine.set(Calendar.MINUTE, 0);
					prochaine.roll(Calendar.HOUR, 1);
				}
				break;
			}

			case Preferences.ANNONCE_HEURE_QUART:
				// Carillon au prochain quart d'heure
			{
				int minute = prochaine.get(Calendar.MINUTE);
				if (minute < 15)
					prochaine.set(Calendar.MINUTE, 15);
				else if (minute < 30)
					prochaine.set(Calendar.MINUTE, 30);
				else if (minute < 45)
					prochaine.set(Calendar.MINUTE, 45);
				else
				{
					prochaine.set(Calendar.MINUTE, 0);
					prochaine.roll(Calendar.HOUR, 1);
				}
			}
			default:

		}

		return prochaine;
	}
}

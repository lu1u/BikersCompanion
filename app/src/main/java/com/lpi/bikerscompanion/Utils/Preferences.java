package com.lpi.bikerscompanion.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by lucien on 23/04/2018.
 */

public class Preferences {
	private static Preferences INSTANCE = null;
	private Context _context;

	static private String PREFS_ANNONCEHEURE = "lpi.bikercompanion.annonceheure"; //$NON-NLS-1$
	static private String PREFS_ANNONCEPAUSES = "lpi.bikercompanion.annoncepauses"; //$NON-NLS-1$
	static private String PREFS_THEME = "lpi.bikercompanion.theme"; //$NON-NLS-1$


	static private String PREFS_ANNONCE_SMS = "lpi.bikercompanion.annonceSMS"; //$NON-NLS-1$
	static private String PREFS_AUTONOMIERESERVOIR = "lpi.bikercompanion.autonomie"; //$NON-NLS-1$
	static private String PREFS_ALERTEAUTONOMIE = "lpi.bikercompanion.alerte_autonomie"; //$NON-NLS-1$
	static private String PREFS_ALERTEDEMIRESERVOIR = "lpi.bikercompanion.alerte_mireservoir"; //$NON-NLS-1$
	static private String PREFS_ALERTEQUARTRESERVOIR = "lpi.bikercompanion.alerte_quartreservoir"; //$NON-NLS-1$
	static private String PREFS_VITESSEMAX = "lpi.bikercompanion.vitesse_max"; //$NON-NLS-1$
	static private String PREFS_ALERTEVITESSEMAX = "lpi.bikercompanion.alerte_vitesse_max"; //$NON-NLS-1$
	static private String PREFS_SORTIE_AUDIO = "lpi.bikercompanion.sortieaudio"; //$NON-NLS-1$
	private static final String PREFS_PRECISION_GPS_DELAI = "lpi.bikercompanion.gps.delai";
	private static final String PREFS_PRECISION_GPS_DISTANCE = "lpi.bikercompanion.gps.distance";



	// Vitesse max
	public boolean alerteVitesseMax = true;
	public int vitesseMaxKmH = 130;

	// Autonomie
	public boolean alerteAutonomie = true;
	public int autonomieMaxMetres = 300000;
	public boolean alerteDemiReservoir = true;
	public boolean alerteQuartReservoir = true;

	// Pauses
	public static final int ANNONCE_PAUSE_JAMAIS = 0;
	public static final int ANNONCE_PAUSE_DEUX_HEURES = 1;
	public static final int ANNONCE_PAUSE_HEURE = 2;
	public int annoncePauses = ANNONCE_PAUSE_DEUX_HEURES;

	// Heure
	public static final int ANNONCE_HEURE_JAMAIS = 0;
	public static final int ANNONCE_HEURE_HEURES = 1;
	public static final int ANNONCE_HEURE_DEMI = 2;
	public static final int ANNONCE_HEURE_QUART = 3;
	public int annonceHeure = ANNONCE_HEURE_HEURES ;

	// Precision GPS
	public long delaiGPSMillisecondes = 5000;
	public float distanceGPSMetres = 10;

	// Theme
	public int theme = 0;

	//SMS
	public static final int ANNONCE_SMS_JAMAIS = 0;
	public static final int ANNONCE_SMS_CONTACTS = 1;
	public static final int ANNONCE_SMS_TOUS = 2;
	public int litSMS = ANNONCE_SMS_CONTACTS ;

	// Sortie audio
	public static final int SORTIE_AUDIO_DEFAUT = 0;
	public static final int SORTIE_AUDIO_HAUTPARLEUR = 1;
	public static final int SORTIE_AUDIO_BLUETOOTH = 2;
	public int sortieAudio = SORTIE_AUDIO_DEFAUT ;

	private Preferences(Context context)
	{
		_context = context;

		// Lire les preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		alerteVitesseMax = settings.getBoolean(PREFS_ALERTEVITESSEMAX, alerteVitesseMax);
		vitesseMaxKmH = settings.getInt(PREFS_VITESSEMAX, vitesseMaxKmH);

		// Autonomie
		alerteAutonomie = settings.getBoolean(PREFS_ALERTEAUTONOMIE, alerteAutonomie);
		autonomieMaxMetres = settings.getInt(PREFS_AUTONOMIERESERVOIR, autonomieMaxMetres);
		alerteDemiReservoir = settings.getBoolean(PREFS_ALERTEDEMIRESERVOIR, alerteDemiReservoir);
		alerteQuartReservoir = settings.getBoolean(PREFS_ALERTEQUARTRESERVOIR, alerteQuartReservoir);

		// Pauses
		annoncePauses = settings.getInt(PREFS_ANNONCEPAUSES, annoncePauses);

		// Heure
		annonceHeure = settings.getInt(PREFS_ANNONCEHEURE, annonceHeure);

		// Lire SMS
		litSMS = settings.getInt(PREFS_ANNONCE_SMS, litSMS);

		// sortie audio
		sortieAudio = settings.getInt(PREFS_SORTIE_AUDIO, sortieAudio);

		// Precision GPS
		delaiGPSMillisecondes = settings.getLong(PREFS_PRECISION_GPS_DELAI, delaiGPSMillisecondes);
		distanceGPSMetres = settings.getFloat(PREFS_PRECISION_GPS_DISTANCE, distanceGPSMetres);

		theme = settings.getInt(PREFS_THEME, theme);
	}

	/**
	 * Point d'accès pour l'instance unique du singleton
	 */
	public static synchronized Preferences getInstance(Context context)
	{
		if (INSTANCE == null)
		{
			INSTANCE = new Preferences(context);
		}
		return INSTANCE;
	}

	@Override
	public void finalize()
	{
		flush();
	}

	public void flush()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
		SharedPreferences.Editor editor = settings.edit();

		// Vitesse max
		editor.putBoolean(PREFS_ALERTEVITESSEMAX, alerteVitesseMax);
		editor.putInt(PREFS_VITESSEMAX, vitesseMaxKmH);

		// Autonomie
		editor.putInt(PREFS_AUTONOMIERESERVOIR, autonomieMaxMetres);
		editor.putBoolean(PREFS_ALERTEAUTONOMIE, alerteAutonomie);
		editor.putBoolean(PREFS_ALERTEDEMIRESERVOIR, alerteDemiReservoir);
		editor.putBoolean(PREFS_ALERTEQUARTRESERVOIR, alerteQuartReservoir);

		// Pause
		editor.putInt(PREFS_ANNONCEPAUSES, annoncePauses);

		// Heure
		editor.putInt(PREFS_ANNONCEHEURE, annonceHeure);

		// Lire SMS
		editor.putInt(PREFS_ANNONCE_SMS, litSMS);

		// sortie audio
		editor.putInt(PREFS_SORTIE_AUDIO, sortieAudio) ;

		// Precision GPS
		editor.putLong(PREFS_PRECISION_GPS_DELAI, delaiGPSMillisecondes);
		editor.putFloat(PREFS_PRECISION_GPS_DISTANCE, distanceGPSMetres);

		editor.putInt(PREFS_THEME, theme);
		editor.apply();
	}

	public long getMinutesEntrePauses()
	{
		switch( annoncePauses)
		{
			case ANNONCE_PAUSE_DEUX_HEURES:
				return 120;

			case ANNONCE_PAUSE_HEURE:
				return 60;

			default:
				return 0;
		}
	}

	public long getMinutesEntreAnnonceHeure()
	{
		switch (annonceHeure)
		{
			case ANNONCE_HEURE_HEURES:
				return 60;
			case ANNONCE_HEURE_DEMI :
				return 30;
			case ANNONCE_HEURE_QUART :
				return 15;

			default:
				return 0;
		}
	}
}

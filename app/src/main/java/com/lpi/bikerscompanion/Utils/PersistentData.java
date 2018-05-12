package com.lpi.bikerscompanion.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by lucien on 24/04/2018.
 */

public class PersistentData {
	private static final String PREF_MODE_TRAJET = "lpi.com.bikercompanion.persistentdata.MODE_TRAJET";
	private static final String PREF_AUTONOMIE_RESTANCE= "lpi.com.bikercompanion.persistentdata.AUTONOMIE_RESTANTE";
	private static final String PREF_DERNIERE_POSITION = "lpi.com.bikercompanion.persistentdata.DERNIERE_POSITION";
	private static final String PREF_DISTANCE_PARCOURUE = "lpi.com.bikercompanion.persistentdata.DISTANCE_PARCOURUE";
	private static final String PREF_DERNIERE_HEURE_PAUSE = "lpi.com.bikercompanion.persistentdata.DERNIERE_PAUSE";
	private static final String PREF_DERNIERE_HEURE_ALARME = "lpi.com.bikercompanion.persistentdata.DERNIERE_ALARME";
	private static final String PREF_ANNONCE_BATTERIE_50 = "lpi.com.bikercompanion.persistentdata.ANNONCE_BATTERIE_50";
	private static final String PREF_ANNONCE_BATTERIE_25 = "lpi.com.bikercompanion.persistentdata.ANNONCE_BATTERIE_25";
	private static final String PREF_ANNONCE_BATTERIE_10 = "lpi.com.bikercompanion.persistentdata.ANNONCE_BATTERIE_10";
	private static final String PREF_DATE_DERNIER_SMS = "lpi.com.bikercompanion.persistentdata.DATE_DERNIER_SMS";
	private static final String PREF_DERNIERE_DIZAINE = "lpi.com.bikercompanion.persistentdata.DERNIERE_DIZAINE";
	private static final String PREF_DERNIER_KILOMETRE = "lpi.com.bikercompanion.persistentdata.DERNIER_KILOMETRE";
	private static PersistentData INSTANCE = null;
	private Context _context;

	public static final int MODE_TRAJET_ARRET = 0;
	public static final int MODE_TRAJET_PAUSE = 1;
	public static final int MODE_TRAJET_ENROUTE = 2;

	public int modeTrajet = MODE_TRAJET_ARRET;
	public float autonomieRestanteM = 120000 ;
	public Location dernierePosition;
	public float distanceParcourueM;
	public long derniereHeurePause;
	public long derniereHeureAlarme;
	public boolean annonceBatterie50 = false ;
	public boolean annonceBatterie25 = false ;
	public boolean annonceBatterie10 = false ;
	public long dateDernierSMS = 0 ;
	public int derniereDizaine = 0 ;
	public int dernierKilometre = 0 ;


	/**
	 * Point d'accès pour l'instance unique du singleton
	 */
	public static synchronized PersistentData getInstance(Context context)
	{
		if (INSTANCE == null)
		{
			INSTANCE = new PersistentData(context);
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

		editor.putInt(PREF_MODE_TRAJET, modeTrajet);
		editor.putFloat(PREF_AUTONOMIE_RESTANCE, autonomieRestanteM);
		putLocation(editor, PREF_DERNIERE_POSITION, dernierePosition) ;
		editor.putFloat(PREF_DISTANCE_PARCOURUE, distanceParcourueM);
		editor.putLong(PREF_DERNIERE_HEURE_PAUSE, derniereHeurePause);
		editor.putLong(PREF_DERNIERE_HEURE_ALARME, derniereHeureAlarme);
		editor.putLong(PREF_DATE_DERNIER_SMS, dateDernierSMS);
		editor.putBoolean(PREF_ANNONCE_BATTERIE_50, annonceBatterie50);
		editor.putBoolean(PREF_ANNONCE_BATTERIE_25, annonceBatterie25);
		editor.putBoolean(PREF_ANNONCE_BATTERIE_10, annonceBatterie10);
		editor.putInt(PREF_DERNIERE_DIZAINE, derniereDizaine);
		editor.putInt(PREF_DERNIER_KILOMETRE, dernierKilometre);
		editor.apply();
	}

	private void putLocation(@NonNull  SharedPreferences.Editor editor, @NonNull String nom, @Nullable Location position)
	{
		if ( position!=null)
		{
			editor.putString(nom + ".Latitude", Double.toString(position.getLatitude()));
			editor.putString(nom + ".Longitude", Double.toString(position.getLongitude()));
			editor.putString(nom + ".Vitesse", Float.toString(position.getSpeed()));
		}
		else
		{
			editor.remove(nom + ".Latitude");
			editor.remove(nom + ".Longitude");
			editor.remove(nom + ".Vitesse");
		}
	}


	private PersistentData(Context context)
	{
		_context = context;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);

		modeTrajet = settings.getInt(PREF_MODE_TRAJET, modeTrajet) ;
		autonomieRestanteM = settings.getFloat(PREF_AUTONOMIE_RESTANCE, autonomieRestanteM);
		dernierePosition = fromPreferences(settings, PREF_DERNIERE_POSITION);
		distanceParcourueM = settings.getFloat(PREF_DISTANCE_PARCOURUE, distanceParcourueM);
		derniereHeurePause = settings.getLong(PREF_DERNIERE_HEURE_PAUSE, derniereHeurePause);
		derniereHeureAlarme= settings.getLong(PREF_DERNIERE_HEURE_ALARME, derniereHeureAlarme);
		dateDernierSMS= settings.getLong(PREF_DATE_DERNIER_SMS, dateDernierSMS);
		annonceBatterie50 = settings.getBoolean(PREF_ANNONCE_BATTERIE_50, annonceBatterie50);
		annonceBatterie25 = settings.getBoolean(PREF_ANNONCE_BATTERIE_25, annonceBatterie25);
		annonceBatterie10 = settings.getBoolean(PREF_ANNONCE_BATTERIE_10, annonceBatterie10);
		derniereDizaine = settings.getInt(PREF_DERNIERE_DIZAINE, derniereDizaine);
		dernierKilometre = settings.getInt(PREF_DERNIERE_DIZAINE, dernierKilometre);
	}

	private Location fromPreferences(@NonNull  SharedPreferences settings, @NonNull String nom)
	{
		Location loc = new Location("");
		try
		{
			if ( settings.contains(nom + ".Latitude") && settings.contains(nom + ".Longitude") &&settings.contains(nom + ".Vitesse"))
			{
				loc.setLatitude(Double.parseDouble(settings.getString(nom + ".Latitude", "0")));
				loc.setLongitude(Double.parseDouble(settings.getString(nom + ".Longitude", "0")));
				loc.setSpeed(Float.parseFloat(settings.getString(nom + ".Vitesse", "0")));
			}
			else
				return null;

		} catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return loc;
	}


	public String getTextModeTrajet()
	{
		switch (modeTrajet)
		{
			case MODE_TRAJET_ENROUTE: return  "Trajet en cours";
			case MODE_TRAJET_PAUSE: return "Trajet en pause";
			default: return "Trajet arrêté";
		}
	}
}

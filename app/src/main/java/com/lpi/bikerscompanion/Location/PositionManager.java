package com.lpi.bikerscompanion.Location;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.media.ToneGenerator;
import android.provider.Settings;

import com.lpi.bikerscompanion.MainActivity;
import com.lpi.bikerscompanion.TextToSpeech.TextToSpeechManager;
import com.lpi.bikerscompanion.Utils.PersistentData;
import com.lpi.bikerscompanion.Utils.Preferences;

/**
 * Created by lucien on 26/04/2018.
 */

public class PositionManager {
	private static PositionManager INSTANCE = null;
	private Context _context;

	private LocationManager _locationManager;
	private PendingIntent intent;
	private Location mLocation;

	BroadcastReceiver _receiver;

	/**
	 * Point d'accès pour l'instance unique du singleton
	 */
	public static synchronized PositionManager getInstance(Context context)
	{
		if (INSTANCE == null)
		{
			INSTANCE = new PositionManager(context);
		}
		return INSTANCE;
	}

	@Override
	public void finalize()
	{
		try
		{
			super.finalize();
			flush();
		} catch (Throwable throwable)
		{
			throwable.printStackTrace();
		}
	}

	public void flush()
	{
		_context.unregisterReceiver(_receiver);
	}

	private PositionManager(Context context)
	{
		_context = context;
	}

	public void startReceptionPositions()
	{
		Intent intent = new Intent(_context, PositionService.class);
		intent.setAction(PositionService.ACTION_START);
		_context.startService(intent);

	}

	public void stopPositionReception()
	{
		Intent intent = new Intent(_context, PositionService.class);
		intent.setAction(PositionService.ACTION_STOP);
		_context.startService(intent);
	}

	public void showSettingsDialog()
	{
		new AlertDialog.Builder(_context)
				.setTitle("Activer GPS")
				.setMessage("Activez le GPS dans les paramètres de votre appareil")
				.setNegativeButton("Annuler", null)
				.setPositiveButton("Paramètres", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						_context.startActivity(intent);
					}
				})
				.create().show();
	}
/*
	public Location getLocation()
	{

		_locationManager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);

		// Check if the tracking is enabled.
		boolean isGPSEnabled = _locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isNetworkEnabled = _locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!isGPSEnabled && !isNetworkEnabled)
		{
			// Prompt to get the settings enabled by the user.
			//showSettingsDialog();
		}
		else
		{
			// Either one is enabled
			// 10 * 60 * 1000 = 10 minutes
			// 1000 = 1 km
			// this = listener
			Preferences prefs = Preferences.getInstance(_context);

			if (isGPSEnabled)
			{
				// Get the location from GPS
				try
				{
					_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, prefs.delaiGPSMillisecondes, prefs.distanceGPSMetres, intent);

					if (mLocation == null)
					{
						mLocation = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					}
				} catch (SecurityException e)
				{
					showSettingsDialog();
				}
			}
			else
			{
				// Get the location from GPS
				try
				{
					// Set up to capture the location updates
					Intent smsIntent = new Intent(_context, LocationBroadcastReceiver.class);
					PendingIntent intent = PendingIntent.getService(_context, 0, smsIntent, 0);

					_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, prefs.delaiGPSMillisecondes, prefs.distanceGPSMetres, intent);

					if (mLocation == null)
					{
						mLocation = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					}
				} catch (SecurityException e)
				{
					showSettingsDialog();
				}
			}
		}

		return mLocation;
	}
	*/
}

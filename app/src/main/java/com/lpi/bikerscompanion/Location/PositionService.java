package com.lpi.bikerscompanion.Location;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.lpi.bikerscompanion.MainActivity;
import com.lpi.bikerscompanion.R;
import com.lpi.bikerscompanion.Utils.PersistentData;
import com.lpi.bikerscompanion.Utils.Preferences;

public class PositionService extends Service implements LocationListener {
	public static final String ACTION_NEW_LOCATION = "lpi.com.bikerscompanion.Location";
	public static final String PARAM_LOCATION = "location";
	private static final String TAG = "PositionService";
	public static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int NOTIFICATION_ID = 2146;
	public static final String ACTION_STOP = "com.lpi.bikerscompanion.stop";
	public static final String ACTION_START = "com.lpi.bikerscompanion.start";
	public static final String ACTION_PAUSE = "com.lpi.bikerscompanion.pause";
	public static final String ACTION_ARRET_PAUSE = "com.lpi.bikerscompanion.arretpause";
	private LocationManager _locationManager;
	private BatteryChangeReceiver _batteryReceiver;
	private SMSObserver _SMSObserver;

	public PositionService()
	{
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		// Lance la premiere fois
	}


	/**
	 * Called by the system every annonceHeure a client explicitly starts the service by calling
	 * {@link Context#startService}, providing the arguments it supplied and a
	 * unique integer token representing the start request.  Do not call this method directly.
	 * <p>
	 * <p>For backwards compatibility, the default implementation calls
	 * {@link #onStart} and returns either {@link #START_STICKY}
	 * or {@link #START_STICKY_COMPATIBILITY}.
	 * <p>
	 * <p>If you need your application to run on platform versions prior to API
	 * level 5, you can use the following model to handle the older {@link #onStart}
	 * callback in that case.  The <code>handleCommand</code> method is implemented by
	 * you as appropriate:
	 * <p>
	 * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/ForegroundService.java
	 * start_compatibility}
	 * <p>
	 * <p class="caution">Note that the system calls this on your
	 * service's main thread.  A service's main thread is the same
	 * thread where UI operations take place for Activities running in the
	 * same process.  You should always avoid stalling the main
	 * thread's event loop.  When doing long-running operations,
	 * network calls, or heavy disk I/O, you should kick off a new
	 * thread, or use {@link AsyncTask}.</p>
	 *
	 * @param intent  The Intent supplied to {@link Context#startService},
	 *                as given.  This may be null if the service is being restarted after
	 *                its process has gone away, and it had previously returned anything
	 *                except {@link #START_STICKY_COMPATIBILITY}.
	 * @param flags   Additional data about this start request.  Currently either
	 *                0, {@link #START_FLAG_REDELIVERY}, or {@link #START_FLAG_RETRY}.
	 * @param startId A unique integer representing this specific request to
	 *                start.  Use with {@link #stopSelfResult(int)}.
	 * @return The return value indicates what semantics the system should
	 * use for the service's current started state.  It may be one of the
	 * constants associated with the {@link #START_CONTINUATION_MASK} bits.
	 * @see #stopSelfResult(int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d(TAG, "onStartCommand");
		super.onStartCommand(intent, flags, startId);
		String action = intent.getAction();

		if (ACTION_STOP.equals(action))
			stopService();
		else if (ACTION_PAUSE.equals(action))
			pauseService();
		else if (ACTION_ARRET_PAUSE.equals(action))
			arretPauseService();
		else if (ACTION_START.equals(action))
			startService();

		return START_STICKY;
	}

	private void arretPauseService()
	{
	}

	private void pauseService()
	{

	}

	private void startService()
	{
		Preferences prefs = Preferences.getInstance(getApplicationContext());

		// Demarrage GPS
		if (prefs.alerteVitesseMax || prefs.alerteAutonomie)
		{
			if (_locationManager == null)
				_locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			_locationManager.removeUpdates(PositionService.this);
			Log.d(TAG, "requestLocationUpdates " + prefs.delaiGPSMillisecondes + "ms, " + prefs.distanceGPSMetres + "m");
			_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, prefs.delaiGPSMillisecondes, prefs.distanceGPSMetres, PositionService.this);
			_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, prefs.delaiGPSMillisecondes, prefs.distanceGPSMetres, PositionService.this);
		}

		// Heure
		if (prefs.annonceHeure != Preferences.ANNONCE_HEURE_JAMAIS || prefs.annoncePauses != Preferences.ANNONCE_PAUSE_JAMAIS)
		{
			// Lancer chrono
			HourAlarms.getInstance(getApplicationContext()).setNextAlarm();
		}


		// Batterie
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		_batteryReceiver = new BatteryChangeReceiver();
		registerReceiver(_batteryReceiver, intentFilter);

		// SMS
		if (prefs.litSMS != Preferences.ANNONCE_SMS_JAMAIS)
		{
			if (_SMSObserver == null)
			{
				// Lire les SMS
				Log.d(TAG, "InitSMS"); //$NON-NLS-1$
				_SMSObserver = new SMSObserver(getApplicationContext());
				getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, _SMSObserver); //$NON-NLS-1$
			}
		}

		// Service foreground en esperant qu'il ne soit pas tué trop vite
		startForeground(NOTIFICATION_ID, getMyActivityNotification(getApplicationContext(), "Démarrage du trajet"));
	}

	/***
	 * Arret du trajet
	 */
	private void stopService()
	{
		if (_locationManager != null)
			_locationManager.removeUpdates(PositionService.this);

		if ( _batteryReceiver !=null)
		{
			unregisterReceiver(_batteryReceiver);
			_batteryReceiver = null;
		}

		if ( _SMSObserver !=null)
		{
			getContentResolver().unregisterContentObserver(_SMSObserver);
			_SMSObserver = null;
		}
		// Arreter ce service
		stopSelf();
	}

	/**
	 * Called when the location has changed.
	 * <p>
	 * <p> There are no restrictions on the use of the supplied Location object.
	 *
	 * @param location The new location, as a Location object.
	 */
	@Override
	public void onLocationChanged(Location location)
	{
		Log.d(TAG, "onPositionChanged " + location.toString());
		Context context = getApplicationContext();
		//TextToSpeechManager.getInstance(context).playTone(ToneGenerator.TONE_DTMF_3);
		PersistentData data = PersistentData.getInstance(context);
		if (data.modeTrajet == PersistentData.MODE_TRAJET_ENROUTE)
		{
			// Envoyer un intent pour la nouvelle position
			Intent intent = new Intent();
			intent.setAction(ACTION_NEW_LOCATION);
			intent.putExtra(PARAM_LOCATION, location);
			getApplicationContext().sendBroadcast(intent);

			// Watchdog, pour eviter que ce service ne soit ferme
			WatchdogService.schedule(context);
		}
		else
		{
			if (_locationManager != null)
			{
				_locationManager.removeUpdates(this);
				_locationManager = null;
			}
		}
	}

	/**
	 * Called when the provider status changes. This method is called when
	 * a provider is unable to fetch a location or if the provider has recently
	 * become available after a period of unavailability.
	 *
	 * @param provider the name of the location provider associated with this
	 *                 update.
	 * @param status   OUT_OF_SERVICE if the
	 *                 provider is out of service, and this is not expected to change in the
	 *                 near future; TEMPORARILY_UNAVAILABLE if
	 *                 the provider is temporarily unavailable but is expected to be available
	 *                 shortly; and LocationProvider#AVAILABLE if the
	 *                 provider is currently available.
	 * @param extras   an optional Bundle which will contain provider specific
	 *                 status variables.
	 *                 <p>
	 *                 <p> A number of common key/value pairs for the extras Bundle are listed
	 *                 below. Providers that use any of the keys on this list must
	 *                 provide the corresponding value as described below.
	 *                 <p>
	 *                 <ul>
	 *                 <li> satellites - the number of satellites used to derive the fix
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
//		Log.d(TAG, "onStatusChanged " + provider + " " + (status == LocationProvider.OUT_OF_SERVICE ? "OUT_OF_SERVICE" : status == LocationProvider.TEMPORARILY_UNAVAILABLE ? "TEMPORARILY_UNAVAILABLE" : status == LocationProvider.AVAILABLE ? "AVAILABLE" : "status ???"));
	}

	/**
	 * Called when the provider is enabled by the user.
	 *
	 * @param provider the name of the location provider associated with this
	 *                 update.
	 */
	@Override
	public void onProviderEnabled(String provider)
	{
//		Log.d(TAG, "onProviderEnabled " + provider);
	}

	/**
	 * Called when the provider is disabled by the user. If requestLocationUpdates
	 * is called on an already disabled provider, this method is called
	 * immediately.
	 *
	 * @param provider the name of the location provider associated with this
	 *                 update.
	 */
	@Override
	public void onProviderDisabled(String provider)
	{
//		Log.d(TAG, "onProviderDisabled " + provider);
	}

	/**
	 * Called by the system to notify a Service that it is no longer used and is being removed.  The
	 * service should clean up any resources it holds (threads, registered
	 * receivers, etc) at this point.  Upon return, there will be no more calls
	 * in to this Service object and it is effectively dead.  Do not call this method directly.
	 */
	@Override
	public void onDestroy()
	{
		//TextToSpeechManager.getInstance(getApplicationContext()).playTone(ToneGenerator.TONE_DTMF_0);

		Log.d(TAG, "OnDestroy");
		super.onDestroy();
	}

	private static Notification getMyActivityNotification(Context context, String text)
	{
		// The PendingIntent to launch our activity if the user selects
		// this notification
		CharSequence title = context.getText(R.string.app_name);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

		return new NotificationCompat.Builder(context)
				       .setOngoing(true)
				       .setCategory(Notification.CATEGORY_SERVICE)
				       .setSmallIcon(R.drawable.ic_stat_notification)
				       .setTicker(title)
				       .setContentText(text)
				       .setSubText(PersistentData.getInstance(context).getTextModeTrajet())
				       .setVisibility(Notification.VISIBILITY_PUBLIC)
				       .setContentIntent(contentIntent)
				       .build();
	}


	/**
	 * This is the method that can be called to update the Notification
	 */
	public static void updateNotification(Context context, String text)
	{
		Notification notification = getMyActivityNotification(context, text);

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
}

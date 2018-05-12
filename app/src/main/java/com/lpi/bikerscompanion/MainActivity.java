package com.lpi.bikerscompanion;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lpi.bikerscompanion.Location.LocationBroadcastReceiver;
import com.lpi.bikerscompanion.Location.PositionManager;
import com.lpi.bikerscompanion.TextToSpeech.TextToSpeechListener;
import com.lpi.bikerscompanion.TextToSpeech.TextToSpeechManager;
import com.lpi.bikerscompanion.Utils.PersistentData;
import com.lpi.bikerscompanion.Utils.Preferences;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TextToSpeechListener {

	public static final int CATEGORIE_DEMARRAGE = 0;
	public static final int CATEGORIE_ARRET = 1;
	public static final int CATEGORIE_PAUSE = 2;
	public static final int CATEGORIE_DEMIRESERVOIR = 3;
	public static final int CATEGORIE_QUARTRESERVOIR = 4;
	public static final int CATEGORIE_VITESSE = 5;
	public static final int CATEGORIE_PLEIN_ESSENCE = 6;
	public static final int CATEGORIE_ANNONCE_BATTERIE_50 = 7;
	public static final int CATEGORIE_ANNONCE_BATTERIE_25 = 8;
	public static final int CATEGORIE_ANNONCE_BATTERIE_10 = 9;
	public static final int CATEGORIE_DIZAINE = 10;
	public static final int CATEGORIE_TEST = 11;

	private static final int REQUEST_PERMISSIONS = 1225;
	Button _boutonDemarrer, _boutonPause, _boutonStop;
	ProgressBar _progressAutonomie;
	TextView _tvAutonomie;
	TextToSpeechManager _ttsManager;
	BroadcastReceiver _receiver ;
	private IntentFilter _filter;

	/**
	 * Dispatch incoming result to the correct fragment.
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		ThemeActivity.setTheme(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		ThemeActivity.setTheme(this);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

//		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//		fab.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view)
//			{
//				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//				        .setAction("Action", null).show();
//			}
//		});

		/*
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				                                                        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
*/

		_receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent)
			{
				String action = intent.getAction();
				if (LocationBroadcastReceiver.ACTION_KILOMETRE.equals(action))
					onNouveauKilometre(intent);
			}
		};
		_filter = new IntentFilter();
		_filter.addAction(LocationBroadcastReceiver.ACTION_KILOMETRE);

		_boutonDemarrer = (Button) findViewById(R.id.buttonDemarrer);
		_boutonPause = (Button) findViewById(R.id.buttonPause);
		_boutonStop = (Button) findViewById(R.id.buttonStop);
		_progressAutonomie = (ProgressBar) findViewById(R.id.progressBarAutonomie);
		_tvAutonomie = (TextView) findViewById(R.id.textViewAutonomie);

		_ttsManager = TextToSpeechManager.getInstance(this);
		if (!_ttsManager.initialise())
		{

			_boutonDemarrer.setEnabled(false);
			_boutonPause.setEnabled(false);
			_boutonStop.setEnabled(false);
		}

		if (Build.VERSION.SDK_INT >= 23 &&
				    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
				    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
				    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				    ContextCompat.checkSelfPermission(this, "android.permission.BIND_JOB_SERVICE") != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this,
					new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, "android.permission.BIND_JOB_SERVICE", Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS},
					REQUEST_PERMISSIONS);
		}
	}

	private void onNouveauKilometre(Intent intent)
	{
		Preferences prefs = Preferences.getInstance(this);
		PersistentData data = PersistentData.getInstance(this);
		_progressAutonomie.setMax(prefs.autonomieMaxMetres / 1000);
		_progressAutonomie.setProgress((int) (data.autonomieRestanteM / 1000));

	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       String permissions[], int[] grantResults)
	{
		boolean initialise = false;
		switch (requestCode)
		{
			case REQUEST_PERMISSIONS:
			{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					initialise = _ttsManager.initialise();

				}
				else
				{
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
			}

			// other 'case' lines to check for other
			// permissions this app might request.
		}
		_boutonDemarrer.setEnabled(initialise);
		_boutonPause.setEnabled(initialise);
		_boutonStop.setEnabled(initialise);
	}

	/**
	 * Dispatch onResume() to fragments.  Note that for better inter-operation
	 * with older versions of the platform, at the point of this call the
	 * fragments attached to the activity are <em>not</em> resumed.  This means
	 * that in some cases the previous state may still be saved, not allowing
	 * fragment transactions that modify the state.  To correctly interact
	 * with fragments in their proper state, you should instead override
	 * {@link #onResumeFragments()}.
	 */
	@Override
	protected void onResume()
	{
		ThemeActivity.setTheme(this);

		super.onResume();
		PersistentData data = PersistentData.getInstance(this);
		Preferences prefs = Preferences.getInstance(this);

		switch (data.modeTrajet)
		{
			case PersistentData.MODE_TRAJET_ARRET:
				// Montrer "Demarrer"
				_boutonDemarrer.setVisibility(View.VISIBLE);
				_boutonPause.setVisibility(View.GONE);
				_boutonStop.setVisibility(View.GONE);
				break;

			case PersistentData.MODE_TRAJET_ENROUTE:
				// Montrer "Pause" et "stop"
				_boutonDemarrer.setVisibility(View.GONE);
				_boutonPause.setVisibility(View.VISIBLE);
				_boutonStop.setVisibility(View.VISIBLE);
				break;

			case PersistentData.MODE_TRAJET_PAUSE:
				// Montrer "Pause" et "stop"
				_boutonDemarrer.setVisibility(View.GONE);
				_boutonPause.setVisibility(View.VISIBLE);
				_boutonStop.setVisibility(View.VISIBLE);
				break;
		}

		_ttsManager = TextToSpeechManager.getInstance(this);
		_ttsManager.addListener(this);


		_progressAutonomie.setMax(prefs.autonomieMaxMetres / 1000);
		_progressAutonomie.setProgress((int) (data.autonomieRestanteM / 1000));

		_tvAutonomie.setText(String.format(getResources().getString(R.string.texte_autonomie), (int) (data.autonomieRestanteM / 1000), (int) (prefs.autonomieMaxMetres / 1000)));

		registerReceiver(_receiver, _filter);
	}

	/**
	 * Dispatch onPause() to fragments.
	 */
	@Override
	protected void onPause()
	{
		super.onPause();

		_ttsManager = TextToSpeechManager.getInstance(this);
		_ttsManager.removeListener(this);
		unregisterReceiver(_receiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id)
		{
			case R.id.action_settings:
			{
				Intent intent = new Intent(this, PreferencesActivity.class);
				startActivity(intent);
			}
			break;

			case R.id.action_audio:
			{
				Intent intent = new Intent();
				intent.setAction(android.provider.Settings.ACTION_SOUND_SETTINGS);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			break;

			case R.id.action_speech:
			{
				//Open Android Text-To-Speech Settings
				if (Build.VERSION.SDK_INT >= 14)
				{
					Intent intent = new Intent();
					intent.setAction("com.android.settings.TTS_SETTINGS");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
				else
				{
					Intent intent = new Intent();
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TextToSpeechSettings"));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			}
			break;

			case R.id.action_theme:
			{
				Intent intent = new Intent(this, ThemeActivity.class);
				startActivity(intent);
			}
			break;

			case R.id.action_gps:
			{
				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
			break;

			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}


	/***
	 * Demarrer le trajet
	 * @param v
	 */
	public void onClicDemarrer(View v)
	{
		PersistentData data = PersistentData.getInstance(this);

		switch (data.modeTrajet)
		{
			case PersistentData.MODE_TRAJET_ARRET:
				demarrerTrajet();
				break;

			case PersistentData.MODE_TRAJET_PAUSE:
				arreterPause();
				break;
		}
	}

	public void onClicPause(View v)
	{
		PersistentData data = PersistentData.getInstance(this);
		switch (data.modeTrajet)
		{
			case PersistentData.MODE_TRAJET_ARRET:
				// Deja arrete, rien a faire
				break;

			case PersistentData.MODE_TRAJET_ENROUTE:
				// Mettre en pause
				pause();
				break;

			case PersistentData.MODE_TRAJET_PAUSE:
				arreterPause();
				break;
		}

	}

	/***
	 * Mise en pause du trajet
	 */
	private void pause()
	{
		PersistentData data = PersistentData.getInstance(this);

		data.modeTrajet = PersistentData.MODE_TRAJET_PAUSE;
		_ttsManager.annonce("Pause", TextToSpeechManager.REPETITION_TOUJOURS, 0, CATEGORIE_PAUSE);

		// Arreter reception des positions
		PositionManager posM = PositionManager.getInstance(this);
		posM.stopPositionReception();
		data.derniereHeurePause = Calendar.getInstance().getTimeInMillis();

		data.flush();
	}

	private void arreterPause()
	{
		PersistentData data = PersistentData.getInstance(this);

		data.modeTrajet = PersistentData.MODE_TRAJET_ENROUTE;
		_ttsManager.reinitAnnonces();
		_ttsManager.annonce("Fin de la pause", TextToSpeechManager.REPETITION_TOUJOURS, 0, CATEGORIE_PAUSE);
		data.derniereHeurePause = Calendar.getInstance().getTimeInMillis();

		PositionManager posM = PositionManager.getInstance(this);
		posM.startReceptionPositions();
	}

	private void demarrerTrajet()
	{
		Animation animExit = AnimationUtils.loadAnimation(this, R.anim.exit);
		Animation animEnter = AnimationUtils.loadAnimation(this, R.anim.enter);
		// Cacher "Demarrer"
		_boutonDemarrer.setAnimation(animExit);
		_boutonDemarrer.setVisibility(View.GONE);

		// Montrer "Pause"
		_boutonPause.setAnimation(animEnter);
		_boutonPause.setVisibility(View.VISIBLE);

		// Montrer "Stop"
		_boutonStop.setAnimation(animEnter);
		_boutonStop.setVisibility(View.VISIBLE);

		PersistentData data = PersistentData.getInstance(this);
		data.modeTrajet = PersistentData.MODE_TRAJET_ENROUTE;
		data.distanceParcourueM = 0;
		data.dernierePosition = null;
		data.derniereHeurePause = Calendar.getInstance().getTimeInMillis();
		data.annonceBatterie50 = false;
		data.annonceBatterie25 = false;
		data.annonceBatterie10 = false;
		data.derniereDizaine = 0;
		data.dernierKilometre = 0;

		Preferences prefs = Preferences.getInstance(this);
		_ttsManager.reinitAnnonces();
		_ttsManager.annonce(String.format(getResources().getString(R.string.demarrage), (int) (data.autonomieRestanteM / 1000), (int) (prefs.autonomieMaxMetres / 1000)), TextToSpeechManager.REPETITION_JAMAIS, 0, CATEGORIE_DEMARRAGE);

		// Demarrer service de reception des positions
		PositionManager posM = PositionManager.getInstance(this);
		posM.startReceptionPositions();

		data.flush();
	}


	public void onClicStop(View v)
	{
		PersistentData data = PersistentData.getInstance(this);

		switch (data.modeTrajet)
		{
			case PersistentData.MODE_TRAJET_ENROUTE:
			case PersistentData.MODE_TRAJET_PAUSE:
			{
				Animation animExit = AnimationUtils.loadAnimation(this, R.anim.exit);
				Animation animEnter = AnimationUtils.loadAnimation(this, R.anim.enter);

				// Montrer "Demarrer"
				_boutonDemarrer.setAnimation(animEnter);
				_boutonDemarrer.setVisibility(View.VISIBLE);

				// Cacher "Pause"
				_boutonPause.setAnimation(animExit);
				_boutonPause.setVisibility(View.GONE);

				// Cacher "Stop"
				_boutonStop.setAnimation(animExit);
				_boutonStop.setVisibility(View.GONE);
				data.modeTrajet = PersistentData.MODE_TRAJET_ARRET;
				_ttsManager.annonce(getResources().getString(R.string.arret_du_trajet), TextToSpeechManager.REPETITION_JAMAIS, 0, CATEGORIE_ARRET);

				PositionManager posM = PositionManager.getInstance(this);
				posM.stopPositionReception();
				data.flush();
			}

			break;
			case PersistentData.MODE_TRAJET_ARRET:
				//DemarrerTrajet();
				break;

		}
	}

	public void onClickPlein(View v)
	{
		PersistentData data = PersistentData.getInstance(this);
		Preferences prefs = Preferences.getInstance(this);

		data.autonomieRestanteM = prefs.autonomieMaxMetres;

		int autonomie = (int) (data.autonomieRestanteM / 1000);
		_ttsManager.annonce(String.format("Plein d'essence fait, autonomie %1$d km", autonomie), TextToSpeechManager.REPETITION_TOUJOURS, 0, CATEGORIE_PLEIN_ESSENCE);
		_ttsManager.reinitAnnonces(CATEGORIE_DEMIRESERVOIR);
		_ttsManager.reinitAnnonces(CATEGORIE_QUARTRESERVOIR);
		_progressAutonomie.setProgress(autonomie);

		_tvAutonomie.setText(String.format(getResources().getString(R.string.texte_autonomie), autonomie, (int) (prefs.autonomieMaxMetres / 1000)));
		data.flush();
	}

	/***
	 * Le manager de synthese vocale est initialise
	 * @param ttsResult
	 */
	@Override
	public void onTTSInit(int ttsResult)
	{
		if (ttsResult == TextToSpeech.SUCCESS)
		{
//			_ttsManager.annonce("Synthèse vocale initialisée", TextToSpeechManager.REPETITION_JAMAIS, 0, CATEGORIE_INIT);
			_boutonDemarrer.setEnabled(true);
			_boutonPause.setEnabled(true);
			_boutonStop.setEnabled(true);
		}
		else
		{
			messageBox("Erreur", "Impossible d'initialiser la synthèse vocale");
		}
	}

	/***
	 * Affiche une messagebox modale
	 * @param s
	 */
	private void messageBox(String titre, String s)
	{
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setMessage(s);
		dlgAlert.setTitle(titre);
		dlgAlert.setPositiveButton("OK", null);
		dlgAlert.create().show();
	}


}

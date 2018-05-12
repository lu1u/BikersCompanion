package com.lpi.bikerscompanion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.lpi.bikerscompanion.TextToSpeech.TextToSpeechManager;
import com.lpi.bikerscompanion.Utils.Preferences;

public class PreferencesActivity extends AppCompatActivity {
	Switch _switchAlerteVitesseMax, _switchAutonomie, _switchDemiReservoir, _switchQuartReservoir;

	EditText _editVitesseMax;
	private EditText _editAutonomie;
	RadioGroup _rgPauses;
	RadioGroup _rgHeures;
	RadioGroup _rgSortie;
	RadioGroup _rgSMS;
	RadioGroup _rgSortieAudio;

	/**
	 * Take care of popping the fragment back stack or finishing the activity
	 * as appropriate.
	 */
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		Preferences prefs = Preferences.getInstance(this);
		_switchAlerteVitesseMax.setChecked(prefs.alerteVitesseMax);
		prefs.vitesseMaxKmH = Integer.parseInt(_editVitesseMax.getText().toString());

		// Autonomie
		_switchAutonomie.setChecked(prefs.alerteAutonomie);
		prefs.autonomieMaxMetres = Integer.parseInt(_editAutonomie.getText().toString()) * 1000;
		prefs.flush();
	}

	/**
	 * react to the user tapping the back/up icon in the action bar
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				// this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
				// if this doesn't work as desired, another possibility is to call `finish()` here.
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeActivity.setTheme(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		_switchAlerteVitesseMax = (Switch) findViewById(R.id.switchAlarmeVitesse);
		_switchAutonomie = (Switch) findViewById(R.id.switchAutonomie);

		_switchDemiReservoir = (Switch) findViewById(R.id.switchDemiReservoir);
		_switchQuartReservoir = (Switch) findViewById(R.id.switchQuartReservoir);

		_editVitesseMax = (EditText) findViewById(R.id.editTextVitesseMax);
		_editAutonomie = (EditText) findViewById(R.id.editTextAutonomie);

		_rgHeures = (RadioGroup) findViewById(R.id.radiogroupHeures);
		_rgPauses = (RadioGroup) findViewById(R.id.radiogroupPauses);
		_rgSortie = (RadioGroup) findViewById(R.id.radiogroupSortieAudio);
		_rgSMS = (RadioGroup) findViewById(R.id.radiogroupeSMS);
		_rgSortieAudio = (RadioGroup)findViewById(R.id.radiogroupSortieAudio);

		_switchAlerteVitesseMax.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				showLayouts(isChecked, R.id.idLayoutVitesse);
			}
		});
		_switchAutonomie.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				showLayouts(isChecked, R.id.idLayoutAutonomie);
			}
		});
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
		super.onResume();
		initUI();
	}


	/***
	 * Initialisation de l'interface utilisateur
	 */
	private void initUI()
	{
		Preferences prefs = Preferences.getInstance(this);

		// Vitesse max
		_switchAlerteVitesseMax.setChecked(prefs.alerteVitesseMax);
		_editVitesseMax.setText(Integer.toString(prefs.vitesseMaxKmH));
		showLayouts(prefs.alerteVitesseMax, R.id.idLayoutVitesse);

		// Autonomie
		_switchAutonomie.setChecked(prefs.alerteAutonomie);
		_editAutonomie.setText(Integer.toString(prefs.autonomieMaxMetres / 1000));
		_switchDemiReservoir.setChecked(prefs.alerteDemiReservoir);
		_switchQuartReservoir.setChecked(prefs.alerteQuartReservoir);
		showLayouts(prefs.alerteAutonomie, R.id.idLayoutAutonomie);

		// Pauses
		initPausesUI();

		// Heure
		initHeureUI();

		// SMS
		initSMSUI();
		// Sortie audio
		initSortieAudioUI();
	}

	/***
	 * Initialisation du radiogroup "Sortie audio" et de son listener
	 */
	private void initSortieAudioUI()
	{
		final Preferences prefs = Preferences.getInstance(this);

		switch (prefs.sortieAudio)
		{
			case Preferences.SORTIE_AUDIO_DEFAUT:
				_rgSortieAudio.check(R.id.radioButtonAudioDefaut);
				break;
			case Preferences.SORTIE_AUDIO_HAUTPARLEUR:
				_rgSortieAudio.check(R.id.radioButtonAudioHautParleur);
				break;
			case Preferences.SORTIE_AUDIO_BLUETOOTH:
				_rgSortieAudio.check(R.id.radioButtonAudioBluetooth);
		}
		_rgSortieAudio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				switch (checkedId)
				{
					case R.id.radioButtonAudioDefaut:
						prefs.sortieAudio = Preferences.SORTIE_AUDIO_DEFAUT;
						break;
					case R.id.radioButtonAudioHautParleur:
						prefs.sortieAudio = Preferences.SORTIE_AUDIO_HAUTPARLEUR;
						break;
					case R.id.radioButtonAudioBluetooth:
						prefs.sortieAudio = Preferences.SORTIE_AUDIO_BLUETOOTH;
						break;
				}
				prefs.flush();
			}
		});
	}

	/***
	 * Initialisation du radiogroup "Pauses" et de son listener
	 */
	private void initPausesUI()
	{
		final Preferences prefs = Preferences.getInstance(this);

		switch (prefs.annoncePauses)
		{
			case Preferences.ANNONCE_PAUSE_JAMAIS:
				_rgPauses.check(R.id.radioButtonPauseJamais);
				break;
			case Preferences.ANNONCE_PAUSE_DEUX_HEURES:
				_rgPauses.check(R.id.radioButtonPauseDeuxHeures);
				break;
			case Preferences.ANNONCE_PAUSE_HEURE:
				_rgPauses.check(R.id.radioButtonPauseHeures);
		}
		_rgPauses.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				switch (checkedId)
				{
					case R.id.radioButtonPauseJamais:
						prefs.annoncePauses = Preferences.ANNONCE_PAUSE_JAMAIS;
						break;
					case R.id.radioButtonPauseDeuxHeures:
						prefs.annoncePauses = Preferences.ANNONCE_PAUSE_DEUX_HEURES;
						break;
					case R.id.radioButtonPauseHeures:
						prefs.annoncePauses = Preferences.ANNONCE_PAUSE_HEURE;
						break;
				}
				prefs.flush();
			}
		});
	}

	/***
	 * Initialisation du radiogroup "Lecture SMS" et de son listener
	 */
	private void initSMSUI()
	{
		final Preferences prefs = Preferences.getInstance(this);

		switch (prefs.litSMS)
		{
			case Preferences.ANNONCE_SMS_JAMAIS:
				_rgSMS.check(R.id.radioButtonSMSJamais);
				break;
			case Preferences.ANNONCE_SMS_CONTACTS:
				_rgSMS.check(R.id.radioButtonSMSContacts);
				break;
			case Preferences.ANNONCE_SMS_TOUS:
				_rgSMS.check(R.id.radioButtonSMSToujours);
		}
		_rgSMS.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				switch (checkedId)
				{
					case R.id.radioButtonSMSJamais:
						prefs.litSMS = Preferences.ANNONCE_SMS_JAMAIS;
						break;
					case R.id.radioButtonSMSContacts:
						prefs.litSMS = Preferences.ANNONCE_SMS_CONTACTS;
						break;
					case R.id.radioButtonSMSToujours:
						prefs.litSMS = Preferences.ANNONCE_SMS_TOUS;
						break;
				}
				prefs.flush();
			}
		});
	}

	/***
	 * Initialisation du radiogroup "Annonce de l'heure" et de son listener
	 */
	private void initHeureUI()
	{
		final Preferences prefs = Preferences.getInstance(this);

		switch (prefs.annonceHeure)
		{
			case Preferences.ANNONCE_HEURE_JAMAIS:
				_rgHeures.check(R.id.radioButtonHeureJamais);
				break;
			case Preferences.ANNONCE_HEURE_HEURES:
				_rgHeures.check(R.id.radioButtonHeureHeure);
				break;
			case Preferences.ANNONCE_HEURE_DEMI:
				_rgHeures.check(R.id.radioButtonHeureDemi);
				break;
			case Preferences.ANNONCE_HEURE_QUART:
				_rgHeures.check(R.id.radioButtonHeureQuart);
				break;
		}
		_rgHeures.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				switch (checkedId)
				{
					case R.id.radioButtonHeureJamais:
						prefs.annonceHeure = Preferences.ANNONCE_HEURE_JAMAIS;
						break;
					case R.id.radioButtonHeureHeure:
						prefs.annonceHeure = Preferences.ANNONCE_HEURE_HEURES;
						break;
					case R.id.radioButtonHeureDemi:
						prefs.annonceHeure = Preferences.ANNONCE_HEURE_DEMI;
						break;
					case R.id.radioButtonHeureQuart:
						prefs.annonceHeure = Preferences.ANNONCE_HEURE_QUART;
						break;
				}
				prefs.flush();
			}
		});

	}


	private void showLayouts(boolean montrer, int resId)
	{
		View v = findViewById(resId);

		if (montrer)
		{
			// Montrer layoutLOginResume
			Animation anim = AnimationUtils.loadAnimation(this, R.anim.enter_top);
			v.setAnimation(anim);
			v.setVisibility(View.VISIBLE);
		}
		else
		{
			// Cacher layoutLoginResume
			Animation anim = AnimationUtils.loadAnimation(this, R.anim.exit_top);
			v.setAnimation(anim);
			v.setVisibility(View.GONE);
		}
	}

	/***
	 * Clic sur le bouton "test audio"
	 */
	public void onClickTestAudio(View v)
	{
		TextToSpeechManager.getInstance(this).annonce("Test de la sortie audio", TextToSpeechManager.REPETITION_TOUJOURS, 0, MainActivity.CATEGORIE_TEST);
	}
}

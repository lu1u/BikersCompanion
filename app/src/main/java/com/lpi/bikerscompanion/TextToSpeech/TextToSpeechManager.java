package com.lpi.bikerscompanion.TextToSpeech;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import com.lpi.bikerscompanion.R;
import com.lpi.bikerscompanion.Utils.Preferences;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lucien on 24/04/2018.
 */

public class TextToSpeechManager extends UtteranceProgressListener implements TextToSpeech.OnInitListener {
	private static TextToSpeechManager INSTANCE = null;
	private static AudioManager _audio;
	private Context _context;

	public static final int REPETITION_TOUJOURS = 0;       // toujours repeter l'annonce
	public static final int REPETITION_JAMAIS = 1;         // ne jamais repeter cette annonce si elle a ete dite depuis le debut du trajet
	public static final int REPETITION_DELAI = 2;          // ne repeter cette annonce que si aucune annonce de la meme categorie a ete dite depuis un certain delai

	private TextToSpeech _Tts;
	private boolean _ttsInitialise = false;
	private ArrayList<String> _pendingMessages;

	private HashMap<Integer, Long> _categories;
	private static ArrayList<TextToSpeechListener> _listeners = new ArrayList<>();
	//	ToneGenerator _toneGenerator;
	MediaPlayer _mediaPlayer;

	/**
	 * Point d'accès pour l'instance unique du singleton
	 */
	public static synchronized TextToSpeechManager getInstance(Context context)
	{
		if (INSTANCE == null)
		{
			INSTANCE = new TextToSpeechManager(context);
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
		_mediaPlayer.release();
	}

	private TextToSpeechManager(Context context)
	{
		_context = context;
		_ttsInitialise = false;
		_pendingMessages = new ArrayList<>();
		_categories = new HashMap<>();
		_Tts = new TextToSpeech(context, this);
		_audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//		_toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME);
		_mediaPlayer = MediaPlayer.create(context, R.raw.chime);
	}

	/***
	 *
	 * @param message
	 * @param typeRepetition
	 * @param delaiSecondes
	 * @param categorie
	 */
	public void annonce(final String message, int typeRepetition, int delaiSecondes, int categorie)
	{
		if (!_ttsInitialise)
			return;

		long maintenant = System.currentTimeMillis() / 1000;
		switch (typeRepetition)
		{
			case REPETITION_TOUJOURS:
				break;

			case REPETITION_DELAI:
			{
				// Verifier que la derniere annonce de meme categorie a ete faite il y a suffisament longtemps
				Long value = _categories.get(categorie);
				if (value != null)
				{
					if ((maintenant - value.longValue()) < delaiSecondes)
						return;
				}
			}
			break;


			case REPETITION_JAMAIS:
			{
				// Verifier qu'aucune annonce de cette categorie a ete faite
				Long value = _categories.get(categorie);
				if (value != null)
					return;
			}
			break;
		}

		// Derniere fois qu'on a dit une annonce de cette categorie
		_categories.put(categorie, maintenant);

		// Ajouter l'annonce a la queue
		//_pendingMessages.add(message);
		Toast.makeText(_context, message, Toast.LENGTH_LONG);

		switch (Preferences.getInstance(_context).sortieAudio)
		{


			case Preferences.SORTIE_AUDIO_HAUTPARLEUR:
			{
				boolean speakerOn = _audio.isSpeakerphoneOn();

				if (!speakerOn)
					_audio.setSpeakerphoneOn(true);
				parleApresLeBip(message);
				if (!speakerOn)
					_audio.setSpeakerphoneOn(false);
			}
			break;

			case Preferences.SORTIE_AUDIO_BLUETOOTH:
			{
				boolean bluetooth = _audio.isBluetoothScoOn() ;
				if (!bluetooth)
					_audio.setBluetoothScoOn(true);
				parleApresLeBip(message);
				if (!bluetooth)
					_audio.setBluetoothScoOn(false);

			}
			break;

			default:
				parleApresLeBip(message);
				break;
		}





	}

	/***
	 * Emet un "bip" puis fait l'annonce vocale apres le bip
	 * @param message
	 */
	private void parleApresLeBip(final String message)
	{
		_mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			                                     public void onCompletion(MediaPlayer mp)
			                                     {
				                                     _Tts.speak(message, TextToSpeech.QUEUE_ADD, null, null);
			                                     }
		                                     }
		);
		_mediaPlayer.seekTo(0);
		_mediaPlayer.start();
	}

	/***
	 * Reinitialiser les restriction de repetition d'annonces
	 */

	public void reinitAnnonces()
	{
		_categories.clear();
	}

	/***
	 * Reinitialiser les restriction de repetition d'annonces
	 */
	public void reinitAnnonces(int categorie)
	{
		_categories.remove(categorie);
	}

	/**
	 * Called to signal the completion of the TextToSpeech engine initialization.
	 *
	 * @param status {@link TextToSpeech#SUCCESS} or {@link TextToSpeech#ERROR}.
	 */
	@Override
	public void onInit(int status)
	{
		switch (status)
		{
			case TextToSpeech.ERROR:
				break;

			case TextToSpeech.SUCCESS:
				_ttsInitialise = true;
				_Tts.setOnUtteranceProgressListener(this);
				speakPendingMessages();
				break;
		}

		for (TextToSpeechListener listener : _listeners)
			listener.onTTSInit(status);
	}

	/***
	 * Dit le premier des messages en attente, s'il existe
	 */
	private void speakPendingMessages()
	{
		if (_pendingMessages == null)
			return;

		if (_pendingMessages.size() == 0)
			return;

		if (!_ttsInitialise)
			return;

		_Tts.speak(_pendingMessages.get(0), TextToSpeech.QUEUE_ADD, null, null);
	}

	/**
	 * Called when an utterance "starts" as perceived by the caller. This will
	 * be soon before audio is played back in the case of a {@link TextToSpeech#speak}
	 * or before the first bytes of a file are written to the file system in the case
	 * of {@link TextToSpeech#synthesizeToFile}.
	 *
	 * @param utteranceId The utterance ID of the utterance.
	 */
	@Override
	public void onStart(String utteranceId)
	{
	}

	/**
	 * Called when an utterance has successfully completed processing.
	 * All audio will have been played back by this point for audible output, and all
	 * output will have been written to disk for file synthesis requests.
	 * <p>
	 * This request is guaranteed to be called after {@link #onStart(String)}.
	 *
	 * @param utteranceId The utterance ID of the utterance.
	 */
	@Override
	public void onDone(String utteranceId)
	{
		if (_pendingMessages != null)
			if (_pendingMessages.size() > 0)
			{
				_pendingMessages.remove(0);

				speakPendingMessages();
			}
	}

	/**
	 * Called when an error has occurred during processing. This can be called
	 * at any point in the synthesis process. Note that there might be calls
	 * to {@link #onStart(String)} for specified utteranceId but there will never
	 * be a call to both {@link #onDone(String)} and {@link #onError(String)} for
	 * the same utterance.
	 *
	 * @param utteranceId The utterance ID of the utterance.
	 * @deprecated Use {@link #onError(String, int)} instead
	 */
	@Override
	public void onError(String utteranceId)
	{

	}

	public void addListener(TextToSpeechListener listener)
	{
		for (TextToSpeechListener l : _listeners)
			if (listener == l)
				// Deja present dans la liste
				return;

		_listeners.add(listener);
	}

	public void removeListener(TextToSpeechListener listener)
	{
		_listeners.remove(listener);
	}

	/***
	 * retourne vrai si ce manager est initialise
	 * @return
	 */
	public boolean initialise()
	{
		return _ttsInitialise;
	}

//	public void playTone(int tone)
//	{
//		if (_toneGenerator != null)
//			_toneGenerator.startTone(tone, 50);
//	}
}

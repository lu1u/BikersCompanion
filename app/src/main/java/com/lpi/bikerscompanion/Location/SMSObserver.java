package com.lpi.bikerscompanion.Location;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

import com.lpi.bikerscompanion.MainActivity;
import com.lpi.bikerscompanion.R;
import com.lpi.bikerscompanion.TextToSpeech.TextToSpeechManager;
import com.lpi.bikerscompanion.Utils.PersistentData;
import com.lpi.bikerscompanion.Utils.Preferences;


public class SMSObserver extends ContentObserver {
	private static final String TAG = "SMSObserver";
	private static final String[] COLONNES_NUMERO = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

	Context context;

	public SMSObserver(Context c)
	{
		super(new Handler());
		context = c;
	}


	/***
	 * Envoi ou reception d'un SMS
	 */
	@Override
	public void onChange(boolean selfChange)
	{

		Log.d(TAG, "OnSMS"); //$NON-NLS-1$
		PersistentData data = PersistentData.getInstance(context);
		if (data.modeTrajet == PersistentData.MODE_TRAJET_ARRET)
			return;

		Preferences preferences = Preferences.getInstance(context);
		if (preferences.litSMS == Preferences.ANNONCE_SMS_JAMAIS)
			return;

		try
		{
			// Lire le dernier SMS non lu
			@SuppressWarnings("nls")
			Cursor cur = context.getContentResolver().query(Uri.parse("content://sms//inbox"),
					new String[]
							{"address", "body", "date "}, "read = 0", null, android.provider.CallLog.Calls.DATE + " DESC");
			if (cur == null)
				return;

			cur.moveToFirst();
			long date = cur.getLong(2);

			if (date != data.dateDernierSMS)
			{
				String adresse = cur.getString(0);
				String body = cur.getString(1);
				String contact = getContactFromNumber(adresse);
				if (contact == null && preferences.litSMS == Preferences.ANNONCE_SMS_TOUS)
					contact = adresse;

				if (contact != null)
					TextToSpeechManager.getInstance(context).annonce(String.format("Nouveau SMS de %1$s, %2$s", contact, body), TextToSpeechManager.REPETITION_TOUJOURS, 0, MainActivity.CATEGORIE_DEMIRESERVOIR);

				data.dateDernierSMS = date;
				data.flush();
			}

			cur.close();
		} catch (Exception e)
		{
			e.printStackTrace();
			//Erreur(e.getLocalizedMessage());
		}
	}

	/**
	 * Essaie de retrouver le nom d'un contact a partir de son numero de telephone
	 *
	 * @param numero : numero appelant
	 * @return le nom du contact ou "numero inconnu "+numero
	 */
	public String getContactFromNumber(String numero)
	{
		String res;

		try
		{
			Cursor c = context.getContentResolver().query(
					Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numero)), COLONNES_NUMERO, null,
					null, null);
			c.moveToFirst();
			res = c.getString(c.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
			c.close();

		} catch (Exception e)
		{
			if (numero.startsWith("+33")) //$NON-NLS-1$
			{
				numero = "0" + numero.substring(3); //$NON-NLS-1$
				return getContactFromNumber(numero);
			}
			else
				res = null;
		}

		return res;

		//String strFormat = context.getResources().getString(R.string.unknownContact);
		//return String.format(strFormat, numero);
	}


	@Override
	public boolean deliverSelfNotifications()
	{
		return false;
	}
}
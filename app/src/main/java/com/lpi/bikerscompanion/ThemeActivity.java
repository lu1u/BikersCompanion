package com.lpi.bikerscompanion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lpi.bikerscompanion.Utils.Preferences;

public class ThemeActivity extends AppCompatActivity {
	ListView _liste;
	public static final Integer[] THEMES_ID = {R.style.Theme1, R.style.Theme2, R.style.Theme3, R.style.Theme4, R.style.Theme5, R.style.Theme6};
	static private class Couleur
	{
		public int primary, dark, accent, background, texte;
		public Couleur(int id1, int id2, int id3, int id4, int id5)
		{
			primary =id1;
			dark =id2;
			accent =id3;
			background = id4;
			texte = id5;
		}
	}

	static private final Couleur[] COULEURS = { new Couleur(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent, R.color.couleurBack, R.color.primaryText),
			new Couleur(R.color.primary2, R.color.primary_dark2, R.color.accent2, R.color.couleurBack2, R.color.primaryText2),
			new Couleur(R.color.primary3, R.color.primary_dark3, R.color.accent3, R.color.couleurBack3, R.color.primaryText3),
			new Couleur(R.color.primary4, R.color.primary_dark4, R.color.accent4, R.color.couleurBack4, R.color.primaryText4),
			new Couleur(R.color.primary5, R.color.primary_dark5, R.color.accent5, R.color.couleurBack5, R.color.primaryText5),
			new Couleur(R.color.primary6, R.color.primary_dark6, R.color.accent6, R.color.couleurBack6, R.color.primaryText6)
	};
	/***
	 * Change le theme courant
	 * @param a
	 */
	public static void setTheme(Activity a)
	{
		Preferences p = Preferences.getInstance(a);
		int t = p.theme;
		if (t < 0)
			t = 0;
		if (t >= THEMES_ID.length)
			t = THEMES_ID.length - 1;
		a.setTheme(THEMES_ID[t]);
	}

	/**
	 * Take care of popping the fragment back stack or finishing the activity
	 * as appropriate.
	 */
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}

	/**
	 * react to the user tapping the back/up icon in the action bar
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
		setTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		_liste = (ListView) findViewById(R.id.idListe);

		String[] liste = getResources().getStringArray(R.array.themes);
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, liste);
		//_liste.setAdapter(arrayAdapter);
		_liste.setAdapter(new ThemeArrayAdapter(this));
		_liste.setSelection(Preferences.getInstance(this).theme);

		_liste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Activity a = ThemeActivity.this;
				Preferences prefs = Preferences.getInstance(a);
				prefs.theme = position;
				prefs.flush();

				setTheme(a);
				setResult(Activity.RESULT_OK);
				finish();
				TaskStackBuilder.create(a)
				                .addNextIntent(new Intent(a, MainActivity.class))
				                .addNextIntent(a.getIntent())
				                .startActivities();
			}
		});
	}


	private class ThemeArrayAdapter extends ArrayAdapter<Integer> {
		String[] _liste;

		/**
		 * Constructor
		 *
		 * @param context The current context.
		 */
		public ThemeArrayAdapter(@NonNull Context context)
		{
			super(context, 0, THEMES_ID);
			_liste = getResources().getStringArray(R.array.themes);

		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				view = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.themes_element_list, parent, false);
			}

			TextView tv = ((TextView) view.findViewById(R.id.textViewThemeName));
			tv.setText(_liste[position]);

			//((LinearLayout)view.findViewById(R.id.idLayoutBack)).setBackgroundColor(COULEURS[position].background);

			((ImageView)view.findViewById(R.id.imageViewPrimary)).setColorFilter(ContextCompat.getColor(getContext(), COULEURS[position].primary), android.graphics.PorterDuff.Mode.SRC_IN);
			((ImageView)view.findViewById(R.id.imageViewPrimaryDark)).setColorFilter(ContextCompat.getColor(getContext(), COULEURS[position].dark), android.graphics.PorterDuff.Mode.SRC_IN);
			((ImageView)view.findViewById(R.id.imageViewAccent)).setColorFilter(ContextCompat.getColor(getContext(), COULEURS[position].accent), android.graphics.PorterDuff.Mode.SRC_IN);
			((ImageView)view.findViewById(R.id.imageViewBackground)).setColorFilter(ContextCompat.getColor(getContext(), COULEURS[position].background), android.graphics.PorterDuff.Mode.SRC_IN);

			if ( position%2 ==0)
				view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAlternativeBackground));
			else
				view.setBackgroundColor(0xAAAAAA);
			return view;
		}
	}
}

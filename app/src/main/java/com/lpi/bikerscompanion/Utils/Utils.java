package com.lpi.bikerscompanion.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.lpi.bikerscompanion.R;

/**
 * Created by lucien on 07/05/2018.
 */
public class Utils
{
	public static void addHint(final Activity a, int id, final String message)
	{
		View v = a.findViewById(id);
		if (v == null)
			return;

		addHint(a, v, message);
	}
	public static void addHint( final Activity a, View v, final String message)
	{
		v.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
					//Toast.makeText(v.getContext(), ic_message, Toast.LENGTH_SHORT).set.show();
					displayToastAboveButton(a, v, message);
			}
		});
	}

	private static void displayToastAboveButton(Activity a, View v, String message)
	{
	/*
	int xOffset = 0;
	int yOffset = 0;
	Rect gvr = new Rect();

	View parent = (View) v.getParent();
	int parentHeight = parent.getHeight();

	if (v.getGlobalVisibleRect(gvr))
	{
		View root = v.getRootView();

		int halfWidth = root.getRight() / 2;
		int halfHeight = root.getBottom() / 2;

		int parentCenterX = ((gvr.right - gvr.left) / 2) + gvr.left;

		int parentCenterY = ((gvr.bottom - gvr.top) / 2) + gvr.top;

		if (parentCenterY <= halfHeight)
		{
			yOffset = -(halfHeight - parentCenterY) - parentHeight;
		}
		else
		{
			yOffset = (parentCenterY - halfHeight) - parentHeight;
		}

		if (parentCenterX < halfWidth)
		{
			xOffset = -(halfWidth - parentCenterX);
		}

		if (parentCenterX >= halfWidth)
		{
			xOffset = parentCenterX - halfWidth;
		}
	}

	LayoutInflater inflater = a.getLayoutInflater();
	View layout = inflater.inflate(R.layout.hint_toast_layout, (ViewGroup) a.findViewById(R.id.layoutRoot));
	TextView text = (TextView) layout.findViewById(R.id.text);
	text.setText(message);

	Toast toast = Toast.makeText(a, message, Toast.LENGTH_SHORT);
	toast.setGravity(Gravity.CENTER, xOffset, yOffset);
	toast.setView(layout);
	toast.show();
	*/
		Snackbar.make(v, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
	}

	public static Bitmap getBitmap(Context context, int resId)
	{
	/*
}
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		return context.getResources().getDrawable(resId, context.getApplicationContext().getTheme());
else
		return context.getResources().getDrawable(resId);      */
		return BitmapFactory.decodeResource(context.getResources(), resId);
	}

	public static void confirmDialog(@NonNull Activity a, @NonNull String titre, @NonNull String message, final int requestCode, final @NonNull ConfirmListener listener)
	{
		new AlertDialog.Builder(a)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(titre)
				.setMessage(message)
				.setPositiveButton(a.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						listener.onConfirmOK(requestCode);
					}

				})
				.setNegativeButton(a.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						listener.onConfirmCancel(requestCode);
					}

				})
				.show();
	}

	public interface ConfirmListener
	{
		void onConfirmOK(int requestCode);

		void onConfirmCancel(int requestCode);
	}
}

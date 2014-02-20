package de.ifgi.sitcom.campusmappergamified.dialogs;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.activities.MappingActivity;

/*
 * dialog shown after click on auto map symbol
 */
public class SettingsDialog extends SherlockDialogFragment {
	

	public final static String PREFS_SETTINGS = "de.ifgi.sitcom.campusmapper.settings";
	public final static String PREFS_SETTINGS_IMAGE_SCALE_FACTOR = "imageScaleFactor";
    
	private EditText mEditScaleFactor;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				getSherlockActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View dialogView = inflater.inflate(R.layout.dialog_settings, null);
		builder.setView(dialogView)
				// Add title
				.setTitle(R.string.title_settings)
				// Add action buttons
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

								int newValue = Integer
										.parseInt(mEditScaleFactor.getText()
												.toString());

								if (newValue > 0 && newValue < 10) {
									SharedPreferences prefs = getSherlockActivity()
											.getSharedPreferences(
													PREFS_SETTINGS, 0);
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putInt(
											PREFS_SETTINGS_IMAGE_SCALE_FACTOR,
											newValue);
									editor.commit();
								}

							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						});

		mEditScaleFactor = (EditText) dialogView
				.findViewById(R.id.editText_scale_factor);
		SharedPreferences prefs = getSherlockActivity().getSharedPreferences(
				PREFS_SETTINGS, 0);
		mEditScaleFactor.setText(Integer.toString(prefs.getInt(
				PREFS_SETTINGS_IMAGE_SCALE_FACTOR, 2)));

		return builder.create();
	}
	


}

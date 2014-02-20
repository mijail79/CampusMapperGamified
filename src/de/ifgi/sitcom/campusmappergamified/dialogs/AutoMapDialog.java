package de.ifgi.sitcom.campusmappergamified.dialogs;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.ifgi.sitcom.campusmappergamified.R;

/*
 * dialog shown after click on auto map symbol
 */
public class AutoMapDialog extends SherlockDialogFragment {
	
	
    
    // Use this instance of the interface to deliver action events
	private AutoMappingDialogListener mListener;

	
	
    public interface AutoMappingDialogListener {
        public void onAutoMappingDialogPositiveClick(AutoMapDialog dialog);
        public void onAutoMappingDialogNegativeClick(AutoMapDialog dialog);
    }
        
	public void attachHandler(AutoMappingDialogListener autoMappingDialogListener) {

		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = autoMappingDialogListener;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(autoMappingDialogListener.toString()
					+ " must implement AutoMappingDialogListener");
		}

	}
    
    
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    View dialogView = inflater.inflate(R.layout.dialog_auto_mapping, null);
	    builder.setView(dialogView)
	    // Add title
	    .setTitle(R.string.title_auto_mapping)
	    // Add action buttons
	           .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   if (mListener != null)
	                   mListener.onAutoMappingDialogPositiveClick(AutoMapDialog.this);
	               }
	           })
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   if (mListener != null)
	            	   mListener.onAutoMappingDialogNegativeClick(AutoMapDialog.this);
	               }
	           }); 
	    
	    
	    return builder.create();
	}
	


}

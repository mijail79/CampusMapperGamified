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
 * dialog shown after cancel auto mapping was clicked
 */
public class CancelAutoMapDialog extends SherlockDialogFragment {
	
	
    
    // Use this instance of the interface to deliver action events
	private CancelAutoMappingDialogListener mListener;

	
	
    public interface CancelAutoMappingDialogListener {
        public void onCancelAutoMappingDialogPositiveClick(CancelAutoMapDialog dialog);
        public void onCancelAutoMappingDialogNegativeClick(CancelAutoMapDialog dialog);
    }
        
	public void attachHandler(CancelAutoMappingDialogListener autoMappingDialogListener) {

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
	    View dialogView = inflater.inflate(R.layout.dialog_cancel_auto_mapping, null);
	    builder.setView(dialogView)
	    // Add title
	    .setTitle(R.string.title_cancel_auto_mapping)
	    // Add action buttons
	           .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   if (mListener != null)
	                   mListener.onCancelAutoMappingDialogPositiveClick(CancelAutoMapDialog.this);
	               }
	           })
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   if (mListener != null)
	                   mListener.onCancelAutoMappingDialogNegativeClick(CancelAutoMapDialog.this);
	               }
	           }); 
	    
	    
	    return builder.create();
	}
	


}

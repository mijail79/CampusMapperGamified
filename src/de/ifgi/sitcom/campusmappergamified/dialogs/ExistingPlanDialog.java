package de.ifgi.sitcom.campusmappergamified.dialogs;



import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;

/*
 * dialog shown when plan for selected location already exists
 */
public class ExistingPlanDialog extends SherlockDialogFragment {
	
    
    // Use this instance of the interface to deliver action events
	private ExistingPlanDialogListener mListener;
	
	private ListView mPlanList;
	

	private ArrayList<FloorPlan> mFloorPlans;
	private int selectedIndex = -1;
	
	
    public interface ExistingPlanDialogListener {
        public void onExistingPlanDialogPositiveClick(ExistingPlanDialog dialog);
        public void onExistingPlanDialogNegativeClick(ExistingPlanDialog dialog);
    }
    
    private String[] stringArrayFromFloorPlans(){
    	String [] result = new String [mFloorPlans.size() +1];
    	result[0] = getString(R.string.button_new_project);
    	
    	for (int i = 0; i < mFloorPlans.size(); i++){    		
    		result[i + 1] = mFloorPlans.get(i).getId();
    	}
    	
    	
    	return result;
    }
        
	public void attachHandler(Activity activity) {

		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (ExistingPlanDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}

	}
    
    
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    View dialogView = inflater.inflate(R.layout.dialog_existing_plan, null);
	    builder.setView(dialogView)
	    // Add title
	    .setTitle(R.string.title_existing_plan)
	    // Add action buttons
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   if (mListener != null)
	                   mListener.onExistingPlanDialogNegativeClick(ExistingPlanDialog.this);
	               }
	           }); 

	    
	    mPlanList = (ListView) dialogView.findViewById(R.id.listview_plans);
	    

	    if(mFloorPlans != null) showList(stringArrayFromFloorPlans());

	  		
	    return builder.create();
	}
	
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}
	
	
	private void showList(String [] listItems){

    	if( getSherlockActivity() != null){
        	
        	ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_1, listItems);
        	mPlanList.setAdapter(arrayAdapter);
        	mPlanList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> a, View view,
						int position, long id) {
					
					selectedIndex = position;
					ExistingPlanDialog.this.getDialog().cancel();
	                   mListener.onExistingPlanDialogPositiveClick(ExistingPlanDialog.this);
					
				}
			});        		
    	}
	}
	
	// returns null when new plan is choosen
	public FloorPlan getSelectedPlan(){
		
		if (selectedIndex > 0){
			return mFloorPlans.get(selectedIndex - 1);
		} else {
			return null;
		}
		
	}

	
    public void setFloorPlans(ArrayList<FloorPlan> floorPlans){
    	this.mFloorPlans = floorPlans;
    }



}

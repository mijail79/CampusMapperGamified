package de.ifgi.sitcom.campusmappergamified.dialogs;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.activities.ChooseLocationActivity;
import de.ifgi.sitcom.campusmappergamified.activities.IndoorConnectionActivity;
import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import de.ifgi.sitcom.campusmappergamified.indoordata.Connection;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.indoordata.Stairs;
import de.ifgi.sitcom.campusmappergamified.indoordata.VerticalConnection;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.EmptyGeometry;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;

/* dialog shown when stairs/ elevator was created/ edited
 * 
 */
public class StairsDialog extends SherlockDialogFragment {

	private RadioButton mRadioButtonStairs;
	private RadioButton mRadioButtonElevator;
	private Button mAddDestinationButton;
	private VerticalConnection mVerticalConnection;
	
	private ArrayList<Geometry> mDestinations;
	private ArrayList<EditText> mDestinationEdits;
	private ArrayList<Button> mDestinationButtons;
	private LinearLayout mDestinationsLayout;
	

	private int mBasicFloorNumber;
	private String mBuildingURI;

	// Use this instance of the interface to deliver action events
	private StairsDialogListener mListener;

	public interface StairsDialogListener {
		public void onStairsDialogPositiveClick(StairsDialog dialog);

		public void onStairsDialogNegativeClick(StairsDialog dialog);
	}

	public void attachHandler(UIActionHandler mappingTouchHandler) {

		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (StairsDialogListener) mappingTouchHandler;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(mappingTouchHandler.toString()
					+ " must implement NoticeDialogListener");
		}

	}
	
	private void initViews(View dialogView){
		// init view elements
		mRadioButtonStairs = (RadioButton) dialogView
				.findViewById(R.id.radio_stairs);
		mRadioButtonElevator = (RadioButton) dialogView
				.findViewById(R.id.radio_elevator);
		mDestinationsLayout =  (LinearLayout) dialogView
				.findViewById(R.id.linearLayout_stairs_dialog);
		mAddDestinationButton =  (Button) dialogView
				.findViewById(R.id.button_add_destination_floor);
		mDestinationEdits = new ArrayList<EditText>();
		mDestinationButtons = new ArrayList<Button>();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				getSherlockActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View dialogView = inflater.inflate(R.layout.dialog_stairs, null);
		builder.setView(dialogView)
				// Add title
				.setTitle(R.string.title_stairs)
				// Add action buttons
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								if (mListener != null)
									mListener
											.onStairsDialogPositiveClick(StairsDialog.this);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (mListener != null)
									mListener
											.onStairsDialogNegativeClick(StairsDialog.this);
							}
						});

		// init view elements
		initViews(dialogView);
		
		showDestinationFloors();
		
		// listener for addDestinationButton
		mAddDestinationButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				createDestinationFloorView("", false);
				mDestinations.add(new EmptyGeometry(new FloorPlan()));
				
			}
		});

		/*
		 * listener for radio group: if checked changed, we clear the
		 * destinations
		 */
		RadioGroup radioGroupStairsElevator = (RadioGroup) dialogView
				.findViewById(R.id.radiogroup_stairs_elevator);
		radioGroupStairsElevator
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {

					}
				});

		/*
		 * when entrance is not null, we edit an existing entrance, so show its
		 * properties
		 */
		if (mVerticalConnection != null) {

			if (mVerticalConnection.getClass() == Stairs.class) {
				mRadioButtonStairs.setChecked(true);
				mRadioButtonElevator.setVisibility(View.GONE);

			} else {
				// otherwise we have an elevator
				mRadioButtonElevator.setChecked(true);
				mRadioButtonStairs.setVisibility(View.GONE);
			}

		}

		return builder.create();
	}
	
	private void showDestinationFloors(){
		// if vertical connection was set, show all its destinations
		if(mVerticalConnection != null && mVerticalConnection.getDestinations() != null && mVerticalConnection.getDestinations().size() > 0){

			for (Geometry g : mVerticalConnection.getDestinations()){
				boolean locationKnown = g.getClass() != EmptyGeometry.class;
				createDestinationFloorView(Integer.toString(g.getFloorPlan().getFloorNumber()), locationKnown);				
			}
			
			mDestinations = mVerticalConnection.getDestinations();

		}
		// if it has no destinations create new ArrayList for destinations to come
		else {
		
			mDestinations = new ArrayList<Geometry>();
		}
		
		

	}
	
	private void createDestinationFloorView(String floorNumber, boolean locationKnown){
		
		//set index
		final int index = mDestinationEdits.size();
		
		// create LinearLayout
		LinearLayout ll = new LinearLayout(getSherlockActivity());
		ll.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(4, 4, 4, 4);
		ll.setLayoutParams(llp);
		// create EditText
		EditText editText = new EditText(getSherlockActivity());
		editText.setText(floorNumber);
		llp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(4, 4, 4, 4);
		editText.setLayoutParams(llp);
		editText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);

		// create Button
		Button button = new Button(getSherlockActivity());
		llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(4, 4, 4, 4);
		button.setLayoutParams(llp);
		// set icon
		if (locationKnown)
			button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_found, 0, 0, 0);
		else
			button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location, 0, 0, 0);			
		// set clickListener
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				
				
				
				// verify that floor was selected properly
				try {
					Intent intent = new Intent(getSherlockActivity(),
							IndoorConnectionActivity.class);
					int floorNumber = Integer.parseInt(mDestinationEdits.get(index)
							.getText().toString());
					
					if (floorNumber != mBasicFloorNumber) {
					intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_URI, mBuildingURI);
						intent.putExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER, floorNumber);
						if (mRadioButtonStairs.isChecked())
							intent.putExtra(IndoorConnectionActivity.EXTRA_FEATURE_TYPE, Connection.CONNECTION_TYPE_STAIRS);
						else
							intent.putExtra(IndoorConnectionActivity.EXTRA_FEATURE_TYPE, Connection.CONNECTION_TYPE_ELEVATOR);

						if(mDestinations.get(index).getClass() == Point.class){
							int x = ((Point)mDestinations.get(index)).getX();
							int y = ((Point)mDestinations.get(index)).getY();
							
							intent.putExtra(IndoorConnectionActivity.EXTRA_COORDINATE_X, x);
							intent.putExtra(IndoorConnectionActivity.EXTRA_COORDINATE_Y, y);
							intent.putExtra(IndoorConnectionActivity.EXTRA_ESCAPE_PLAN_URI, mDestinations.get(index).getFloorPlan().getEscapePlanURI());
							intent.putExtra(IndoorConnectionActivity.EXTRA_FLOOR_URI, mDestinations.get(index).getFloorPlan().getFloorURI());

						}
						
						startActivityForResult(intent, index);

					} else {
						// notify user to choose higher floornumber
						Toast.makeText(getSherlockActivity(),
								"Please select another floor.",
								Toast.LENGTH_SHORT).show();
					}

				} catch (NumberFormatException e) {
					Log.e("debug", "invalid floornumber");
					Toast.makeText(getSherlockActivity(),
							"Please choose from the floor.",
							Toast.LENGTH_SHORT).show();
				}
				
			}
		});

		// add LinearLayout to Dialog`s View
		mDestinationsLayout.addView(ll);
		
		// add editText and Button to LinearLayout
		ll.addView(button);
		ll.addView(editText);

		// tv should focus
		editText.requestFocus();
		
		// add editText and Button to list
		mDestinationEdits.add(editText);
		mDestinationButtons.add(button);
		
	}

	public void updateVerticalConnection(VerticalConnection v) {

		ArrayList<Geometry> updatedDestinations = new ArrayList<Geometry>();
		
		// update floornumbers
		for(int i = 0; i < mDestinationEdits.size(); i++){
			
			Geometry updatedGeometry = mDestinations.get(i);
			EditText et = mDestinationEdits.get(i);
			
			try {
				updatedGeometry.getFloorPlan().setFloorNumber(Integer.parseInt(et.getText().toString()));
				updatedDestinations.add(updatedGeometry);

			} catch (NumberFormatException e) {
				Log.e("debug", "invalid floornumber");
			}			
		}
		
		v.setDestinations(updatedDestinations);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

			if (resultCode == Activity.RESULT_OK) {

				int x = data.getIntExtra(
						IndoorConnectionActivity.EXTRA_COORDINATE_X, -1);
				int y = data.getIntExtra(
						IndoorConnectionActivity.EXTRA_COORDINATE_Y, -1);

				if (x != -1 && y != -1) {
					Point destination = new Point(x, y);
					
					FloorPlan floorPlan = new FloorPlan();
					floorPlan.setEscapePlanURI(data.getStringExtra(IndoorConnectionActivity.EXTRA_ESCAPE_PLAN_URI));
					floorPlan.setFloorURI(data.getStringExtra(IndoorConnectionActivity.EXTRA_FLOOR_URI));

					destination.setFloorPlan(new FloorPlan());
					mDestinations.set(requestCode, destination);

					// change icon on button to indicate position was set
					mDestinationButtons.get(requestCode).setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.ic_location_found, 0, 0, 0);
					return;
				}
			}

			
			// if result was not ok, set position to null and show default icon
			mDestinations.set(requestCode, new EmptyGeometry(new FloorPlan()));
			// change icon on button to indicate position was not set
			mDestinationButtons.get(requestCode).setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_location, 0, 0, 0);
		
	}

	/*
	 * getters and setters
	 */

	public int getBasicFloorNumber() {
		return mBasicFloorNumber;
	}

	public void setBasicFloorNumber(int basicFloorNumber) {
		this.mBasicFloorNumber = basicFloorNumber;
	}

	public VerticalConnection getVerticalConnection() {
		return mVerticalConnection;
	}

	public void setVerticalConnection(VerticalConnection verticalConnection) {
		this.mVerticalConnection = verticalConnection;
	}

	public int getType() {
		if (mRadioButtonStairs.isChecked())
			return Connection.CONNECTION_TYPE_STAIRS;
		else
			return Connection.CONNECTION_TYPE_ELEVATOR;
	}
	
	public String getBuildingURI() {
		return mBuildingURI;
	}

	public void setBuildingURI(String buildingURI) {
		this.mBuildingURI = buildingURI;
	}

	public ArrayList<Geometry> getDestinations() {
		return mDestinations;
	}

	public void setDestinations(ArrayList<Geometry> destinations) {
		this.mDestinations = destinations;
	}
	
	

}

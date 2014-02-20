package de.ifgi.sitcom.campusmappergamified.dialogs;

import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.activities.ChooseLocationActivity;
import de.ifgi.sitcom.campusmappergamified.activities.IndoorConnectionActivity;
import de.ifgi.sitcom.campusmappergamified.activities.OutdoorConnectionActivity;
import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import de.ifgi.sitcom.campusmappergamified.indoordata.Connection;
import de.ifgi.sitcom.campusmappergamified.indoordata.Entrance;
import de.ifgi.sitcom.campusmappergamified.indoordata.EntranceIndoor;
import de.ifgi.sitcom.campusmappergamified.indoordata.EntranceOutdoor;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;

/*
 * dialog shown when entrance was created/ edited
 */
public class EntranceDialog extends SherlockDialogFragment {

	private RadioButton mRadioButtonOutdoor;
	private RadioButton mRadioButtonIndoor;
	private Button mButtonDestEntrance;
	private Entrance mEntrance;


	private FloorPlan mBasicFloorPlan;
	
	private Point mPositionIndoor;
	private GeoPoint mPositionOutdoor;

	static final int REQUEST_FEATURE_LOCATION_ENTRANCE_INDOOR = 1;
	static final int REQUEST_FEATURE_LOCATION_ENTRANCE_OUTDOOR = 2;

	// Use this instance of the interface to deliver action events
	private EntranceDialogListener mListener;

	public interface EntranceDialogListener {
		public void onEntranceDialogPositiveClick(EntranceDialog dialog);

		public void onEntranceDialogNegativeClick(EntranceDialog dialog);
	}
	
	public void init(FloorPlan basicFloorPlan){
		this.mBasicFloorPlan = basicFloorPlan;
	}

	public void attachHandler(UIActionHandler mappingTouchHandler) {

		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (EntranceDialogListener) mappingTouchHandler;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(mappingTouchHandler.toString()
					+ " must implement NoticeDialogListener");
		}

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				getSherlockActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View dialogView = inflater.inflate(R.layout.dialog_entrance, null);
		builder.setView(dialogView)
				// Add title
				.setTitle(R.string.title_entrace)
				// Add action buttons
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								if (mListener != null)
									mListener
											.onEntranceDialogPositiveClick(EntranceDialog.this);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (mListener != null)
									mListener
											.onEntranceDialogNegativeClick(EntranceDialog.this);
							}
						});

		// access view elements
		mRadioButtonOutdoor = (RadioButton) dialogView
				.findViewById(R.id.radio_outdoors);
		mRadioButtonIndoor = (RadioButton) dialogView
				.findViewById(R.id.radio_indoors);
		mButtonDestEntrance = (Button) dialogView
				.findViewById(R.id.button_choose_destination_entrance);

		// set onClick listeners
		mButtonDestEntrance.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mRadioButtonOutdoor.isChecked()) {
					// selected outdoor position
					
					launchOutdoorConnectionActivity();

				} else {
					// select position indoors, on other plan on same floor
					launchIndoorConnectionActivity();

				}

			}

		});

		/*
		 * listener for the radio group if checked changed, we clear the
		 * destinations
		 */
		RadioGroup radioGroupStairsElevator = (RadioGroup) dialogView
				.findViewById(R.id.radiogroup_indoor_outdoor);
		radioGroupStairsElevator
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						mPositionIndoor = null;
						// change icon on button to indicate position was not
						// set
						mButtonDestEntrance
								.setCompoundDrawablesWithIntrinsicBounds(
										R.drawable.ic_location, 0, 0, 0);
					}
				});
		
		/*
		 * when entrance is not null, we edit an existing entrance, so show its properties
		 */
		if (mEntrance != null){
			if (mEntrance.getType() == Connection.CONNECTION_TYPE_ENTRANCE_INDOOR){
				mRadioButtonIndoor.setChecked(true);
				mRadioButtonOutdoor.setVisibility(View.GONE);

				if(((EntranceIndoor) mEntrance).getGeometryB() != null){
					mPositionIndoor = (Point) ((EntranceIndoor) mEntrance).getGeometryB();
					// change icon on button to indicate position was set
					mButtonDestEntrance
							.setCompoundDrawablesWithIntrinsicBounds(
									R.drawable.ic_location_found, 0, 0, 0);
				}
				
				
			} else {
				// otherwise we have an entrance from outdoors
				mRadioButtonOutdoor.setChecked(true);
				mRadioButtonIndoor.setVisibility(View.GONE);

				if(((EntranceOutdoor) mEntrance).getPositionOutdoors() != null){
					mPositionOutdoor = ((EntranceOutdoor) mEntrance).getPositionOutdoors();
					// change icon on button to indicate position was set
					mButtonDestEntrance
							.setCompoundDrawablesWithIntrinsicBounds(
									R.drawable.ic_location_found, 0, 0, 0);
				}
				
			}
			
		}

		return builder.create();
	}
	
	private void launchOutdoorConnectionActivity(){

		Intent intent = new Intent(getSherlockActivity(),
				OutdoorConnectionActivity.class);
		intent.putExtra(OutdoorConnectionActivity.EXTRA_BUILDING_NAME,
				mBasicFloorPlan.getBuildingName());
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_URI,
				mBasicFloorPlan.getBuildingURI());
		startActivityForResult(intent,
				REQUEST_FEATURE_LOCATION_ENTRANCE_OUTDOOR);
	}
	
	private void launchIndoorConnectionActivity(){
		Intent intent = new Intent(getSherlockActivity(),
				IndoorConnectionActivity.class);
		intent.putExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER,
				mBasicFloorPlan.getFloorNumber());
		intent.putExtra(OutdoorConnectionActivity.EXTRA_BUILDING_NAME,
				mBasicFloorPlan.getBuildingName());
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_URI,
				mBasicFloorPlan.getBuildingURI());
		intent.putExtra(IndoorConnectionActivity.EXTRA_FEATURE_TYPE,
				Connection.CONNECTION_TYPE_ENTRANCE_INDOOR);
		
		
		if(mPositionIndoor != null){
			int x = mPositionIndoor.getX();
			int y = mPositionIndoor.getY();
			
			intent.putExtra(IndoorConnectionActivity.EXTRA_COORDINATE_X, x);
			intent.putExtra(IndoorConnectionActivity.EXTRA_COORDINATE_Y, y);
			intent.putExtra(IndoorConnectionActivity.EXTRA_ESCAPE_PLAN_URI, mPositionIndoor.getFloorPlan().getEscapePlanURI());
			intent.putExtra(IndoorConnectionActivity.EXTRA_FLOOR_URI, mPositionIndoor.getFloorPlan().getFloorURI());

		}
		
		
		startActivityForResult(intent,
				REQUEST_FEATURE_LOCATION_ENTRANCE_INDOOR);
	}
	
	public void updateEntrance(Entrance e){
		
		if (e.getClass() == EntranceIndoor.class){
			EntranceIndoor entranceIndoor = (EntranceIndoor) e;			
			entranceIndoor.setGeometryB(mPositionIndoor);
			mPositionIndoor.getFloorPlan().setFloorNumber(mBasicFloorPlan.getFloorNumber());
			entranceIndoor.setFloorPlanB(mPositionIndoor.getFloorPlan());
		} else {
			// otherwise we have an entrance from outdoors
			
			EntranceOutdoor entranceOutoor = (EntranceOutdoor) e;
			entranceOutoor.setPositionOutdoors(mPositionOutdoor);
		}
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_FEATURE_LOCATION_ENTRANCE_INDOOR:
			if (resultCode == Activity.RESULT_OK) {

				int x = data.getIntExtra(
						IndoorConnectionActivity.EXTRA_COORDINATE_X, -1);
				int y = data.getIntExtra(
						IndoorConnectionActivity.EXTRA_COORDINATE_Y, -1);

				if (x != -1 && y != -1) {
					mPositionIndoor = new Point(x, y);
					
					FloorPlan floorPlan = new FloorPlan();
					floorPlan.setEscapePlanURI(data.getStringExtra(IndoorConnectionActivity.EXTRA_ESCAPE_PLAN_URI));
					floorPlan.setFloorURI(data.getStringExtra(IndoorConnectionActivity.EXTRA_FLOOR_URI));
					mPositionIndoor.setFloorPlan(new FloorPlan());

					// change icon on button to indicate position was set
					mButtonDestEntrance.setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.ic_location_found, 0, 0, 0);
					return;
				}
			}

			// if result was not ok, set position to null and show default icon
			mPositionIndoor = null;
			// change icon on button to indicate position was not set
			mButtonDestEntrance.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_location, 0, 0, 0);
			break;

			
			
		case REQUEST_FEATURE_LOCATION_ENTRANCE_OUTDOOR:
			if (resultCode == Activity.RESULT_OK) {

				int latE6 = data.getIntExtra(
						OutdoorConnectionActivity.EXTRA_LAT_E6, 0);
				int lonE6 = data.getIntExtra(
						OutdoorConnectionActivity.EXTRA_LON_E6, 0);

				if (latE6 != 0 && lonE6 != 0) {
					mPositionOutdoor = new GeoPoint(latE6, lonE6);

					// change icon on button to indicate position was set
					mButtonDestEntrance.setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.ic_location_found, 0, 0, 0);
					return;
				}
			}

			// if result was not ok, set position to null and show default icon
			mPositionOutdoor = null;
			// change icon on button to indicate position was not set
			mButtonDestEntrance.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_location, 0, 0, 0);

			break;
			
		}
	}

	
	/*
	 * getters and setters
	 */

	public Entrance getEntrance() {
		return mEntrance;
	}

	public void setEntrance(Entrance entrance) {
		this.mEntrance = entrance;
	}

	public Point getPositionIndoor() {
		return mPositionIndoor;
	}

	public void setPositionIndoor(Point position) {
		this.mPositionIndoor = position;
	}


	public GeoPoint getPositionOutdoor() {
		return mPositionOutdoor;
	}

	public void setPositionOutdoor(GeoPoint positionOutdoor) {
		this.mPositionOutdoor = positionOutdoor;
	}

	public FloorPlan getBasicFloorPlan() {
		return mBasicFloorPlan;
	}

	public void setBasicFloorPlan(FloorPlan basicFloorPlan) {
		this.mBasicFloorPlan = basicFloorPlan;
	}
	
	public int getType() {
		if (mRadioButtonOutdoor.isChecked())
			return Connection.CONNECTION_TYPE_ENTRANCE_OUTDOOR;
		else
			return Connection.CONNECTION_TYPE_ENTRANCE_INDOOR;
	}
	
	
}

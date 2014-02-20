package de.ifgi.sitcom.campusmappergamified.activities;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.dialogs.ExistingPlanDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.SettingsDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.ExistingPlanDialog.ExistingPlanDialogListener;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.io.RDFReader;
import de.ifgi.sitcom.campusmappergamified.io.RDFWriter;
import de.ifgi.sitcom.campusmappergamified.io.TripleStoreQueries;
import de.ifgi.sitcom.campusmappergamified.outdoordata.Building;

import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

/*
 * Allows to select the building and the floor where the floorplan is situated. 
 * Requests a list of all buildings from the triple store using 
 * TripleStoreConnector.queryBuildings(). Only buildings included in the list 
 * are allowed to be selected. It is mandatory to fill both, building and 
 * floor. After completion either ImageSourceActivity or ExistingPlanActivity 
 * (to be implemented) is called, depending on if there already exists data 
 * for the selected building and floor.
 */
public class ChooseLocationActivity extends SherlockFragmentActivity  implements ExistingPlanDialogListener{
	
	public final static String EXTRA_BUILDING_NAME = "de.ifgi.sitcom.campusmapper.buildingName";
	public final static String EXTRA_FLOOR_NUMBER = "de.ifgi.sitcom.campusmapper.floorNumber";
	public final static String EXTRA_BUILDING_URI = "de.ifgi.sitcom.campusmapper.buildingURI";
	public final static String EXTRA_LOAD_FROM_SERVER = "de.ifgi.sitcom.campusmapper.loadFromServer";
	public final static String EXTRA_LOAD_LOCAL_DATA = "de.ifgi.sitcom.campusmapper.loadLocalData";

	// textView to select the building
	private AutoCompleteTextView mTextViewBuilding;
	// textView to select the floor level
	private AutoCompleteTextView mTextViewFloor;
	// progressCircle to be shown until Lodum data was loaded
	ProgressDialog mProgressDialog;
	
	private LoadFromLODUM mLoadFromLODUM;
	// to be filled with the data loaded from Lodum
	private ArrayList<Building> mBuildings;
	private String [] mBuildingNames;
	private int mBuildingIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_building_information);
		
		// enable up button in action bar
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		

		mTextViewBuilding = (AutoCompleteTextView) findViewById(R.id.multiAutoCompleteTextView1);
		mTextViewFloor = (AutoCompleteTextView) findViewById(R.id.multiAutoCompleteTextView2);
		
		// add listeners to handle done, enter, next, etc.
		mTextViewBuilding.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (event != null){
					if (event.getAction() == KeyEvent.KEYCODE_ENTER ||
							event.getAction() == KeyEvent.ACTION_DOWN){
						mTextViewFloor.requestFocus();
						return true;
					}
					
				}
				
				
				return false;
			}
		});
		
		
		mTextViewFloor.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (event != null){
					if (event.getAction() == KeyEvent.KEYCODE_ENTER ||
							event.getAction() == KeyEvent.ACTION_DOWN){


						onOk();
						return true;
					}
					
				}
				
				if(actionId == EditorInfo.IME_ACTION_DONE) {
					onOk();
					return true;
				}
				
				
				return false;
			}
		});
		
//		llProgressCircle = (LinearLayout) findViewById(R.id.ll_progress_circle);
		// show progress dialog
		mProgressDialog = ProgressDialog.show(this,
						"",
						"",
						true);
		
		
		mBuildings = (ArrayList<Building>) getLastCustomNonConfigurationInstance();
		if (mBuildings == null) {

			// hide progress circle
//			llProgressCircle.setVisibility(View.VISIBLE);
			
			// load in async task
			if (!isNetworkAvailable(ChooseLocationActivity.this)) {
				mProgressDialog.dismiss();
				Toast.makeText(
						ChooseLocationActivity.this,
						getString(R.string.message_no_internet),
						Toast.LENGTH_SHORT).show();
			} else
				{
				mLoadFromLODUM = new LoadFromLODUM();
				mLoadFromLODUM.execute("");
				}
		} else showBuildings();

	}
	
	
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
	    return mBuildings;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.building_information, menu);
		//	update score in the menu
		menu.findItem(R.id.action_score).setTitle(String.format("%04d", MyCampusMapperGame.getInstance().getMyScore()));

		return true;
	}

	private boolean isValid(){
		
		if (!isNetworkAvailable(this)){
			Toast.makeText(ChooseLocationActivity.this, getString(R.string.message_no_internet), Toast.LENGTH_SHORT).show();
		    return false;
		}
		
		
		String buildingName = mTextViewBuilding.getText().toString();
		if(buildingName.length() == 0 || mTextViewFloor.getText().toString().length() == 0) {
			Toast.makeText(this, getString(R.string.message_chose_building_and_floor), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		// check if building exists
		if (mBuildingNames != null)
			for (String b: mBuildingNames){
				if (buildingName.equals(b)) return true;		
			}
			
		Toast.makeText(this, getString(R.string.message_building_not_in_database), Toast.LENGTH_SHORT).show();
		return false;
	}
	
	public void launchImageSourceActivity() {
		// stop async task
		if (mLoadFromLODUM != null) mLoadFromLODUM.cancel(true);
		
		Intent intent = new Intent(this, ImageSourceActivity.class);
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_NAME, mTextViewBuilding.getText().toString());
		intent.putExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER, mTextViewFloor.getText().toString());
		if(mBuildings != null && mBuildingIndex >= 0)
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_URI, mBuildings.get(mBuildingIndex).getBuildingURI());
		startActivity(intent);
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    
	      case android.R.id.home:
	          // app icon in action bar clicked; go home
	          Intent intent = new Intent(this, StartActivity.class);
	          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	          startActivity(intent);
	          return true;
	    
		case R.id.action_ok:
			
			onOk();
			return true;
			
		case R.id.action_settings:			
			new SettingsDialog().show(getSupportFragmentManager(), "");
			return true;


	    default:
	      break;
	    }

	    return true;
	  }
	
	private void onOk(){
		if (! isValid()) return;
		
		/*
		 * check if there already exists data for the seleceted floor and building
		 * 
		 * if so, show dialog offering to edit existing data or to create new plan
		 */

		mProgressDialog = ProgressDialog.show(this,
				"",
				"",
				true);
		new Thread(new Runnable() {
			public void run() {
				ArrayList<FloorPlan> floorPlans = getFloorPlans();
				mProgressDialog.dismiss();
				if (floorPlans != null && floorPlans.size() > 0){
					// show dialog
					showChoosePlanDialog(floorPlans);
				} else {
					launchImageSourceActivity();
				}
			}
		}).start();	  			
	}
	
	
	 private void showChoosePlanDialog(ArrayList<FloorPlan> floorPlans){
			
			 ExistingPlanDialog choosePlanDialogFragment = new ExistingPlanDialog();
			choosePlanDialogFragment.attachHandler(this);
			choosePlanDialogFragment.setFloorPlans(floorPlans);
			choosePlanDialogFragment.show(getSupportFragmentManager(), "");

		 
		 
	 }
	 
	private ArrayList<FloorPlan> getFloorPlans(){
		
		if(mBuildings == null || mBuildingIndex < 0) return null;

		RDFReader rdfReader = new RDFReader();
		ArrayList<FloorPlan> floorPlans = rdfReader.getFloorPlanList(mBuildings.get(mBuildingIndex).getBuildingURI(), mTextViewFloor.getText().toString(), mBuildings.get(mBuildingIndex).getBuildingName());
		
		return floorPlans;
	}
	
	public static boolean isNetworkAvailable(Context context) 
	{
	    return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
	}
	
    private String [] buildingsToString(ArrayList<Building> buildings){
   	 
   	 String [] buildingNames = new String [buildings.size()];
   	 for (int i = 0; i < buildings.size(); i++){
   		 buildingNames[i] = buildings.get(i).getBuildingName();
   	 }
   	 
   	 return buildingNames;
    }
	
	private void showBuildings(){
		
		mBuildingIndex = -1;
		mBuildingNames = buildingsToString(mBuildings);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				ChooseLocationActivity.this,
				android.R.layout.simple_dropdown_item_1line, mBuildingNames);

		mTextViewBuilding.setAdapter(adapter);
		mTextViewBuilding.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTextViewBuilding.showDropDown();
			}
		});

		mTextViewBuilding.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int index, long arg3) {

				mBuildingIndex = index;
				mTextViewFloor.requestFocus();
				
				// show keyboard
				InputMethodManager imm = (InputMethodManager) ChooseLocationActivity.this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.showSoftInput(mTextViewFloor, 0);
				}
			}
		});
		
		// hide progress circle
//		llProgressCircle.setVisibility(View.GONE);
		mProgressDialog.dismiss();
	}
	
	
	
	 private class LoadFromLODUM extends AsyncTask<String, Void, ArrayList<Building>> {

         @Override
         protected ArrayList<Building> doInBackground(String... params) {

               return new TripleStoreQueries().queryBuildings();
         }      

         @Override
		protected void onPostExecute(ArrayList<Building> buildings) {

			if (buildings == null) {
				Toast.makeText(
						ChooseLocationActivity.this,
						getString(R.string.message_no_server_response),
						Toast.LENGTH_SHORT).show();
				return;
			}

			Log.v("buildings loaded from lodum", "" + buildings.size());

			ChooseLocationActivity.this.mBuildings = buildings;
			showBuildings();			
			mTextViewBuilding.showDropDown();
			
	       	 // show keyboard
	       	InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	       	if(imm != null) {
	       	    imm.showSoftInput(mTextViewBuilding, 0); 
	       	}
		}
         

         @Override
         protected void onPreExecute() {
         }

         @Override
         protected void onProgressUpdate(Void... values) {
         }
         
	 }

	 private void startMappingActivity(FloorPlan floorPlan){

		 Intent intent = new Intent(this, MappingActivity.class);
			intent.putExtra(ImageSourceActivity.EXTRA_SOURCE_URI, floorPlan.getSourceFloorPlanImageUri().toString());
			intent.putExtra(ImageSourceActivity.EXTRA_CROPPED_URI, floorPlan.getCroppedFloorPlanImageUri().toString());
			intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_NAME, floorPlan.getBuildingName());
			intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_URI, floorPlan.getBuildingURI());
			intent.putExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER, Integer.toString(floorPlan.getFloorNumber()));
			intent.putExtra(ChooseLocationActivity.EXTRA_LOAD_FROM_SERVER, true);
			
			startActivity(intent);
	 }


	@Override
	public void onExistingPlanDialogPositiveClick(ExistingPlanDialog dialog) {
		final FloorPlan floorPlan = dialog.getSelectedPlan();
		
		if(floorPlan == null){
			// create new plan
			launchImageSourceActivity();
		} else {
			mProgressDialog = ProgressDialog.show(this,
					"",
					"",
					true);
			new Thread(new Runnable() {
				public void run() {

					// load floorplan data

					RDFReader rdfReader = new RDFReader();
					rdfReader.getFloorPlan(floorPlan);
					
					// save floorplan data on sd
					RDFWriter rdfCreater = new RDFWriter();
					rdfCreater.floorPlanToRDF(floorPlan); // will be written to temp.ttl in campusMapper on sd
					
					mProgressDialog.dismiss();
					
					// start mappig activity
					startMappingActivity(floorPlan);				
				}
			}).start();				
		}
		
		
	}

	@Override
	public void onExistingPlanDialogNegativeClick(ExistingPlanDialog dialog) {
		// do nothing		
	}

}

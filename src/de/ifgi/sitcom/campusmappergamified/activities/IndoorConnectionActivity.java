package de.ifgi.sitcom.campusmappergamified.activities;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.dialogs.ChoosePlanDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.SettingsDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.ChoosePlanDialog.ChoosePlanDialogListener;
import de.ifgi.sitcom.campusmappergamified.handler.mapping.SelectTargetHandler;
import de.ifgi.sitcom.campusmappergamified.indoordata.Connection;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.io.RDFReader;
import de.ifgi.sitcom.campusmappergamified.views.ImageView;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class IndoorConnectionActivity extends SherlockFragmentActivity implements ChoosePlanDialogListener {

	private ChoosePlanDialog choosePlanDialogFragment;
	
	private ImageView mView;	
	private ProgressBar mProgressBar;
	private FloorPlan mFloorPlan;
	private int mFeatureType;
	private SelectTargetHandler mSelectFeatureHandler;
	int mPosX = -1;
	int mPosY = -1;

	public static final String EXTRA_FEATURE_TYPE = "de.ifgi.sitcom.campusmapper.featureType";
	public static final String EXTRA_COORDINATE_X = "de.ifgi.sitcom.campusmapper.coordinateX";
	public static final String EXTRA_COORDINATE_Y = "de.ifgi.sitcom.campusmapper.coordinateY";
	public static final String EXTRA_ESCAPE_PLAN_URI = "de.ifgi.sitcom.campusmapper.escapePlanURI";
	public static final String EXTRA_FLOOR_URI = "de.ifgi.sitcom.campusmapper.escapePlanURI";
	public static final String EXTRA_FEATURE_URI = "de.ifgi.sitcom.campusmapper.featureURI";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		mFeatureType = intent.getIntExtra(EXTRA_FEATURE_TYPE, Connection.CONNECTION_TYPE_STAIRS);
		
		mPosX =  intent.getIntExtra(EXTRA_COORDINATE_X, -1);
		mPosY =  intent.getIntExtra(EXTRA_COORDINATE_Y, -1);
		String escapePlanUri =  intent.getStringExtra(EXTRA_ESCAPE_PLAN_URI);
		String floorUri =  intent.getStringExtra(EXTRA_FLOOR_URI);
		
		
		switch (mFeatureType) {
		case Connection.CONNECTION_TYPE_STAIRS:
			getSupportActionBar().setTitle(R.string.title_choose_stairs_location);			
			break;
		case Connection.CONNECTION_TYPE_ELEVATOR:
			getSupportActionBar().setTitle(R.string.title_choose_elevator_location);
			break;
		case Connection.CONNECTION_TYPE_ENTRANCE_INDOOR:
			getSupportActionBar().setTitle(R.string.title_choose_entrance_indoor_location);
			break;
		}

		
    	mView = new ImageView(IndoorConnectionActivity.this, null, null);
    	RelativeLayout rLayout = new RelativeLayout(this);
		rLayout.addView(mView);
		
		
		// create progress bar to be shown while auto mapping is performing
		mProgressBar = new ProgressBar(this);
		rLayout.addView(mProgressBar);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mProgressBar
				.getLayoutParams();
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mProgressBar.setLayoutParams(layoutParams);
		
		setContentView(rLayout);
		
		
	    // create floorplan
	      mFloorPlan = (FloorPlan) getLastCustomNonConfigurationInstance();
	      if (mFloorPlan == null){
	    	  
	    	  
	    	  if(escapePlanUri != null){
	    		  mFloorPlan = new FloorPlan();
	    		  mFloorPlan.setEscapePlanURI(escapePlanUri);
	    		  mFloorPlan.setFloorURI(floorUri);
	    		  mFloorPlan.setBuildingURI(intent
							.getStringExtra(ChooseLocationActivity.EXTRA_BUILDING_URI));
	    		  mFloorPlan.setFloorNumber(intent
										.getIntExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER, 0));

	    			new LoadPlanFromLODUM().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
	    	  }else {
	  	  		choosePlanDialogFragment = new ChoosePlanDialog();
				choosePlanDialogFragment.attachHandler(this);
				
				FloorPlan basicFloorPlan = new FloorPlan();
				basicFloorPlan.setBuildingURI(intent
						.getStringExtra(ChooseLocationActivity.EXTRA_BUILDING_URI));
				basicFloorPlan.setFloorNumber(intent
									.getIntExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER, 0));
				choosePlanDialogFragment.setBasicFloorPlan(basicFloorPlan);			
				choosePlanDialogFragment.show(getSupportFragmentManager(), "");	    		  
	    	  }

	      } else {
	    	  showPlan(mFloorPlan);
	      }
	      

	}
	
	/*
	 * this is important for handling orientation changes or if the activity loses the foreground due to e.g. phone calls
	 * since the floorPlan instance encapsulates all the data collected we pass it here to be loaded again later using 
	 * getLastCustomNonConfigurationInstance()
	 * 
	 * @see android.support.v4.app.FragmentActivity#onRetainCustomNonConfigurationInstance()
	 */
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
	    return mFloorPlan;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.select_feature, menu);
		return true;
	}
	
	/*
	 * called when actionbar is clicked
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected
	 * (android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_done:

			if (mSelectFeatureHandler.getPosition() != null) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra(EXTRA_COORDINATE_X,
						(int) mSelectFeatureHandler.getPosition().getX());
				resultIntent.putExtra(EXTRA_COORDINATE_Y,
						(int) mSelectFeatureHandler.getPosition().getY());
				resultIntent.putExtra(EXTRA_FEATURE_URI,
						mSelectFeatureHandler.getFeatureUri());
				resultIntent.putExtra(EXTRA_ESCAPE_PLAN_URI, mFloorPlan.getEscapePlanURI());
				resultIntent.putExtra(EXTRA_FLOOR_URI, mFloorPlan.getFloorURI());
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			} else
				Toast.makeText(this, "Please make a selection.",
						Toast.LENGTH_SHORT).show();

			return true;

		case R.id.action_cancel:

			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
			
		case R.id.action_settings:			
			new SettingsDialog().show(getSupportFragmentManager(), "");
			return true;

		}

		return true;

	}


	@Override
	public void onChoosePlanDialogPositiveClick(ChoosePlanDialog dialog) {

	    /*
	     * It might be that the async task of loading person names is still running.
	     * For this reason use .executeOnExecutor instead of .execute. This way we can have multiple async tasks at the same time.
	     */
		mFloorPlan = dialog.getSelectedPlan();
		new LoadPlanFromLODUM().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

	}

	@Override
	public void onChoosePlanDialogNegativeClick(ChoosePlanDialog dialog) {
		onBackPressed();
	}
	
	
	public void showPlan(FloorPlan floorPlan){

    	this.mFloorPlan = floorPlan;		
		mView.setImageUri(floorPlan.getCroppedFloorPlanImageUri());
		mView.setAction(ImageView.ACTION_LOAD_IMAGE_FROM_SERVER);
		mSelectFeatureHandler = new SelectTargetHandler(floorPlan, this, mFeatureType);
		mView.setTouchHandler(mSelectFeatureHandler);
		mProgressBar.setVisibility(View.GONE);
		
		if( mPosX!= -1 && mPosY != -1){
			mSelectFeatureHandler.selectObject(mPosX, mPosY);
		}
		
	}
	
	private class LoadPlanFromLODUM extends AsyncTask<String, Void, FloorPlan> {

        @Override
        protected FloorPlan doInBackground(String... params) {

			// load floorplan data
			RDFReader rdfReader = new RDFReader();
			rdfReader.getFloorPlan(mFloorPlan);
        	
        	
        	return mFloorPlan;
        }  
        

        @Override
		protected void onPostExecute(FloorPlan floorPlan) {
        	showPlan(floorPlan);

		}

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
        
	 }

}

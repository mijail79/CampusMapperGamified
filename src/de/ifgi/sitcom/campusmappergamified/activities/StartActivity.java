package de.ifgi.sitcom.campusmappergamified.activities;


import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.dialogs.SettingsDialog;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.io.InternalMappingTxtLog;
import de.ifgi.sitcom.campusmappergamified.io.RDFReader;


/*
 * 
 * Launched when the app starts, except in the first time. 
 * Provides the user with a short introduction text, 
 * an introduction video and buttons to either create a new dataset 
 * (calling ChooseLocationActivity) or to select an already existing, 
 * locally stored dataset (calling Activity to be implemented...).
 */
public class StartActivity extends SherlockFragmentActivity {

	private Button mLocalProjectButton;
	private InternalMappingTxtLog log = new InternalMappingTxtLog();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			if(MyCampusMapperGame.getInstance().isNewStart())
			{
				 log.appendLog("Gamified Applications starts at;"+ new Date());
			}

		setContentView(R.layout.activity_start);
        mLocalProjectButton = (Button) findViewById(R.id.button_local_project);
		if (hasLocalFloorPlan()) mLocalProjectButton.setVisibility(View.VISIBLE);
	}
	
	private boolean hasLocalFloorPlan(){
		return new RDFReader().localDataAvailable();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.start, menu);
		//	update score in the menu
	    SharedPreferences prefs = getSharedPreferences(MappingActivity.PREFS_LAST_LOCATION, 0);    
	  	SharedPreferences registerPref = getSharedPreferences("ActivityREGISTER", Context.MODE_PRIVATE);	
	       MyCampusMapperGame.getInstance().setMyScore(Integer.valueOf(prefs.getString(MappingActivity.PREFS_LAST_SCORE, "0")));
	       MyCampusMapperGame.getInstance().setPlayerEmail(registerPref.getString("playerEmail", ""));
	       MyCampusMapperGame.getInstance().setPlayerNick(registerPref.getString("playerNick", ""));

		 log.appendLog("Initial Score;"+MyCampusMapperGame.getInstance().getMyScore());
		 
		menu.findItem(R.id.action_score).setTitle(String.format("%04d", MyCampusMapperGame.getInstance().getMyScore()));
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		    switch (item.getItemId()) {
		    	
	          // add the new score button
			case R.id.action_score:
			    Intent intentLeaderboard = new Intent(this, LeaderboardActivity.class);
			    intentLeaderboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    startActivity(intentLeaderboard);
				return true;
				
			case R.id.action_settings:			
				new SettingsDialog().show(getSupportFragmentManager(), "");
				return true;


		    default:
		      break;
		    }

		    return true;
	}

	/** Called when the user clicks new project button */
	public void newProject(View v){
		launchBuildingInformationActivity();
	}
	
	/** Called when the user clicks local project button */
	public void localProject(View v){
		
		startMappingActivity(getLastLocation());
		
	}
	

	private FloorPlan getLastLocation(){
		
		FloorPlan floorPlan = new FloorPlan();

	       // Restore preferences
	       SharedPreferences prefs = getSharedPreferences(MappingActivity.PREFS_LAST_LOCATION, 0);
	       floorPlan.setBuildingName(prefs.getString(MappingActivity.PREFS_LAST_LOCATION_BUILDING_NAME, ""));
	       floorPlan.setBuildingURI(prefs.getString(MappingActivity.PREFS_LAST_LOCATION_BUILDING_URI, ""));
	       floorPlan.setFloorNumber(prefs.getInt(MappingActivity.PREFS_LAST_LOCATION_FLOOR_NUMBER, 0));
	       floorPlan.setSourceFloorPlanImageUri(Uri.parse(prefs.getString(MappingActivity.PREFS_LAST_LOCATION_SOURCE_URI, "")));
	       floorPlan.setCroppedFloorPlanImageUri(Uri.parse(prefs.getString(MappingActivity.PREFS_LAST_LOCATION_CROPPED_URI, "")));
	       floorPlan.setFromServer(prefs.getBoolean(MappingActivity.PREFS_LAST_LOCATION_FROM_SERVER, false));
	    // restore score
	       MyCampusMapperGame.getInstance().setMyScore(Integer.valueOf(prefs.getString(MappingActivity.PREFS_LAST_SCORE, "0")));
	   	
		return floorPlan;
	}

	 private void startMappingActivity(FloorPlan floorPlan){

		 Intent intent = new Intent(this, MappingActivity.class);
			intent.putExtra(ImageSourceActivity.EXTRA_SOURCE_URI, floorPlan.getSourceFloorPlanImageUri().toString());
			intent.putExtra(ImageSourceActivity.EXTRA_CROPPED_URI, floorPlan.getCroppedFloorPlanImageUri().toString());
			intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_NAME, floorPlan.getBuildingName());
			intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_URI, floorPlan.getBuildingURI());
			intent.putExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER, Integer.toString(floorPlan.getFloorNumber()));
			intent.putExtra(ChooseLocationActivity.EXTRA_LOAD_LOCAL_DATA, true);
			intent.putExtra(ChooseLocationActivity.EXTRA_LOAD_FROM_SERVER, floorPlan.isFromServer());
			
			startActivity(intent);
	 }
	
	public void launchBuildingInformationActivity() {

		Intent intent = new Intent(this, ChooseLocationActivity.class);
		startActivity(intent);
	}
	
}

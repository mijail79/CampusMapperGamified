package de.ifgi.sitcom.campusmappergamified.activities;

import java.util.ArrayList;
import java.util.Date;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.dialogs.SettingsDialog;
import de.ifgi.sitcom.campusmappergamified.game.PlayerAndBuilding;
import de.ifgi.sitcom.campusmappergamified.io.InternalMappingTxtLog;
import de.ifgi.sitcom.campusmappergamified.io.TripleStoreQueries;

public class OwnershipMapActivity extends SherlockFragmentActivity{

	private int mMenuID = R.menu.ownership;
	
	private MapView mMapView;
	private InternalMappingTxtLog log = new InternalMappingTxtLog();

	private LoadBuildingShapeFromLODUM mLoadBuildingShapeFromLODUM;

	public static final String EXTRA_BUILDING_NAME = "de.ifgi.sitcom.campusmapper.buildingName";
	public static final String EXTRA_LAT_E6 = "de.ifgi.sitcom.campusmapper.lat";
	public static final String EXTRA_LON_E6 = "de.ifgi.sitcom.campusmapper.lon";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ownership_map);
		
		//writes the log when user visits the conquest map
		log.appendLog("Visit conquest map;"+new Date());
		
		mMapView = (MapView) findViewById(R.id.mapview);
		mMapView.setMultiTouchControls(true);
		mMapView.getController().setZoom(14);

		float latMünster = 51.960000f; // in DecimalDegrees
		float lngMünster = 7.610000f; // in DecimalDegrees
		GeoPoint gpMünster = new GeoPoint((int) (latMünster * 1E6),
				(int) (lngMünster * 1E6));
		mMapView.getController().setCenter(gpMünster);

		/*
		 * It might be that the async task of loading person names is still
		 * running. For this reason use .executeOnExecutor instead of .execute.
		 * This way we can have multiple async tasks at the same time.
		 */
		mLoadBuildingShapeFromLODUM = new LoadBuildingShapeFromLODUM();
	//	mLoadBuildingShapeFromLODUM.executeOnExecutor(
	//			AsyncTask.THREAD_POOL_EXECUTOR, "");
		
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
	{
		mLoadBuildingShapeFromLODUM.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
	}
	else 
	{
		mLoadBuildingShapeFromLODUM.execute("");
	}

	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(mMenuID, menu);
		menu.findItem(R.id.action_score).setTitle(String.format("%04d", MyCampusMapperGame.getInstance().getMyScore()));
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

	      case android.R.id.home:
	    	  // app icon in action bar clicked; go home
	               // User clicked OK button
		          Intent intent = new Intent(this, MappingActivity.class);
		          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					setResult(Activity.RESULT_OK, intent);
					finish();
	          return true;
	          
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
	
	public static boolean isNetworkAvailable(Context context) 
	{
	    return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
	}
	
	private class LoadBuildingShapeFromLODUM extends AsyncTask<String, Void, ArrayList<PlayerAndBuilding>> {
	//	RDFReader myReader = new RDFReader();
		
		@Override
		protected ArrayList<PlayerAndBuilding> doInBackground(String... params) {
			return new TripleStoreQueries().queryBuildingsCenter();
		}
		
		@Override
		protected void onPostExecute(ArrayList<PlayerAndBuilding> result) {

			final ArrayList<OverlayItem> centerList = new ArrayList<OverlayItem>();
			Resources res = getResources();
			OverlayItem locationOverlayItem;
			for (PlayerAndBuilding myPB : result)
			{
				// draw building center
				if (myPB.getFlag().equals("ic_me_flag")) {
					Drawable d = res.getDrawable(R.drawable.ic_me_flag);
					locationOverlayItem = new OverlayItem(myPB.getBuildingId().toString(), myPB.getPlayerId().toString(), myPB.getCenter());
					locationOverlayItem.setMarker(d);
					centerList.add(locationOverlayItem);
				}
				else if(myPB.getFlag().equals("ic_hasowner_flag"))
				{
					Drawable d = res.getDrawable(R.drawable.ic_hasowner_flag);
					locationOverlayItem = new OverlayItem(myPB.getBuildingId().toString(), myPB.getPlayerId().toString(), myPB.getCenter());
					locationOverlayItem.setMarker(d);
					centerList.add(locationOverlayItem);					
				}
				else
				{
					Drawable d = res.getDrawable(R.drawable.ic_free_flag);
					locationOverlayItem = new OverlayItem(myPB.getBuildingId().toString(), myPB.getPlayerId().toString(), myPB.getCenter());
					locationOverlayItem.setMarker(d);
					centerList.add(locationOverlayItem);
				}
			}
            
			ItemizedIconOverlay<OverlayItem> centerOverlay = new ItemizedIconOverlay<OverlayItem>(
					OwnershipMapActivity.this, centerList, null);
			mMapView.getOverlays().add(centerOverlay);
			mMapView.invalidate();
		}
			
			@Override
			protected void onPreExecute() {
			}
			
			@Override
			protected void onProgressUpdate(Void... values) {
			}
		
		}

}

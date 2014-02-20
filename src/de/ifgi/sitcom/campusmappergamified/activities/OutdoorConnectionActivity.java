package de.ifgi.sitcom.campusmappergamified.activities;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.dialogs.SettingsDialog;
import de.ifgi.sitcom.campusmappergamified.io.TripleStoreQueries;
import de.ifgi.sitcom.campusmappergamified.outdoordata.Building;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class OutdoorConnectionActivity extends SherlockFragmentActivity {

	private MapView mMapView;
	private String mBuildingName;

	private LoadBuildingShapeFromLODUM mLoadBuildingShapeFromLODUM;
	private GestureDetector mGestureDetector;
	private ItemizedIconOverlay<OverlayItem> mEntrancePositionOverlay;
	private GeoPoint mEntrancePosition;

	public static final String EXTRA_BUILDING_NAME = "de.ifgi.sitcom.campusmapper.buildingName";
	public static final String EXTRA_LAT_E6 = "de.ifgi.sitcom.campusmapper.lat";
	public static final String EXTRA_LON_E6 = "de.ifgi.sitcom.campusmapper.lon";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entrance_location);

		Intent intent = getIntent();
		mBuildingName = intent.getStringExtra(EXTRA_BUILDING_NAME);

		mMapView = (MapView) findViewById(R.id.mapview);
		mMapView.setMultiTouchControls(true);
		mMapView.getController().setZoom(14);

		float latMünster = 51.966667f; // in DecimalDegrees
		float lngMünster = 7.633333f; // in DecimalDegrees
		GeoPoint gpMünster = new GeoPoint((int) (latMünster * 1E6),
				(int) (lngMünster * 1E6));
		mMapView.getController().setCenter(gpMünster);

		// add toch listener
		mGestureDetector = new GestureDetector(this, new SingleTapListener());
		mMapView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				Log.v("debug", "touch!");
				mGestureDetector.onTouchEvent(event);

				return false;
			}
		});

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
		getSupportMenuInflater().inflate(R.menu.entrance_location, menu);
		//	 update score in the menu
		menu.findItem(R.id.action_score).setTitle(String.format("%04d", MyCampusMapperGame.getInstance().getMyScore()));
		
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
			
			if (mEntrancePosition != null) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra(EXTRA_LAT_E6,
						(int) mEntrancePosition.getLatitudeE6());
				resultIntent.putExtra(EXTRA_LON_E6,
						(int) mEntrancePosition.getLongitudeE6());
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
	

	private class LoadBuildingShapeFromLODUM extends
			AsyncTask<String, Void, Building> {

		@Override
		protected Building doInBackground(String... params) {
			return new TripleStoreQueries().queryBuildingShape(mBuildingName);
		}

		@Override
		protected void onPostExecute(Building result) {
			// zoom to building center
			if (result.getCenter() != null) {
				mMapView.getController().setZoom(18);
				mMapView.getController().setCenter(result.getCenter());
			}

			// draw shape
			// only enters here if a building shape (polygon) exist
			if (result.getShape() != null && result.getShape().size() > 2) {
				PathOverlay pathOverlay = new PathOverlay(Color.BLUE,
						OutdoorConnectionActivity.this);
				for (GeoPoint p : result.getShape()) {
					pathOverlay.addPoint(p);
				}

				mMapView.getOverlays().add(pathOverlay);
			} else {
				// draw building center
				if (result.getCenter() != null) {
					OverlayItem locationOverlayItem = new OverlayItem(
							mBuildingName, "", result.getCenter());
					final ArrayList<OverlayItem> centerList = new ArrayList<OverlayItem>();
					centerList.add(locationOverlayItem);
					ItemizedIconOverlay<OverlayItem> centerOverlay = new ItemizedIconOverlay<OverlayItem>(
							OutdoorConnectionActivity.this, centerList, null);
					mMapView.getOverlays().add(centerOverlay);
				}

			}

		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

	}

	private class SingleTapListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {

			Log.v("debug", "single tap up");

			mEntrancePosition = (GeoPoint) mMapView.getProjection().fromPixels(
					e.getX(), e.getY());
			OverlayItem locationOverlayItem = new OverlayItem("Eingang", "",
					mEntrancePosition);

			// if overlay was not set yet, create it and add it to the map view
			if (mEntrancePositionOverlay == null) {
				final ArrayList<OverlayItem> centerList = new ArrayList<OverlayItem>();
				centerList.add(locationOverlayItem);
				mEntrancePositionOverlay = new ItemizedIconOverlay<OverlayItem>(
						OutdoorConnectionActivity.this, centerList, null);
				mMapView.getOverlays().add(mEntrancePositionOverlay);
			} else {
				// otherwiese remove old marker via remove all items and add new
				// one
				mEntrancePositionOverlay.removeAllItems();
				mEntrancePositionOverlay.addItem(locationOverlayItem);
			}

			// invalidate to force redrawing
			mMapView.invalidate();

			return true;

		}
	}
}

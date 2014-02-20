package de.ifgi.sitcom.campusmappergamified.activities;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.LoaderCallbackInterface;
//import org.opencv.android.OpenCVLoader;
//import org.opencv.android.Utils;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfPoint2f;
//import org.opencv.core.Point;
//import org.opencv.imgproc.Imgproc;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.dialogs.SettingsDialog;
import de.ifgi.sitcom.campusmappergamified.handler.cropping.CroppingHandler;
import de.ifgi.sitcom.campusmappergamified.views.ImageView;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

/*
 * Used for cropping and transformating the image. Uses an instance of imageView to show 
 * the image. Uses an instance of CroppingHandler to handle touch events and to draw the 
 * selection frame on top of the image view. After cropping the result is shown. User can 
 * undo the cropping, if the result was not satisfying. After completion MappingActivity 
 * is started.
 */
public class CroppingActivity extends SherlockFragmentActivity {

	private Uri mSourceImageUri;
	private Uri mCroppedImageUri;
	private ImageView mView;
	
	private String mBuildingName;
	private String mFloorNumber;
	private String mBuildingURI;
	ProgressDialog mProgressDialog;
	
	private RelativeLayout mRLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    // Get the imageURIe from the intent
	    Intent intent = getIntent();
	    String uriString = intent.getStringExtra(ImageSourceActivity.EXTRA_SOURCE_URI);
	    mSourceImageUri = Uri.parse(uriString);


		mBuildingName = intent.getStringExtra(ChooseLocationActivity.EXTRA_BUILDING_NAME);
		mBuildingURI = intent.getStringExtra(ChooseLocationActivity.EXTRA_BUILDING_URI);
		mFloorNumber = intent.getStringExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER);
	    
		
    	// create/ add image view: this view shows the floorplan image
		mView = (ImageView) getLastCustomNonConfigurationInstance();
		if (mView == null) {
			mView = new ImageView(this, mSourceImageUri, new CroppingHandler());
		    SharedPreferences prefs = getSharedPreferences(SettingsDialog.PREFS_SETTINGS, 0);
			mView.setmImSampleSize(prefs.getInt(SettingsDialog.PREFS_SETTINGS_IMAGE_SCALE_FACTOR, 2));
//	        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
		}
		mView.setAction(ImageView.ACTION_LOAD_IMAGE);
		mRLayout = new RelativeLayout(this);
		mRLayout.addView(mView);
		
		setContentView(mRLayout);
		
		// enable up button in action bar
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
			    			    	
	}

	
	
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		mRLayout.removeAllViews(); // mView cannot have a parent when used later
	    return mView;
	}
	
	
	/*
	 * 	prevent app from leaving the activity on back button press when cropping result is shown
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if (mView.getUIActionHandler().getMenu() == R.menu.transformation_result) {
		        

		    	((CroppingHandler) mView.getUIActionHandler()).undo();
				// reload image
				mView.setAction(ImageView.ACTION_LOAD_IMAGE);			
				// do not show undo button in action bar, i.e load other menu xml
				supportInvalidateOptionsMenu();
		    	
		        return true;
		    }			
		}

	    
	    
	    return super.onKeyDown(keyCode, event);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(mView.getUIActionHandler().getMenu(), menu);
		//	 update score in the menu
		menu.findItem(R.id.action_score).setTitle(String.format("%04d", MyCampusMapperGame.getInstance().getMyScore()));

		return true;
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
	    
		case R.id.action_ok_transformation:
			
			//do the image transformation
			imageTransformation();

			// update behaviour of handler
			mView.getUIActionHandler().handleMenuAction(item);
			
			// show undo button in action bar, i.e load other menu xml
			supportInvalidateOptionsMenu();
			return true;
			
		case R.id.action_ok_result:
			
			// indicate activity by showing progress dialog			
			mProgressDialog = ProgressDialog.show(this,
					"",
					"",
					true);
			
			// save image on sd card
			new AsyncImageSaving().execute("");
			return true;
			
		case R.id.action_undo:

			// update behaviour of handler
			mView.getUIActionHandler().handleMenuAction(item);
			
			// reload image
			mView.setAction(ImageView.ACTION_LOAD_IMAGE);
			
			// do not show undo button in action bar, i.e load other menu xml
			supportInvalidateOptionsMenu();
			return true;
			
		case R.id.action_settings:			
			new SettingsDialog().show(getSupportFragmentManager(), "");
			return true;

	    default:
	      break;
	    }

	    return true;
	  }
	
	
	/*
	 * launches MappingActivity
	 */
	public void launchMappingActivity() {
				
		// launch activity
		Intent intent = new Intent(this, MappingActivity.class);
		intent.putExtra(ImageSourceActivity.EXTRA_SOURCE_URI, mSourceImageUri.toString());
		intent.putExtra(ImageSourceActivity.EXTRA_CROPPED_URI, mCroppedImageUri.toString());
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_NAME, mBuildingName);
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_URI, mBuildingURI);
		intent.putExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER, mFloorNumber);
		startActivity(intent);
	}
	/*
	 * performs the image transformation
	 */
	private void imageTransformation (){

		
		/*
		 * 
		 * A---B 
		 * C---D
		 */
		
		Bitmap bitmap = mView.getBitmap();
//		Mat image = mView.getImage();
		CroppingHandler croppingTouchHandler = (CroppingHandler) mView.getUIActionHandler();
	    Matrix matrix = new Matrix();

	    int minX = Math.min((int)croppingTouchHandler.getCircleAX(), (int)croppingTouchHandler.getCircleCX());
	    int minY = Math.min((int)croppingTouchHandler.getCircleAY(), (int)croppingTouchHandler.getCircleBY());
	    int maxX = Math.max((int)croppingTouchHandler.getCircleBX(), (int)croppingTouchHandler.getCircleDX());
	    int maxY = Math.max((int)croppingTouchHandler.getCircleCY(), (int)croppingTouchHandler.getCircleDY());
	    int newWidth = maxX - minX;
	    int newHeight = maxY - minY;
	    

	    float[] src = new float[] {
	    		croppingTouchHandler.getCircleAX(),
	    		croppingTouchHandler.getCircleAY(),
	    		croppingTouchHandler.getCircleBX(),
	    		croppingTouchHandler.getCircleBY(),
	    		croppingTouchHandler.getCircleDX(),
	    		croppingTouchHandler.getCircleDY(),
	    		croppingTouchHandler.getCircleCX(),
	    		croppingTouchHandler.getCircleCY()
	    };


	    
	    int ab = (int)croppingTouchHandler.getCircleBX() - (int)croppingTouchHandler.getCircleAX();
	    int bd = (int)croppingTouchHandler.getCircleDY() - (int)croppingTouchHandler.getCircleBY();
	    int dc = (int)croppingTouchHandler.getCircleDX() - (int)croppingTouchHandler.getCircleCX();
	    int ca = (int)croppingTouchHandler.getCircleCY() - (int)croppingTouchHandler.getCircleAY();

	    float[] dst = new float[] {
		minX,
		minY,
        maxX,
        minY,
        maxX,
        maxY,
        minX,
        maxY
};
	    

	    // transform
	    matrix.setPolyToPoly(src, 0, dst, 0, 4);
	    bitmap = Bitmap.createBitmap(bitmap, minX, minY, newWidth, newHeight, matrix, true);
	    
	    //crop
	    int devLeft = Math.abs((int)croppingTouchHandler.getCircleAX() - (int)croppingTouchHandler.getCircleCX());
	    int devTop = Math.abs((int)croppingTouchHandler.getCircleAY() - (int)croppingTouchHandler.getCircleBY());
	    int devRight = Math.abs((int)croppingTouchHandler.getCircleBX() - (int)croppingTouchHandler.getCircleDX());
	    int devBottom = Math.abs((int)croppingTouchHandler.getCircleCY() - (int)croppingTouchHandler.getCircleDY());
	    bitmap = Bitmap.createBitmap(bitmap, devLeft, devTop, bitmap.getWidth() - devLeft - devRight, bitmap.getHeight() - devTop - devBottom);
	    mView.setBitmap(bitmap);

//		Utils.bitmapToMat(bitmap, image);		
	}
	
	/*
	 * class used to save the result image using separate thread
	 */
	 private class AsyncImageSaving extends AsyncTask<String, Void,  Boolean> {

         @Override
         protected Boolean doInBackground(String... params) {
     		String fullPath = Environment.getExternalStorageDirectory() + "/CampusMapper/";
    		
    		Log.v("debug", fullPath);

    		try {
    			File dir = new File(fullPath);
    			if (!dir.exists()) {
    				dir.mkdirs();
    			}

    			
    			// create name, i.e. current time and date
    			String fileName = "temp.png";
    			
    			OutputStream fOut = null;
    			File file = new File(fullPath, fileName);
    			file.createNewFile();
    			fOut = new FileOutputStream(file);

    			// 100 means no compression, the lower you go, the stronger the
    			// compression
    			mView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fOut);
    			fOut.flush();
    			fOut.close();

    			MediaStore.Images.Media.insertImage(getContentResolver(),
    					file.getAbsolutePath(), file.getName(), file.getName());


//    			croppedImageUri = Uri.fromFile(file);
    			mCroppedImageUri = Uri.parse(fullPath + fileName);
    			Log.v("cropped uri", mCroppedImageUri.toString());
    			
    			return true;

    		} catch (Exception e) {
    			Log.e("saveToExternalStorage()", e.getMessage());
    			return false;
    		}
         }      

         @Override
		protected void onPostExecute(Boolean success) {

 			// hide progress dialog
 			mProgressDialog.dismiss();
        	 
        	 
			// launch activity
			launchMappingActivity();
         }

         @Override
         protected void onPreExecute() {
         }

         @Override
         protected void onProgressUpdate(Void... values) {
         }
         
	 }

}

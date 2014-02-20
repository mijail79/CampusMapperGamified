package de.ifgi.sitcom.campusmappergamified.activities;




import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.dialogs.SettingsDialog;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

/*
 * Provides one button to access any existing camera app to be used for capturing 
 * the floorplan image. Provides another button to access any existing gallery app 
 * to select an already existing floorplan image. After image was selected/ created, 
 * Cropping Activity is called.
 */
public class ImageSourceActivity extends SherlockFragmentActivity {
	
	public final static String EXTRA_SOURCE_URI = "de.ifgi.sitcom.campusmapper.sourceURI";
	public final static String EXTRA_CROPPED_URI = "de.ifgi.sitcom.campusmapper.croppedURI";
	private String mImageUri;
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int ACCESS_GALLERY_ACTIVITY_REQUEST_CODE = 200;

	private String mBuildingName;
	private String mBuildingURI;
	private String mFloorNumber;
	
	private RadioButton mRadioButtonCamera;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_image_source);
		
		// get extras from intent
		Intent intent = getIntent();
		mBuildingName = intent.getStringExtra(ChooseLocationActivity.EXTRA_BUILDING_NAME);
		mBuildingURI = intent.getStringExtra(ChooseLocationActivity.EXTRA_BUILDING_URI);
		mFloorNumber = intent.getStringExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER);
		
		// enable up button in action bar
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		
		mRadioButtonCamera = (RadioButton) findViewById(R.id.radio_camera);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.image_source, menu);
		//	 update score in the menu
		menu.findItem(R.id.action_score).setTitle(String.format("%04d", MyCampusMapperGame.getInstance().getMyScore()));
		
		return true;
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    
//		case R.id.action_crop:
//			//do something when this button is pressed
//			crop();
//			return true;
//			
//		case R.id.action_map:
//			//do something when this button is pressed
//			map();
//			return true;
			
		case R.id.action_next:

			// if camera radio button is checked, call camera
			if(mRadioButtonCamera.isChecked()) showCamera(null);
			// otherwise gallery
			else showGallery(null);
			return true;
	    
	      case android.R.id.home:
	          // app icon in action bar clicked; go home
	          Intent intent = new Intent(this, StartActivity.class);
	          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	          startActivity(intent);
	          return true;
	          
			case R.id.action_settings:			
				new SettingsDialog().show(getSupportFragmentManager(), "");
				return true;
	    
	    default:
	      break;
	    }

	    return true;
	  }
	
	
	/** Called when the user clicks the Camera button */
	public void showCamera(View view) {
		
//		imageUri = getOutputMediaFileUri().toString();
//		Log.v("image uri camera", imageUri);
//		imageUri = getOutputMediaFileUri();
		
		// call camera app and take photo, afterwards onActivityResult is called
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	 	cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getOutputMediaFileUri()); // set the image file name			
        startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        
	}
	
	/** Called when the user clicks the Gallery button */
	public void showGallery(View view) {
		
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, ACCESS_GALLERY_ACTIVITY_REQUEST_CODE);
		
	}
		
	/** Called when the user clicks the hidden crop button
	 * this is just a shortcut to be used while developing to directly access the mapping activity
	 *  
	 *  TODO remove this function and its menu item
	 *  */
	public void crop() {
		
//		imageUri = Uri.parse("content://media/external/images/media/7808");
		
		Intent intent = new Intent(this, CroppingActivity.class);

		intent.putExtra(ImageSourceActivity.EXTRA_SOURCE_URI, getRealPathFromURI(Uri.parse("content://media/external/images/media/7808")));
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_NAME, mBuildingName);
		intent.putExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER, mFloorNumber);
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_URI, mBuildingURI);
		startActivity(intent);
	}

	
	/** Called when the user clicks the hidden map button
	 * this is just a shortcut to be used while developing to directly access the mapping activity
	 *  
	 *  TODO remove this function and its menu item
	 *  */
	public void map() {
		
		// launch activity
		Intent intent = new Intent(this, MappingActivity.class);
		intent.putExtra(ImageSourceActivity.EXTRA_SOURCE_URI, getRealPathFromURI(Uri.parse("content://media/external/images/media/7808")));
		intent.putExtra(ImageSourceActivity.EXTRA_CROPPED_URI, getRealPathFromURI(Uri.parse("content://media/external/images/media/7808")));
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_NAME, mBuildingName);
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_URI, mBuildingURI);
		intent.putExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER, mFloorNumber);
		startActivity(intent);
	}
	
	// called after returning from camera app or gallery
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    super.onActivityResult(requestCode, resultCode, data);
	    
		Log.v("debug", "Request Code is " + requestCode);
	    if (resultCode == RESULT_OK){

	    	
	    	switch (requestCode) {
	    	
	    	case ACCESS_GALLERY_ACTIVITY_REQUEST_CODE:
	    		Uri galleryImageUri = data.getData();
	    		mImageUri = getRealPathFromURI(galleryImageUri);

	    		Log.v("image uri on gallery result", mImageUri);
//	    		imageUri =  data.getData();
	    		
		        break;
		    	
	    	case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
	    		
	    		Log.v("image uri on camera result", mImageUri);
	    		
	    		
	    		
	    		break;
	    	
	    	}
	    	
//	        Log.v("debug", "External Gallery Image URI is " + imageUri.toString());	    	
			
			// start buildingInformationActivity
			launchTransformationActivity();
	    	
	    } else {
//	    	Toast.makeText(this, "no image chosen", Toast.LENGTH_SHORT).show();
	    }
	}
	

	// TODO may throw out of bounds on old android
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	

	
	public void launchTransformationActivity() {
		Intent intent = new Intent(this, CroppingActivity.class);
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_NAME, mBuildingName);
		intent.putExtra(ChooseLocationActivity.EXTRA_BUILDING_URI, mBuildingURI);
		intent.putExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER, mFloorNumber);
		intent.putExtra(EXTRA_SOURCE_URI, mImageUri.toString());
		startActivity(intent);
	}
	
	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri(){
	      return Uri.fromFile(getOutputMediaFile());
	}

	/** Create a File for saving an image or video */
	private File getOutputMediaFile(){

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "Floor Plans");

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.v("debug", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(new Date());
	    File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
	    mImageUri = mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg";
        Log.v("image uri after get output file", mImageUri);
        
	    return mediaFile;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    if (mImageUri != null) {
	        outState.putString("cameraImageUri", mImageUri.toString());
	    }
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    if (savedInstanceState.containsKey("cameraImageUri")) {
	    	mImageUri = savedInstanceState.getString("cameraImageUri");
//	    	imageUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
	    }
	}

}

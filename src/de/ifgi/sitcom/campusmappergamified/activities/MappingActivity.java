package de.ifgi.sitcom.campusmappergamified.activities;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.dialogs.AutoMapDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.AutoMapDialog.AutoMappingDialogListener;
import de.ifgi.sitcom.campusmappergamified.dialogs.SettingsDialog;
import de.ifgi.sitcom.campusmappergamified.exceptions.UploadException;
import de.ifgi.sitcom.campusmappergamified.game.ScoreManager;
import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import de.ifgi.sitcom.campusmappergamified.handler.mapping.AutoMapHandler;
import de.ifgi.sitcom.campusmappergamified.handler.mapping.CorridorHandler;
import de.ifgi.sitcom.campusmappergamified.handler.mapping.DoorHandler;
import de.ifgi.sitcom.campusmappergamified.handler.mapping.EditHandler;
import de.ifgi.sitcom.campusmappergamified.handler.mapping.EntranceHandler;
import de.ifgi.sitcom.campusmappergamified.handler.mapping.RoomHandler;
import de.ifgi.sitcom.campusmappergamified.handler.mapping.StairsHandler;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.indoordata.Person;
import de.ifgi.sitcom.campusmappergamified.io.ImageUpload;
import de.ifgi.sitcom.campusmappergamified.io.InternalMappingTxtLog;
import de.ifgi.sitcom.campusmappergamified.io.RDFDeleteAndUpdate;
import de.ifgi.sitcom.campusmappergamified.io.RDFReader;
import de.ifgi.sitcom.campusmappergamified.io.RDFUpload;
import de.ifgi.sitcom.campusmappergamified.io.RDFWriter;
import de.ifgi.sitcom.campusmappergamified.io.TripleStoreQueries;
import de.ifgi.sitcom.campusmappergamified.views.ImageView;
//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.LoaderCallbackInterface;
//import org.opencv.android.OpenCVLoader;
//import de.ifgi.sitcom.campusmappergamified.handler.mapping.EntranceHandler;

/*
 * This is where all mapping activities take place. Uses an instance of imageView 
 * for showing the image. Depending on the current state one of the handlers found 
 * in package handler.mapping is given to the image view, to handle touch events 
 * or other user actions. On start EditHandler is active. When the user clicks the 
 * plus button depending on which type is selected the referring handler gets 
 * activated (e.g. if corridor is selected, CorridorHandler will be activated).
 */
public class MappingActivity extends SherlockFragmentActivity implements AutoMappingDialogListener{

	private ImageView mView;
	private int mMenuID = R.menu.mapping;
	private UIActionHandler mCurrentTouchHandler;
	private EditHandler mEditTouchHandler;
	private AutoMapHandler mAutoMappingTouchHandler;
	private static Handler mHandler;
	private FloorPlan mFloorPlan;
	private ProgressBar mProgressBar;
	
	private Uri mCroppedImageUri;
	private Uri mSourceImageUri;

	
	public static final int MESSAGE_SHOW_EDIT = 1;
	public static final int MESSAGE_SELECT_CORRIDOR = 2;
	public static final int MESSAGE_SELECT_ENTRANCE = 3;
	public static final int MESSAGE_SELECT_STAIRS = 4;
	public static final int MESSAGE_SELECT_ROOM = 5;
	public static final int MESSAGE_SELECT_DOOR = 6;
	public static final int MESSAGE_UPLOAD_SUCCESS = 7;
	public static final int MESSAGE_UPLOAD_FAILURE = 8;
	
	private static final String IMAGE_SERVER_URI = "http://giv-lodum.uni-muenster.de/images/";

	public final static String PREFS_LAST_LOCATION = "de.ifgi.sitcom.campusmapper.lastLocation";
	public final static String PREFS_LAST_LOCATION_BUILDING_NAME = "buildingName";
	public final static String PREFS_LAST_LOCATION_FLOOR_NUMBER = "floorNumber";
	public final static String PREFS_LAST_LOCATION_BUILDING_URI = "buildingURI";
	public final static String PREFS_LAST_LOCATION_CROPPED_URI = "croppedURI";
	public final static String PREFS_LAST_LOCATION_SOURCE_URI = "sourceURI";
	public final static String PREFS_LAST_LOCATION_FROM_SERVER = "fromServer";
	public final static String PREFS_LAST_SCORE = "myScore";
	
	private InternalMappingTxtLog log = new InternalMappingTxtLog();
			
    @Override
    protected void onStop(){
       super.onStop();

       // save floorplan in file
		// generate RDF
		RDFWriter rdfCreater = new RDFWriter();
		rdfCreater.floorPlanToRDF(mFloorPlan); 
		ScoreManager myScorePerBuilding = new ScoreManager(getApplicationContext());

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences prefs = getSharedPreferences(PREFS_LAST_LOCATION, 0);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putString(PREFS_LAST_LOCATION_BUILDING_NAME, mFloorPlan.getBuildingName());
      editor.putString(PREFS_LAST_LOCATION_BUILDING_URI, mFloorPlan.getBuildingURI());
      editor.putInt(PREFS_LAST_LOCATION_FLOOR_NUMBER, mFloorPlan.getFloorNumber());
      editor.putString(PREFS_LAST_LOCATION_CROPPED_URI, mCroppedImageUri.toString());
      editor.putString(PREFS_LAST_LOCATION_SOURCE_URI, mSourceImageUri.toString());
      editor.putBoolean(PREFS_LAST_LOCATION_FROM_SERVER, mFloorPlan.isFromServer());
      // save score in preferences
      editor.putString(PREFS_LAST_SCORE, MyCampusMapperGame.getInstance().getMyScore().toString());
      myScorePerBuilding.setScorePerBuilding(mFloorPlan.getBuildingURI());
      

      // Commit the edits!
      editor.commit();
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get intent to receive passed key value pairs
		Intent intent = getIntent();
	  	boolean fromServer = intent.getBooleanExtra(ChooseLocationActivity.EXTRA_LOAD_FROM_SERVER, false);
	  	boolean localData = intent.getBooleanExtra(ChooseLocationActivity.EXTRA_LOAD_LOCAL_DATA, false);
	    
	  	SharedPreferences registerPref = getSharedPreferences("ActivityREGISTER", Context.MODE_PRIVATE);	
        MyCampusMapperGame.getInstance().setPlayerNick(registerPref.getString("playerNick", ""));
        MyCampusMapperGame.getInstance().setPlayerName(registerPref.getString("playerName", ""));
        MyCampusMapperGame.getInstance().setPlayerEmail(registerPref.getString("playerEmail", ""));
	  	
		// create floorplan
		mFloorPlan = (FloorPlan) getLastCustomNonConfigurationInstance();
		if (mFloorPlan == null) {

			mFloorPlan = new FloorPlan();
			mFloorPlan
					.setBuildingName(intent
							.getStringExtra(ChooseLocationActivity.EXTRA_BUILDING_NAME));
			mFloorPlan.setBuildingURI(intent
					.getStringExtra(ChooseLocationActivity.EXTRA_BUILDING_URI));
			mFloorPlan.setFromServer(fromServer);
			try {
				mFloorPlan
						.setFloorNumber(Integer.parseInt(intent
								.getStringExtra(ChooseLocationActivity.EXTRA_FLOOR_NUMBER)));
			} catch (NumberFormatException e) {
				Log.e("debug", "invalid floornumber");

			}
			if (fromServer || localData) {
				// load data from file
				
				RDFReader rdfReader = new RDFReader();
				mFloorPlan = rdfReader.loadFloorPlanFromSD(mFloorPlan);
			
			}
		}

	    //write the log
		 log.appendLog("Building;"+mFloorPlan.getBuildingName());
		 log.appendLog("Floor Number;"+mFloorPlan.getFloorNumber());
		
		// Get the croppedImageURIe from the intent
		String uriString = intent
				.getStringExtra(ImageSourceActivity.EXTRA_CROPPED_URI);
		mCroppedImageUri = Uri.parse(uriString);
		Log.v("croppedURI", uriString);
		
		// Get the sourceImageURIe from the intent
		uriString = intent
				.getStringExtra(ImageSourceActivity.EXTRA_SOURCE_URI);
		mSourceImageUri = Uri.parse(uriString);
		Log.v("sourceURI", uriString);

		// create/ add image view: this view shows the floorplan image
		mView = new ImageView(this, mCroppedImageUri, null);
		RelativeLayout rLayout = new RelativeLayout(this);
		rLayout.addView(mView);
		
		// show the layout
		setContentView(rLayout);
		
		// enable up button in action bar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		/*
		 * create Handler
		 * 
		 * we need this to modify the UI by sending messages from other threads
		 * 
		 */
	    mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	
	        	
	        	switch (msg.what) {
				case MESSAGE_SHOW_EDIT:
		        	startEditMode();	
					return;
				case MESSAGE_SELECT_CORRIDOR:
					getSupportActionBar().getTabAt(0).select();
					updateActionBar(R.menu.mapping_corridor_selected);
					break;
				case MESSAGE_SELECT_ROOM:
					getSupportActionBar().getTabAt(1).select();
					updateActionBar(R.menu.mapping_room_selected);
					break;
				case MESSAGE_SELECT_DOOR:
					getSupportActionBar().getTabAt(2).select();
					updateActionBar(R.menu.mapping_door_selected);
					break;
				case MESSAGE_SELECT_ENTRANCE:
					getSupportActionBar().getTabAt(3).select();					
					updateActionBar(R.menu.mapping_entrance_selected);
					break;
				case MESSAGE_SELECT_STAIRS:
					getSupportActionBar().getTabAt(4).select();
					updateActionBar(R.menu.mapping_stairs_selected);
					break;
				case MESSAGE_UPLOAD_SUCCESS:
 		        	 showUploadSuccesDialog();
					break;
				case MESSAGE_UPLOAD_FAILURE:
 		        	 Toast.makeText(
 							MappingActivity.this,
 							getString(R.string.submit_failure), Toast.LENGTH_SHORT).show();
					break;
				}
	        	
	        }

	      };

	      	    
		// create Handler
		mEditTouchHandler = new EditHandler(mFloorPlan, this);
	    mAutoMappingTouchHandler = new AutoMapHandler(mFloorPlan, this);

		// add tabs
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setTitle("");

	    // corridor
	    Tab tab = actionBar.newTab().setText("Corridor").setTabListener(new TabListener(new CorridorHandler(mFloorPlan, this)));
	    actionBar.addTab(tab);

	    // room
	    tab = actionBar.newTab().setText("Room").setTabListener(new TabListener(new RoomHandler(mFloorPlan, this)));
	    actionBar.addTab(tab);

	    // door
	    tab = actionBar.newTab().setText("Door").setTabListener(new TabListener(new DoorHandler(mFloorPlan, this)));
	    actionBar.addTab(tab);
	    
	    // entrance/ exit
	    tab = actionBar.newTab().setText("Entry").setTabListener(new TabListener(new EntranceHandler(mFloorPlan, this)));
	    actionBar.addTab(tab);
	    
	    // stairs/ elevator
	    tab = actionBar.newTab().setText("Stairs").setTabListener(new TabListener(new StairsHandler(mFloorPlan, this)));
	    actionBar.addTab(tab);
	    
	    
	    // submit
	    tab = actionBar.newTab().setText("").setTabListener(new TabListener(null) {
			
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			}
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				submit();	
			}
			
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {				
			}
		});
	    tab.setIcon(R.drawable.ic_action_upload);
	    actionBar.addTab(tab);

	    
	    /*
	     *  load person names from triple store
	     *  needs to be done asynchronously since we can not make http request from the main thread
	     */
		if (mFloorPlan.getPersonNames() == null) {

			// load in async task
			if (isNetworkAvailable(MappingActivity.this))
				new LoadFromLODUM().execute("");
		}
	}

	/*
	 * checks if we have an internet connection
	 */
	public static boolean isNetworkAvailable(Context context) {
		return ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo() != null;
	}
	
	private void showUploadSuccesDialog(){
		// show dialog to make sure the user wants to leave
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.submit_success)
		       .setTitle(R.string.title_submit_succes);
		//writes the log
		 log.appendLog("Final Building Score;"+MyCampusMapperGame.getInstance().getMybuildingScore());
		 log.appendLog("Final Score;"+MyCampusMapperGame.getInstance().getMyScore());
		 log.appendLog("Gamified Applications finished at;"+ new Date());
		 MyCampusMapperGame.getInstance().setNewStart(false);
		// Add the buttons
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User clicked OK button
		        	   // go to start activity
				          Intent intent = new Intent(MappingActivity.this, StartActivity.class);
				          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				          // save preferences before going to start activity
				          onStop();
				          startActivity(intent);
		           }
		       });
		AlertDialog dialog = builder.create();
		dialog.show();
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
		getSupportMenuInflater().inflate(mMenuID, menu);
	//	update score in the menu
		menu.findItem(R.id.action_score).setTitle(String.format("%04d", MyCampusMapperGame.getInstance().getMyScore()));
		return true;
	}

	/*
	 * called when actionbar is clicked
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

	      case android.R.id.home:
	    	  // app icon in action bar clicked; go home
	               // User clicked OK button
		          Intent intent = new Intent(MappingActivity.this, StartActivity.class);
		          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		          // call onStop in order to save preferences in a local variable before going back.
		          this.onStop();	
		          startActivity(intent);
	          return true;
			
          // add the new score button
		case R.id.action_score:
		    Intent intentLeaderboard = new Intent(this, LeaderboardActivity.class);
		    intentLeaderboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(intentLeaderboard);
			return true;
			
		case R.id.action_new:
			updateActionBar(mCurrentTouchHandler.getMenu());
			mView.setTouchHandler(mCurrentTouchHandler);
			
			return true;
			
		case R.id.action_cancel:
			startEditMode();
			return true;
			
		case R.id.action_next:
			nextAction();
			return true;
			
		case R.id.action_settings:			
			new SettingsDialog().show(getSupportFragmentManager(), "");
			return true;
			
		default:

			// events that we can not handle here, we give to the current handler
			mView.getUIActionHandler().handleMenuAction(item);
			break;
		}

		return true;
	}
	
	private void nextAction(){
		int tabIndex = getSupportActionBar().getSelectedNavigationIndex();
		tabIndex ++;
		
		getSupportActionBar().getTabAt(tabIndex).select();

	}


	private void submit(){
		
  		AlertDialog.Builder builderRDF = new AlertDialog.Builder(this);
  		builderRDF.setMessage(R.string.description_submit)
  		       .setTitle(R.string.title_submit);
  		// Add the buttons
  		builderRDF.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
  		           public void onClick(DialogInterface dialog, int id) {

						// User clicked OK button

						// show progress dialog
						final ProgressDialog progressDialog = ProgressDialog
								.show(MappingActivity.this,
										"",
										getString(R.string.title_progress_submit_indoor_data),
										true);
  		        	   
  		        	   // start new thread for uploading indoor data and
						// images
						new Thread(new Runnable() {
						//	@Override
							public void run() {

								try {
									
									if (!mFloorPlan.isFromServer()){
										
										// create file name for the images, i.e.
										// current time and date
										SimpleDateFormat formatter = new SimpleDateFormat(
												"yyyy_MM_dd_HH_mm_ss",
												Locale.GERMANY);
										Date now = new Date();
										String fileNameSource = "source_"
												+ formatter.format(now) + ".png";
										String fileNameCropped = "cropped_"
												+ formatter.format(now) + ".png";
										
										mFloorPlan.setSourceFloorPlanImageUri(Uri.parse(IMAGE_SERVER_URI + fileNameSource));
										mFloorPlan.setCroppedFloorPlanImageUri(Uri.parse(IMAGE_SERVER_URI + fileNameCropped));
										mFloorPlan.setId(formatter.format(now));
										
										// upload image 1
										ImageUpload imageUpload = new ImageUpload();
										imageUpload.uploadFile(
												mSourceImageUri.getPath(),
												fileNameSource);

										// upload image 2
										imageUpload.uploadFile(
												mCroppedImageUri.getPath(),
												fileNameCropped);										
									} else {
										// delete old data
										// TODO
									}


									// upload indoor data
									// generate RDF
									RDFWriter rdfCreater = new RDFWriter();
									final String rdfString = rdfCreater.floorPlanToRDF(mFloorPlan); 

									Log.v("rdfString", rdfString);
									
									//  upload the new scores per person per building
									RDFUpload rDFUpload = new RDFUpload();	
									RDFDeleteAndUpdate rDFDeleteAndUpload =  new RDFDeleteAndUpdate();
									//upload the floorplan
									rDFUpload.uploadRDF(rdfString);
									// upload n-ary relationship between buildings and players
									rDFUpload.uploadRDF(getRDFStringScorePerBuilding(mFloorPlan.getBuildingURI()));
									// Update the total score of a player.
									rDFDeleteAndUpload.DeleteAndUpdateRDF(getRDFStringTotalScore());
									
									mCroppedImageUri = mFloorPlan.getCroppedFloorPlanImageUri();
									mSourceImageUri = mFloorPlan.getSourceFloorPlanImageUri();
									mFloorPlan.setFromServer(true);
									
									// show success toast and go back to start activity
									MappingActivity.mHandler
											.sendEmptyMessage(MESSAGE_UPLOAD_SUCCESS);

								} catch (UploadException e) {
									e.printStackTrace();
									// show failure toast
									MappingActivity.mHandler
											.sendEmptyMessage(MESSAGE_UPLOAD_FAILURE);
								}
								
								// hide progress dialog
								progressDialog.dismiss();

							}
						}).start();	  		        	   
  		        	   
  		           }
  		       });
  		builderRDF.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
  		           public void onClick(DialogInterface dialog, int id) {
  		               // User cancelled the dialog
  		        	   
  		        	   // go back to last step (stairs)
  		        	 getSupportActionBar().getTabAt(4).select();
  		           }
  		       });
  		AlertDialog dialogRDF = builderRDF.create();
  		dialogRDF.show();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();

		if (mFloorPlan.isFromServer())
			mView.setAction(ImageView.ACTION_LOAD_IMAGE_FROM_SERVER);
		else
			mView.setAction(ImageView.ACTION_LOAD_IMAGE);	
		
//		// laod opencv library using opencv manager
//		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this,
//				mLoaderCallback);
	}
	
	/*
	 * Gets the RDF string that will be submitted to the DB to store n-ary relation
	 * between players and score per building
	 */
	public String getRDFStringScorePerBuilding (String buildingURICode)
	{
		String scorePerBuilgingRDF = 
		"<indoor:"+ MyCampusMapperGame.getInstance().getPlayerEmail() + "> indoor:hasRelationToBuilding <indoor:"+ MyCampusMapperGame.getInstance().getPlayerEmail() + buildingURICode.substring(buildingURICode.lastIndexOf("/") + 1) + "> . " +
		"<indoor:"+ MyCampusMapperGame.getInstance().getPlayerEmail() + buildingURICode.substring(buildingURICode.lastIndexOf("/") + 1)  + "> rdf:type prv:BuildingScoreRelation .	" +	
		"<indoor:"+ MyCampusMapperGame.getInstance().getPlayerEmail() + buildingURICode.substring(buildingURICode.lastIndexOf("/") + 1)  + "> indoor:hasBuilding '" + buildingURICode.substring(buildingURICode.lastIndexOf("/") + 1) + "' . " +
		"<indoor:"+ MyCampusMapperGame.getInstance().getPlayerEmail() + buildingURICode.substring(buildingURICode.lastIndexOf("/") + 1)  + "> indoor:hasPlayerScore '" + MyCampusMapperGame.getInstance().getMybuildingScore() + "'^^xsd:integer .	";

		return scorePerBuilgingRDF;
	}
	
	
	/*
	 * Gets the RDF string that will be submitted to the DB to update total score of a player
	 */
	public String getRDFStringTotalScore ()
	{
		String totalScore = 
			"DELETE " +
			 " { " +
			  "  GRAPH <http://data.uni-muenster.de/context/indoormapping> " +
			  "    { " +
				"	<indoor:" + MyCampusMapperGame.getInstance().getPlayerEmail() + "> rdf:type prv:Player . " +
				"	<indoor:" + MyCampusMapperGame.getInstance().getPlayerEmail() + "> indoor:hasTotalPlayerScore ?x . " +
			    "  } " +
			"} " +
			"INSERT { " +
			"	GRAPH <http://data.uni-muenster.de/context/indoormapping> { " +   
			"		<indoor:" + MyCampusMapperGame.getInstance().getPlayerEmail() + "> rdf:type prv:Player . " +
			"		<indoor:" + MyCampusMapperGame.getInstance().getPlayerEmail() + "> indoor:hasTotalPlayerScore '" + MyCampusMapperGame.getInstance().getMyScore() + "'^^xsd:integer . " +					  
			"		} " +
			"	} " +
			"	WHERE " +
			"	{ " +
			"	GRAPH <http://data.uni-muenster.de/context/indoormapping> " +
			"		{   " +
			"		<indoor:" + MyCampusMapperGame.getInstance().getPlayerEmail() + "> rdf:type prv:Player . " +
			"		<indoor:" + MyCampusMapperGame.getInstance().getPlayerEmail() + "> indoor:hasTotalPlayerScore ?x ." +
			"		} " +
			"	}" ;
		
		return totalScore;
	}	

	
	/*
	 * changes the actionbar´s content
	 */
	public void updateActionBar(int menu){
		mMenuID = menu;
		supportInvalidateOptionsMenu();
	}
	
	/*
	 * activate edit handler
	 */
	private void startEditMode(){
		
    	updateActionBar(mEditTouchHandler.getMenu());
    	mView.setTouchHandler(mEditTouchHandler);
	}
	
	/*
	 * activate edit handle from concurrent thread
	 */
	public void triggerEditMode(){
		mHandler.sendEmptyMessage(MESSAGE_SHOW_EDIT);
	}
	
	/*
	 * send message to handler defined in onCreate
	 * basically used to activate another UIHandler such as CorridorHandler
	 */
	public void sendHandlerMessage (int m){
		mHandler.sendEmptyMessage(m);
	}
	
	/*
	 * listens to events in the actionbar´s tabbar
	 */
    class TabListener implements ActionBar.TabListener {
        
    	private final UIActionHandler touchHandler;

    	
        public TabListener(UIActionHandler touchHandler) {
        	this.touchHandler = touchHandler;        	
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
        	
        	mCurrentTouchHandler = touchHandler;
        	
        	
    		if (mView.getUIActionHandler() == null || 
    				mView.getUIActionHandler().getClass() != AutoMapHandler.class) {
    			startEditMode();
//    			MappingActivity.this.getSupportActionBar().setSubtitle(titleID);
    		}

        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }

    }

	@Override
	public void onAutoMappingDialogPositiveClick(
			AutoMapDialog dialog) {
		
		updateActionBar(mAutoMappingTouchHandler.getMenu());
    	mView.setTouchHandler(mAutoMappingTouchHandler);
    	mProgressBar.setVisibility(View.VISIBLE);
    	mProgressBar.setProgress(60);


	}

	@Override
	public void onAutoMappingDialogNegativeClick(
			AutoMapDialog dialog) {
		// do nothing
		
	}
	
	/*
	 * show or hide the progressbar
	 */
	public void setProgressBarVisibility(int visibility){
		mProgressBar.setVisibility(visibility);
	}
	
	/*
	 * class used to asynchronously accessing LODUM´s triple store to get the names of people working in this building
	 */
	private class LoadFromLODUM extends AsyncTask<String, Void, ArrayList<Person>> {

        @Override
        protected ArrayList<Person> doInBackground(String... params) {

              return new TripleStoreQueries().queryPersons(mFloorPlan.getBuildingName());
        }      

        @Override
		protected void onPostExecute(ArrayList<Person> persons) {

			if (persons == null) {
				return;
			}

//			Log.v("debug", "post execute");
			Log.v("person names", "result length " + persons.size());

			mFloorPlan.getBuilding().setPersons(persons);
			mFloorPlan.setPersonNames(personsToString(persons));
		}
        
        private String [] personsToString(ArrayList<Person> persons){
          	 
          	 String [] personNames = new String [persons.size()];

          	 for (int i = 0; i < persons.size(); i++){
          		 personNames[i] = persons.get(i).getName();
          	 }
          	 
          	 return personNames;
           }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
        
	 }
 

}

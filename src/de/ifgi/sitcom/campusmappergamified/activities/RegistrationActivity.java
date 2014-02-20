package de.ifgi.sitcom.campusmappergamified.activities;


import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.io.InternalMappingTxtLog;
import de.ifgi.sitcom.campusmappergamified.player.Player;


/*
 * 
 * Launched when the app starts for the first time. Asks the user to 
 * provide a username that will be used along the application and saved locally
 * and in the database.
 */
public class RegistrationActivity extends SherlockFragmentActivity {

	Player myPlayer;
	EditText usernameText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    SharedPreferences registerPref = getSharedPreferences("ActivityREGISTER", Context.MODE_PRIVATE);	
	    	if(registerPref.getBoolean("register_executed", false)){
				Intent intent = new Intent(this, StartActivity.class);
		//		new registerLog().execute(this);
				startActivity(intent);
				finish();
		    } else {
		        Editor myEditor = registerPref.edit();		    	
		        myEditor.putBoolean("register_executed", true);
		        myEditor.commit();
		    }
		setContentView(R.layout.activity_registration);
	}
	

	/** Called when the user clicks go button */
	public void goStart(View v){		
		new RegisterUserDB().execute(this);
		Intent intent = new Intent(this, StartActivity.class);
	//	new registerLog().execute(this);
		startActivity(intent);
		finish();
	}

	
	/*
	 * class used to retrieve score from DB and save the result using separate thread
	 */
	private class RegisterUserDB extends AsyncTask<Activity, Void, String>
	{
		 @Override
	        protected String doInBackground(Activity... params) {
			 //
			    SharedPreferences registerPref = getSharedPreferences("ActivityREGISTER", Context.MODE_PRIVATE);	
		        Editor myEditor = registerPref.edit();	

				usernameText = (EditText)findViewById(R.id.textview_username);
				if (usernameText.getText().toString().trim().length() > 0)
				{
					myPlayer = new Player(usernameText.getText().toString().trim());
					String playerEmail = myPlayer.registerUser();
					myEditor.putString("playerNick", usernameText.getText().toString());
					myEditor.putString("playerEmail", playerEmail);
			        myEditor.commit();					
				}
				return null;
	        }  
	        
	        @Override
	        protected void onPreExecute() {
	        }

	        @Override
	        protected void onProgressUpdate(Void... values) {
	        }
	        
	}
	
	/*
	 * class used to save a log file with activities performed by the user
	 */
/*	private class registerLog extends AsyncTask<Activity, Void, String>
	{
		 @Override
	        protected String doInBackground(Activity... params) {
			 //
			 InternalMappingTxtLog log = new InternalMappingTxtLog();
		//	 System.out.println("mijail: append log..");
			 log.appendLog("Gamified Applications starts at..."+ new Date());
				return null;
	        }  
	        
	        @Override
	        protected void onPreExecute() {
	        }

	        @Override
	        protected void onProgressUpdate(Void... values) {
	        }
	        
	}	*/
}

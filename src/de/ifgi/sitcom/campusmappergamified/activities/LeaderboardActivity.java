package de.ifgi.sitcom.campusmappergamified.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.dialogs.SettingsDialog;
import de.ifgi.sitcom.campusmappergamified.io.InternalMappingTxtLog;

/*
 * Activity that manages the leaderboard of players.
 */
public class LeaderboardActivity extends SherlockFragmentActivity{
	
    // Set the SPARQL endpoint URI
    private static final String SPARQL_ENDPOINT_URI = "http://data.uni-muenster.de:8080/openrdf-workbench/repositories/indoormapping/query";
 //   private static final String SPARQL_ENDPOINT_URI1 = "http://data.uni-muenster.de/sparql";
    
	private int mMenuID = R.menu.leaderboard;
	private InternalMappingTxtLog log = new InternalMappingTxtLog();
    ArrayList<HashMap<String,String>> playerList;
    ListView lv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
		
        //writes the log when user visits the leaderboard
		log.appendLog("Visit leaderboard;"+new Date());
		
		// enable up button in action bar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	    lv=(ListView) LeaderboardActivity.this.findViewById(R.id.leaderboardListView);
		new LeaderboardList().execute(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(mMenuID, menu);
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
	          Intent intent = new Intent(this, MappingActivity.class);
	          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				setResult(Activity.RESULT_OK, intent);
				finish();
          return true;
          
        case R.id.action_ownership:
		    Intent ownershipMap = new Intent(this, OwnershipMapActivity.class);
		    ownershipMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(ownershipMap);
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
	 * Get all players from the database
	 * 
	 */
	public ArrayList<HashMap<String,String>> getLeaderboard()
	{
        ArrayList<HashMap<String,String>> alist = new ArrayList<HashMap<String,String>>();
        
      String queryString =  "PREFIX prv: <http://purl.org/net/provenance/ns/> "+
    		  				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    		  				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
						      "PREFIX indoor:<http://data.uni-muenster.de/context/indoormapping/> "+
						    	"  SELECT DISTINCT  ?nick ?totalScore  WHERE "+
						    	 " { "+
						    	 " 	GRAPH <http://data.uni-muenster.de/context/indoormapping> "+ 
						    	 " 	{ "+
						    	 " 		?x rdf:type prv:Player . "+
						//    	 " 		?x foaf:givenName ?name .  "+
						    	 " 		?x foaf:nick ?nick . "+
						    	 " 		?x foaf:mbox ?email . "+
						    	 " 		?x indoor:hasRegistrationDate ?registrationDate . "+ 
						    	 " 		?x indoor:hasTotalPlayerScore ?totalScore . "+
						    	 " 		?x indoor:hasPersonBadge ?badge "+
						    	 " 	} "+
						    	"  } ORDER BY DESC(?totalScore) ";
       
        	 Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
			   QueryEngineHTTP qe = new QueryEngineHTTP(SPARQL_ENDPOINT_URI, query);
			        String pw = "!nd00rmapping";
			        qe.setBasicAuthentication("indoormapping", pw.toCharArray());			

	        ResultSet resultSet = null;
	        
	        try {
	            // Execute the query and obtain results
	            resultSet = qe.execSelect();	
			} catch (Exception e) {

				return null;
			}
	    		
	        try {
	        	int i = 0;
	    		while (resultSet.hasNext())
	    		{
		    		QuerySolution solution = resultSet.next();
		        	
		        	Log.v("solution players", solution.toString());
		        	
		        	if (solution.get("nick") != null) {
		        		
		                HashMap<String, String> map = new HashMap<String, String>();
		        		String namePlayer= solution.getLiteral("nick").getString();
		        		String scorePlayer = solution.getLiteral("totalScore").getString();
		        		
		        		map.put("nick", namePlayer);
		        		map.put("totalScore", scorePlayer);
		        		alist.add(map);
		        		i = i++;
		        	}
	    		}
			} catch (Exception e) {
				return null;
			}
		
	        qe.close();

        return alist;
	}
	
	/*
	 * class used to save the result image using separate thread
	 */
	private class LeaderboardList extends AsyncTask<Activity, Void, ArrayList<HashMap<String, String>>>
	{
		 @Override
	        protected ArrayList<HashMap<String, String>> doInBackground(Activity... params) {
			 //
			 playerList = getLeaderboard();

			 return playerList;
	        }  
	        
	        @Override
	        protected void onPreExecute() {
	        }

	        @Override
	        protected void onProgressUpdate(Void... values) {
	        }
	        
	        @Override
	        protected void onPostExecute(ArrayList<HashMap<String, String>> playerList) {
				   String[] from = {"nick","totalScore"};
				   int[] to={R.id.playerNameTextView,R.id.scoreTextView};
				    ListAdapter adapter = new SimpleAdapter(LeaderboardActivity.this, playerList, R.layout.leaderboard_row, from, to);

				    lv.setAdapter(adapter);
				    lv.invalidateViews();
	        }
	}
}

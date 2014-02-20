package de.ifgi.sitcom.campusmappergamified.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import de.ifgi.sitcom.campusmappergamified.activities.MappingActivity;
import de.ifgi.sitcom.campusmappergamified.activities.MyCampusMapperGame;

public class ScoreManager extends MappingActivity {

	public final static String PREFS_BUILDING_SCORE = "BUILDING_SCORE";
	private SharedPreferences buildingScorePref;	
    private Editor myEditor;	
	
	public ScoreManager(Context context) {
		this.buildingScorePref = context.getSharedPreferences(PREFS_BUILDING_SCORE, Context.MODE_PRIVATE);	
	    this.myEditor = buildingScorePref.edit();
	}	

	public void setScorePerBuilding ( String buildingURICode)
	{
	    myEditor.putString(buildingURICode.substring(buildingURICode.lastIndexOf("/") + 1), MyCampusMapperGame.getInstance().getMybuildingScore().toString());
	    myEditor.commit();
	}
	
	public Integer getScorePerBuilding (String buildingURICode)
	{
		return Integer.valueOf(buildingScorePref.getString(buildingURICode.substring(buildingURICode.lastIndexOf("/") + 1), "0").toString());
	}
}

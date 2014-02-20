package de.ifgi.sitcom.campusmappergamified.activities;

import android.app.Application;
import android.content.Context;

public class MyCampusMapperGame extends Application {
	    private static MyCampusMapperGame instance;
	    public Integer myScore = 0;
	    public Integer mybuildingScore = 0;
	    public String buildingCode = "";
	    public String playerName = "";
	    public String playerNick = "";
	    public String playerEmail = "";
	    public boolean newStart = true;

	    public static MyCampusMapperGame getInstance() {
	        return instance;
	    }

	    public static Context getContext(){
	        return instance;
	        // or return instance.getApplicationContext();
	    }

	    public Integer getMyScore(){
		      return myScore;
		    }
	    
	    public void setMyScore(Integer score){
	    	myScore = score;
	    }

		public Integer getMybuildingScore() {
			return mybuildingScore;
		}

		public void setMybuildingScore(Integer mybuildingScore) {
			this.mybuildingScore = mybuildingScore;
		}

		public String getBuildingCode() {
			return buildingCode;
		}

		public void setBuildingCode(String buildingCode) {
			this.buildingCode = buildingCode;
		}

		public String getPlayerName() {
			return playerName;
		}

		public void setPlayerName(String playerName) {
			this.playerName = playerName;
		}

		public String getPlayerNick() {
			return playerNick;
		}

		public void setPlayerNick(String playerNick) {
			this.playerNick = playerNick;
		}

		public String getPlayerEmail() {
			return playerEmail;
		}

		public void setPlayerEmail(String playerEmail) {
			this.playerEmail = playerEmail;
		}

		@Override
	    public void onCreate() {
	        instance = this;
	        super.onCreate();
	    }

		public boolean isNewStart() {
			return newStart;
		}

		public void setNewStart(boolean newStart) {
			this.newStart = newStart;
		}
	}
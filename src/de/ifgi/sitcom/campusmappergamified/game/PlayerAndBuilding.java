package de.ifgi.sitcom.campusmappergamified.game;

import org.osmdroid.util.GeoPoint;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

public class PlayerAndBuilding {

	public String playerId = "";
	public String buildingId = "";
	public Integer buildingScore = 0;
	public GeoPoint center;
	public String flag = "free";
	
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public String getBuildingId() {
		return buildingId;
	}
	public void setBuildingId(String buildingId) {
		this.buildingId = buildingId;
	}
	public Integer getBuildingScore() {
		return buildingScore;
	}
	public void setBuildingScore(Integer buildingScore) {
		this.buildingScore = buildingScore;
	}
	public GeoPoint getCenter() {
		return center;
	}
	public void setCenter(GeoPoint center) {
		this.center = center;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	
	
}

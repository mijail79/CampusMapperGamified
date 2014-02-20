package de.ifgi.sitcom.campusmappergamified.indoordata.geometry;

import java.util.ArrayList;

import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;

import android.graphics.Canvas;
import android.graphics.Paint;


/*
 * represents polygon in local two-d reference system
 * 
 * intended to refer to pixel coordinates on floor plan image
 */
public class Polygon extends Geometry {

	ArrayList<Point> points;
	private String escapePlanURI;
	private FloorPlan floorPlan;
	
	public Polygon(ArrayList<Point> points) {
		super();
		this.points = points;
	}

	public ArrayList<Point> getPoints() {
		return points;
	}

	public void setPoints(ArrayList<Point> points) {
		this.points = points;
	}
	
	public String getEscapePlanURI() {
		return escapePlanURI;
	}

	public void setEscapePlanURI(String escapePlanURI) {
		this.escapePlanURI = escapePlanURI;
	}

	@Override
	public void draw(Canvas canvas, Paint paint) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public FloorPlan getFloorPlan() {
		return floorPlan;
	}

	@Override
	public void setFloorPlan(FloorPlan floorPlan) {
		this.floorPlan = floorPlan;
	}
	
}

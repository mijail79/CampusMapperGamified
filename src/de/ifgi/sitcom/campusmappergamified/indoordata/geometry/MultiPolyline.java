package de.ifgi.sitcom.campusmappergamified.indoordata.geometry;

import java.util.ArrayList;

import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;

import android.graphics.Canvas;
import android.graphics.Paint;


/*
 * represents one multipolyline in local two-d reference system
 * consists of connected set of polylines
 * 
 * intended to refer to pixel coordinates on floor plan image
 */
public class MultiPolyline extends Geometry {
	
	private ArrayList<Polyline> polylines;
	private String escapePlanURI;
	private FloorPlan floorPlan;

	public MultiPolyline(ArrayList<Polyline> polylines) {
		super();
		this.polylines = polylines;
	}

	public ArrayList<Polyline> getPolylines() {
		return polylines;
	}
	
	public String getEscapePlanURI() {
		return escapePlanURI;
	}

	public void setEscapePlanURI(String escapePlanURI) {
		this.escapePlanURI = escapePlanURI;
	}

	public void setPolylines(ArrayList<Polyline> polylines) {
		this.polylines = polylines;
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

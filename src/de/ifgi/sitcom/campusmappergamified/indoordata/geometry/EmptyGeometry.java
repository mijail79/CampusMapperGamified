package de.ifgi.sitcom.campusmappergamified.indoordata.geometry;

import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import android.graphics.Canvas;
import android.graphics.Paint;


/*
 * represents one point in local two-d reference system
 * 
 * intended to refer to pixel coordinates on floor plan image
 */
public class EmptyGeometry extends Geometry {
	
	private String escapePlanURI;
	private FloorPlan floorPlan;
	
	public EmptyGeometry (FloorPlan floorPlan){
		this.floorPlan = floorPlan;
	}

	
	public String getEscapePlanURI() {
		return escapePlanURI;
	}

	public void setEscapePlanURI(String escapePlanURI) {
		this.escapePlanURI = escapePlanURI;
	}


	@Override
	public void draw(Canvas canvas, Paint paint) {

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

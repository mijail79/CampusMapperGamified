package de.ifgi.sitcom.campusmappergamified.indoordata.geometry;

import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class Geometry {

	
	public abstract void draw(Canvas canvas, Paint paint);
	
	public abstract String getEscapePlanURI();

	public abstract void setEscapePlanURI(String escapePlanURI);
	
	public abstract FloorPlan getFloorPlan();

	public abstract void setFloorPlan(FloorPlan floorPlan);
	
	
}

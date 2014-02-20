package de.ifgi.sitcom.campusmappergamified.indoordata;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;

/*
 * connects rooms and corridors of different floors
 * 
 * 
 */
public class Elevator extends VerticalConnection {
	
	public Elevator(Geometry geometry, ArrayList<Geometry> destinations) {
		super(Connection.CONNECTION_TYPE_ELEVATOR, geometry);
		setDestinations(destinations);
	}

	@ Override
	public void draw(Canvas canvas, boolean selected){
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		if(selected){
			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(14);
			geometry.draw(canvas, paint);
		}
		
		paint.setColor(Color.MAGENTA);
		paint.setStrokeWidth(10);
		geometry.draw(canvas, paint);
	}
	
	
	
}

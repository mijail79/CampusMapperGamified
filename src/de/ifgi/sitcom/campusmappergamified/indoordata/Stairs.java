package de.ifgi.sitcom.campusmappergamified.indoordata;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;

/*
 * connects rooms and corridors of different floors
 * 
 */
public class Stairs extends VerticalConnection {

	
	public Stairs(Geometry geometry, ArrayList<Geometry> destinations) {
		super(Connection.CONNECTION_TYPE_STAIRS, geometry);
		setDestinations(destinations);
	}

	public void draw(Canvas canvas, boolean selected){
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		if(selected){
			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(14);
			geometry.draw(canvas, paint);
		}
		
		paint.setColor(Color.RED);
		paint.setStrokeWidth(10);
		geometry.draw(canvas, paint);
	}

	
}

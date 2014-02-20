package de.ifgi.sitcom.campusmappergamified.indoordata;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;

/*
 * connects a room or corridor either with a point outdoors (to be chosen on osm map), 
 * or with another room/ corridor on a different floor plan in the same floor
 */
public class Entrance extends Connection{
	

	public Entrance(int type, Geometry geometry) {
		super(type, geometry);
	}



	public void draw(Canvas canvas, boolean selected){
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		if(selected){
			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(14);
			geometry.draw(canvas, paint);
		}
		
		paint.setColor(Color.GREEN);
		paint.setStrokeWidth(10);
		geometry.draw(canvas, paint);
	}
	
}

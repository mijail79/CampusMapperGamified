package de.ifgi.sitcom.campusmappergamified.indoordata;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Line;

/*
 * connects two rooms, two corridors, or a corridor and a room
 */
public class Door extends Connection{

	public Door(Line line) {
		super(Connection.CONNECTION_TYPE_DOOR, line);
	}
	
	public void draw(Canvas canvas, boolean selected){
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		if(selected){
			// draw yellow background
			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(7);
			geometry.draw(canvas, paint);
		}
		
		// draw door as dashed blue line
		paint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(3);
		geometry.draw(canvas, paint);

	}

}

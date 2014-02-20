package de.ifgi.sitcom.campusmappergamified.indoordata;

import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/*
 * parent class of all objects that can be mapped
 */
public abstract class MapElement {

	protected Geometry geometry;
	// uri used in lodum
	protected String uri;
	
	public MapElement(Geometry geometry) {
		super();
		this.geometry = geometry;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	
	public void draw (Canvas canvas, boolean selected){
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		if(selected){
			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(7);
			geometry.draw(canvas, paint);
		}
		
		
		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(3);
		geometry.draw(canvas, paint);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
}

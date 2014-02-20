package de.ifgi.sitcom.campusmappergamified.indoordata.geometry;

import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import android.graphics.Canvas;
import android.graphics.Paint;


/*
 * represents one point in local two-d reference system
 * 
 * intended to refer to pixel coordinates on floor plan image
 */
public class Point extends Geometry {

	private int x;
	private int y;
	
	private String escapePlanURI;
	private FloorPlan floorPlan;
	
	public Point (int x, int y){
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}
	
	public boolean equals(Point p){
		return this.x == p.getX() && this.y == p.getY();
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	
	
	public String getEscapePlanURI() {
		return escapePlanURI;
	}

	public void setEscapePlanURI(String escapePlanURI) {
		this.escapePlanURI = escapePlanURI;
	}

	public int getDistance (Point p){
		return (int) Math.sqrt((p.getX()-x)*(p.getX()-x) + (p.getY()-y)*(p.getY()-y));
	}
	
	public boolean intersects(Line line){
		
		int distA = getDistance(line.getPointA());
		int distB = getDistance(line.getPointB());
		int distAB = line.getPointA().getDistance(line.getPointB());
		
		return Math.abs((distA + distB) - distAB) < 4;
	}

	@Override
	public void draw(Canvas canvas, Paint paint) {

		canvas.drawPoint(x, y, paint);
	}
	
	public String toString(){
		return "Point((" + x + " " + y + "))";
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

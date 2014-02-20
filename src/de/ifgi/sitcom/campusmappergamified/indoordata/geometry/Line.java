package de.ifgi.sitcom.campusmappergamified.indoordata.geometry;

import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Line extends Geometry{

	Point [] points = new Point [2];
	private String escapePlanURI;
	private FloorPlan floorPlan;

	public Line(Point[] points) {
		super();
		this.points = points;
	}
	
	public Line (Point a, Point b) {
		super();
		this.points[0] = a;
		this.points[1] = b;
	}

	public String getEscapePlanURI() {
		return escapePlanURI;
	}
	

	public void setEscapePlanURI(String escapePlanURI) {
		this.escapePlanURI = escapePlanURI;
	}
	public Point[] getPoints() {
		return points;
	}

	public void setPoints(Point[] points) {
		this.points = points;
	}
	
	public Point getPointA(){
		return points[0];
	}
	
	public Point getPointB(){
		return points[1];
	}

	public void setPointA(Point a){
		points[0] = a;
	}
	
	public void setPointB(Point b){
		points[1] = b;
	}

	@Override
	public void draw(Canvas canvas, Paint paint) {

		canvas.drawLine(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), paint);
		
		// draw end points in gray
		Paint endPointPaint = new Paint();
		endPointPaint.setColor(Color.BLACK);
		endPointPaint.setStrokeWidth(5);
		points[0].draw(canvas, endPointPaint);
		points[1].draw(canvas, endPointPaint);
		
		
	}
	
	public Point getIntersection(Line l){
		
		float x1 = points[0].getX();
		float y1 = points[0].getY();
		float x2 = points[1].getX();
		float y2 = points[1].getY();
		float x3 = l.getPointA().getX();
		float y3 = l.getPointA().getY();
		float x4 = l.getPointB().getX();
		float y4 = l.getPointB().getY();
		
		float pXCounter = (x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4);
		float pYCounter = (x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4);
		float pDenominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		
		if (pDenominator != 0) return new Point((int)(pXCounter/ pDenominator), (int)(pYCounter/ pDenominator));
		
		return null;
	}
	
	public Point getSnappingPoint(Point p) {

		float lineDX = points[0].getX() - points[1].getX();
		float lineDY = points[0].getY() - points[1].getY();

		// create line through entrance point which is orthogonal to the current
		// line
		Line roomsLine = new Line(p, new Point((int)(p.getX() + lineDY), (int)(p.getY()
				+ lineDX)));

		return getIntersection(roomsLine);

	}
	
	public float getDistance(Point p){
		
		float minDist = Float.MAX_VALUE;
		
		// check end point A
		float newDistance = points[0].getDistance(p);
		if(newDistance < minDist){
			minDist = newDistance;
		}
		
		// check end point B
		newDistance = points[1].getDistance(p);
		if(newDistance < minDist){
			minDist = newDistance;
		}
		
		// check lines								
		Point intersection  = getSnappingPoint(p);
		
		if (intersection != null && intersection.intersects(this)){
			//check intersection´s distance
			newDistance = intersection.getDistance(p);
			if(newDistance < minDist){
				minDist = newDistance;
			}					
		}
		
		
		return minDist;
		
	}
	
	public String toString(){
		return "Line((" + points[0].getX() + " " + points[0].getY() + ", " + points[1].getX() + " " + points[1].getY() + "))";
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

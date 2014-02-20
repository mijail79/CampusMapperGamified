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
public class MultiLine extends Geometry {
	
	private ArrayList<Line> lines;
	private String escapePlanURI;
	private FloorPlan floorPlan;

	public MultiLine(ArrayList<Line> lines) {
		super();
		this.lines = lines;
	}
	
	public MultiLine() {
		super();
		this.lines = new ArrayList<Line>();
	}
	

	public String getEscapePlanURI() {
		return escapePlanURI;
	}

	public void setEscapePlanURI(String escapePlanURI) {
		this.escapePlanURI = escapePlanURI;
	}
	public ArrayList<Line> getLines() {
		return lines;
	}

	public void setLines(ArrayList<Line> lines) {
		this.lines = lines;
	}
	
	public void addLine(Line line){
		lines.add(line);
	}
	

	@Override
	public void draw(Canvas canvas, Paint paint) {

		for(Line l : lines){
			l.draw(canvas, paint);
		}
		
	}


	public void divideLine(Line l, Point p){
		
		// divide the intersected line
		// remove the old one
		int index = lines.indexOf(l);
		
		lines.remove(index);
		
		// replace by two new	
		lines.add(index, new Line(l.getPointA(), p));
		lines.add(index, new Line(l.getPointB(), p));
	}
	
	public String[] toStringArray(){
		String [] stringLines = new String [lines.size()];
		
		int i = 0;
		for (Line l : lines){
			stringLines[i] = l.toString();
			
			
			i++;
		}
		
		
		return stringLines;
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

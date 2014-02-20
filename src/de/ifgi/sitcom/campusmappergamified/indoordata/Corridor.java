package de.ifgi.sitcom.campusmappergamified.indoordata;


import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Line;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.MultiLine;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;

/*
 * a corridor; its geometry is a multiline
 */
public class Corridor extends IndoorSpace{

	public Corridor(MultiLine multiLine) {
		super(multiLine);
	}
	
	public Corridor() {
		super(new MultiLine());
	}
	
	public Line addLine(Line line){
	
		
		if (!((MultiLine) geometry).getLines().isEmpty()){

			/*
			 * nearest neighbor matching
			 * 
			 * for all lines do
			 * get intersection
			 * get distance to intersection
			 * find point with shortest distance
			 * 
			 */
			
			
			
			float minDist = Float.MAX_VALUE;
			Line minLine = null;
			Point minIntersection = null;
			int minPointIndex = 0; // to identify which of the two points of the lines needs to be adjusted
			
			for(Line l : ((MultiLine) geometry).getLines()){

				Point newIntersection = l.getIntersection(line);

				if (newIntersection != null && newIntersection.intersects(l)){
					// check point with index 0, i.e PointA
					float newDistance = newIntersection.getDistance(line.getPointA());
					if(newDistance < minDist){
						minDist = newDistance;
						minLine = l;
						minIntersection = newIntersection;
						minPointIndex = 0;
					}
					
					// check point with index 1, i.e PointB
					newDistance = newIntersection.getDistance(line.getPointB());
					if(newDistance < minDist){
						minDist = newDistance;
						minLine = l;
						minIntersection = newIntersection;
						minPointIndex = 1;
					}					
				}
				

			}
		
			/*
			 * if we found an intersection adjust/ add the new line and the intersected one
			 */
			if (minIntersection != null){
				// adjust the new line
				line.getPoints()[minPointIndex] = minIntersection;
				
				// divide the intersected line
				((MultiLine) geometry).divideLine(minLine, minIntersection);
				
				// add the new line
				((MultiLine) geometry).addLine(line);
				
			} else return null;
			

		}
		
		
		else ((MultiLine) geometry).addLine(line);
		
		return line;
	}
	
	
	public void removeNewestLine(){
		if (((MultiLine) geometry).getLines().size()>0)
		((MultiLine) geometry).getLines().remove(((MultiLine) geometry).getLines().size()-1);
	}
	

}

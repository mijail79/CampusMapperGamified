package de.ifgi.sitcom.campusmappergamified.indoordata;

import java.util.ArrayList;

import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Line;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.MultiLine;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;
import de.ifgi.sitcom.campusmappergamified.outdoordata.Building;

import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;

/*
 * represents one floor plan and encapsulates all the data collected based on it
 */
public class FloorPlan {

	private String id = "";
	private int floorNumber;
	private Building building;

	private ArrayList<Room> rooms;
	private ArrayList<Corridor> corridors;
	private ArrayList<Entrance> entrances;
	private ArrayList<Stairs> stairs;
	private ArrayList<Elevator> elevators;
	private ArrayList<Door> doors;
	
	private String [] roomNames;
	private String [] personNames;
	
	// URIs used in LODUM
	private String floorURI;
	private String escapePlanURI;
	
	private Uri croppedImageUri;
	private Uri sourceImageUri;
	private boolean fromServer = false;

	
	
	public boolean isFromServer() {
		return fromServer;
	}

	public void setFromServer(boolean fromServer) {
		this.fromServer = fromServer;
	}

	public String getEscapePlanURI() {
		return escapePlanURI;
	}

	public void setEscapePlanURI(String escapePlanURI) {
		this.escapePlanURI = escapePlanURI;
	}

	public Uri getCroppedFloorPlanImageUri() {
		return croppedImageUri;
	}

	public void setCroppedFloorPlanImageUri(Uri croppedFloorPlanImageUri) {
		this.croppedImageUri = croppedFloorPlanImageUri;
	}

	public Uri getSourceFloorPlanImageUri() {
		return sourceImageUri;
	}

	public void setSourceFloorPlanImageUri(Uri sourceFloorPlanImageUri) {
		this.sourceImageUri = sourceFloorPlanImageUri;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public String getBuildingURI() {
		return building.getBuildingURI();
	}

	public void setBuildingURI(String buildingURI) {
		building.setBuildingURI(buildingURI);
	}

	public String getFloorURI() {
		return floorURI;
	}

	public void setFloorURI(String floorURI) {
		this.floorURI = floorURI;
	}

	public FloorPlan() {
		super();
		this.rooms = new ArrayList<Room>();
		this.corridors = new ArrayList<Corridor>();
		this.entrances = new ArrayList<Entrance>();
		this.stairs = new ArrayList<Stairs>();
		this.elevators = new ArrayList<Elevator>();
		this.doors = new ArrayList<Door>();
		this.building = new Building();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBuildingName() {
		return building.getBuildingName();
	}

	public void setBuildingName(String buildingName) {
		building.setBuildingName(buildingName);
	}

	public int getFloorNumber() {
		return floorNumber;
	}

	public void setFloorNumber(int floorNumber) {
		this.floorNumber = floorNumber;
	}

	public ArrayList<Room> getRooms() {
		return rooms;
	}

	public void setRooms(ArrayList<Room> rooms) {
		this.rooms = rooms;
	}

	public ArrayList<Corridor> getCorridors() {
		return corridors;
	}

	public void setCorridors(ArrayList<Corridor> corridors) {
		this.corridors = corridors;
	}

	
	public ArrayList<Entrance> getEntrances() {
		return entrances;
	}

	public void setEntrances(ArrayList<Entrance> entrances) {
		this.entrances = entrances;
	}

	public ArrayList<Door> getDoors() {
		return doors;
	}

	public void setDoors(ArrayList<Door> doors) {
		this.doors = doors;
	}

	public void draw(Canvas canvas){
		
		for(Corridor c: corridors){
			c.draw(canvas, false);
		}
		
		for(Door d: doors){
			d.draw(canvas, false);
		}
		
		for(Room r: rooms){
			r.draw(canvas, false);
		}
		
		for(Entrance e: entrances){
			e.draw(canvas, false);
		}
		
		for(Stairs s: stairs){
			s.draw(canvas, false);
		}
		
		for(Elevator e: elevators){
			e.draw(canvas, false);
		}
	}
	
	/*
	 * @ add if is false, the new entrance will not be added to the plan. only the right position is computed
	 */
	public MapElement addEntrance(Point entrancePoint, int type, boolean add){

		/*
		 * find nearest corridor or room and move to
		 */
		float minDist = Float.MAX_VALUE;
		Point minPoint = null;
		Corridor minCorridor = null; // will be null in the end if closest point is end point
		Line minLine = null; // will be null in the end if closest point is end point


		/*
		 * check corridors
		 */
		for (Corridor corridor : corridors){
			
			for(Line l : ((MultiLine) corridor.getGeometry()).getLines()){
				
				// check end point A
				float newDistance = l.getPointA().getDistance(entrancePoint);
				if(newDistance < minDist){
					minDist = newDistance;
					minPoint = l.getPointA();
					minCorridor = null;
					minLine = null;
				}
				
				// check end point B
				newDistance = l.getPointB().getDistance(entrancePoint);
				if(newDistance < minDist){
					minDist = newDistance;
					minPoint = l.getPointB();
					minCorridor = null;
					minLine = null;
				}
				
				// check lines				
				float lineDX = l.getPointA().getX() - l.getPointB().getX();
				float lineDY = l.getPointA().getY() - l.getPointB().getY();
				
				// create line through entrance point which is orthogonal to the current line
				Line entranceLine = new Line (entrancePoint, new Point((int)(entrancePoint.getX() + lineDY), (int)(entrancePoint.getY() + lineDX)));
				
				Point intersection = l.getIntersection(entranceLine);
				if (intersection != null && intersection.intersects(l)){
					//check intersection큦 distance
					newDistance = intersection.getDistance(entrancePoint);
					if(newDistance < minDist){
						minDist = newDistance;
						minPoint = intersection;
						minCorridor = corridor;
						minLine = l;
					}					
				}
			}
		} // end loop through corridors
		
		
		/*
		 * check rooms
		 */
		for (Room room : rooms){
			
			// check end point A
			float newDistance = ((Point) room.getGeometry()).getDistance(entrancePoint);
			if(newDistance < minDist){
				minDist = newDistance;
				minPoint = ((Point) room.getGeometry());
				minCorridor = null;
				minLine = null;
			}
		}

		/*
		 * if closest point is on a corridor
		 */
		if (minCorridor != null) { // i.e. we need to divide the line which intersected
			
			((MultiLine) minCorridor.getGeometry()).divideLine(minLine, minPoint);
		}
		

		// add new entrance with minPoint if available
		if (minPoint != null){

			if(type == Connection.CONNECTION_TYPE_ENTRANCE_OUTDOOR)
				{
				Entrance e = new EntranceOutdoor(minPoint);
				entrances.add(e);
				return e;
				}
			else {
				Entrance e = new EntranceIndoor(minPoint);
				entrances.add(e);
				return e;
			}
		}

		else {
			
			if(type == Connection.CONNECTION_TYPE_ENTRANCE_OUTDOOR)
				{
				Entrance e = new EntranceOutdoor(entrancePoint);
				entrances.add(e);
				return e;
				}
			else {
				Entrance e = new EntranceIndoor(entrancePoint);
				entrances.add(e);
				return e;
			}
		}
	}

	
	/*
	 * @ add if is false, the new stairs will not be added to the plan. only the right position is computed
	 */
	public MapElement addStairs(Point stairsPoint, int type, boolean add){

		/*
		 * find nearest corridor or room and move to
		 */
		float minDist = Float.MAX_VALUE;
		Point minPoint = null;
		Corridor minCorridor = null; // will be null in the end if closest point is end point
		Line minLine = null; // will be null in the end if closest point is end point


		/*
		 * check corridors
		 */
		for (Corridor corridor : corridors){
			
			for(Line l : ((MultiLine) corridor.getGeometry()).getLines()){
				
				// check end point A
				float newDistance = l.getPointA().getDistance(stairsPoint);
				if(newDistance < minDist){
					minDist = newDistance;
					minPoint = l.getPointA();
					minCorridor = null;
					minLine = null;
				}
				
				// check end point B
				newDistance = l.getPointB().getDistance(stairsPoint);
				if(newDistance < minDist){
					minDist = newDistance;
					minPoint = l.getPointB();
					minCorridor = null;
					minLine = null;
				}
				
				// check lines				
				float lineDX = l.getPointA().getX() - l.getPointB().getX();
				float lineDY = l.getPointA().getY() - l.getPointB().getY();
				
				// create line through entrance point which is orthogonal to the current line
				Line stairsLine = new Line (stairsPoint, new Point((int)(stairsPoint.getX() + lineDY), (int)(stairsPoint.getY() + lineDX)));
				
				Point intersection = l.getIntersection(stairsLine);
				if (intersection != null && intersection.intersects(l)){
					//check intersection큦 distance
					newDistance = intersection.getDistance(stairsPoint);
					if(newDistance < minDist){
						minDist = newDistance;
						minPoint = intersection;
						minCorridor = corridor;
						minLine = l;
					}					
				}
			}
		} // end loop through corridors
		
		
		/*
		 * check rooms
		 */
		for (Room room : rooms){
			
			// check end point A
			float newDistance = ((Point) room.getGeometry()).getDistance(stairsPoint);
			if(newDistance < minDist){
				minDist = newDistance;
				minPoint = ((Point) room.getGeometry());
				minCorridor = null;
				minLine = null;
			}
		}
		
		
		/*
		 * if closest point is on a corridor
		 */
		if (minCorridor != null) { // i.e. we need to divide the line which intersected
			
			((MultiLine) minCorridor.getGeometry()).divideLine(minLine, minPoint);
		}


		// add new stairs or elevator with minPoint if available
		if (minPoint != null) {
			if (type == Connection.CONNECTION_TYPE_STAIRS)
			{
				Stairs s = new Stairs(minPoint, null);
				if (add) stairs.add(s);
				return s;
			}

			else
			{
				Elevator e = new Elevator(minPoint, null);
				if (add) elevators.add(e);
				return e;
			}
		} else {
			if (type == Connection.CONNECTION_TYPE_STAIRS)
				{
				Stairs s = new Stairs(stairsPoint, null);
				if (add) stairs.add(s);
				return s;
				}
			else
				{
				Elevator e = new Elevator(stairsPoint, null);
				if (add) elevators.add(e);
				return e;
				}
		}
	}

	public Room addRoom(Point roomsPoint){

		/*
		 * find nearest corridor and move to
		 */
		float minDist = Float.MAX_VALUE;
		Point minPoint = null;
		Corridor minCorridor = null; // will be null in the end if closest point is end point
		Line minLine = null; // will be null in the end if closest point is end point


		for (Corridor corridor : corridors){
			
			for(Line l : ((MultiLine) corridor.getGeometry()).getLines()){
				
				// check end point A
				float newDistance = l.getPointA().getDistance(roomsPoint);
				if(newDistance < minDist){
					minDist = newDistance;
					minPoint = l.getPointA();
					minCorridor = null;
					minLine = null;
				}
				
				// check end point B
				newDistance = l.getPointB().getDistance(roomsPoint);
				if(newDistance < minDist){
					minDist = newDistance;
					minPoint = l.getPointB();
					minCorridor = null;
					minLine = null;
				}
				
				// check lines								
				Point intersection  = l.getSnappingPoint(roomsPoint);
				
				if (intersection != null && intersection.intersects(l)){
					//check intersection큦 distance
					newDistance = intersection.getDistance(roomsPoint);
					if(newDistance < minDist){
						minDist = newDistance;
						minPoint = intersection;
						minCorridor = corridor;
						minLine = l;
					}					
				}
			}
		} // end loop through corridors
		
		if (minCorridor != null) { // i.e. we need to divide the line which intersected
			
			((MultiLine) minCorridor.getGeometry()).divideLine(minLine, minPoint);
		}


		// add new connection with minPoint if available
		if (minPoint != null){
		doors.add(new Door(new Line(roomsPoint, minPoint)));	
		}
		
		Room newRoom = new Room(roomsPoint, null, null);
		rooms.add(newRoom);
		
		return newRoom;
	}
	
	public Line addDoor (Line doorLine){
		
		/*
		 * find nearest room or hall way for point a
		 */
		float minDist = Float.MAX_VALUE;
		Point minPoint = null;
		Corridor minCorridor = null; // will be null in the end if closest point is end point
		Line minLine = null; // will be null in the end if closest point is end point

		// check the rooms
		for (Room r : rooms){
			float newDistance = ((Point) r.getGeometry()).getDistance(doorLine.getPointA());
			if(newDistance < minDist){
				minDist = newDistance;
				minPoint = ((Point) r.getGeometry());
			}
			
			
		}
		
		// check the corridors
		for (Corridor corridor : corridors){
			
			for(Line l : ((MultiLine) corridor.getGeometry()).getLines()){
				
				// check end point A
				float newDistance = l.getPointA().getDistance(doorLine.getPointA());
				if(newDistance < minDist){
					minDist = newDistance;
					minPoint = l.getPointA();
					minCorridor = null;
					minLine = null;
				}
				
				// check end point B
				newDistance = l.getPointB().getDistance(doorLine.getPointA());
				if(newDistance < minDist){
					minDist = newDistance;
					minPoint = l.getPointB();
					minCorridor = null;
					minLine = null;
				}
				
				// check lines								
				Point intersection  = l.getSnappingPoint(doorLine.getPointA());
				
				if (intersection != null && intersection.intersects(l)){
					//check intersection큦 distance
					newDistance = intersection.getDistance(doorLine.getPointA());
					if(newDistance < minDist){
						minDist = newDistance;
						minPoint = intersection;
						minCorridor = corridor;
						minLine = l;
					}					
				}
			}
		} // end loop through corridors
		
		if (minCorridor != null) { // i.e. we need to divide the line which intersected
			
			((MultiLine) minCorridor.getGeometry()).divideLine(minLine, minPoint);
		}
		
		if (minPoint != null) doorLine.setPointA(minPoint);
		else return null;

		
		
		/*
		 * find nearest room or hall way for point b
		 */
		
		minDist = Float.MAX_VALUE;
		minPoint = null;
		minCorridor = null; // will be null in the end if closest point is end point
		minLine = null; // will be null in the end if closest point is end point

		// check the rooms
		for (Room r : rooms){
			float newDistance = ((Point) r.getGeometry()).getDistance(doorLine.getPointB());
			if(newDistance < minDist){
				minDist = newDistance;
				minPoint = ((Point) r.getGeometry());
			}
			
			
		}
		
		// check the corridors
		for (Corridor corridor : corridors){
			
			for(Line l : ((MultiLine) corridor.getGeometry()).getLines()){
				
				// check end point A
				float newDistance = l.getPointA().getDistance(doorLine.getPointB());
				if(newDistance < minDist){
					minDist = newDistance;
					minPoint = l.getPointA();
					minCorridor = null;
					minLine = null;
				}
				
				// check end point B
				newDistance = l.getPointB().getDistance(doorLine.getPointB());
				if(newDistance < minDist){
					minDist = newDistance;
					minPoint = l.getPointB();
					minCorridor = null;
					minLine = null;
				}
				
				// check lines								
				Point intersection  = l.getSnappingPoint(doorLine.getPointB());
				
				if (intersection != null && intersection.intersects(l)){
					//check intersection큦 distance
					newDistance = intersection.getDistance(doorLine.getPointB());
					if(newDistance < minDist){
						minDist = newDistance;
						minPoint = intersection;
						minCorridor = corridor;
						minLine = l;
					}					
				}
			}
		} // end loop through corridors
		
		if (minCorridor != null) { // i.e. we need to divide the line which intersected
			
			((MultiLine) minCorridor.getGeometry()).divideLine(minLine, minPoint);
		}
		
		if (minPoint != null) {
			doorLine.setPointB(minPoint);
			doors.add(new Door(doorLine));
			return doorLine;
			
		}
		else return null;
		
		
	}
	
	public ArrayList<Stairs> getStairs() {
		return stairs;
	}

	public void setStairs(ArrayList<Stairs> stairs) {
		this.stairs = stairs;
	}
	
	
	public MapElement selectObject (float maxDist, Point position){
		
		MapElement minObject = null;
		float minDist = Float.MAX_VALUE;
		
		// check rooms
		for (Room r : rooms){
			float newDist = position.getDistance((Point)r.getGeometry());
			if( newDist < minDist){
				minObject = r;
				minDist = newDist;
			}
		}		

		// check entrances
		for (Entrance e : entrances){
			float newDist = position.getDistance((Point)e.getGeometry());
			if( newDist < minDist){
				minObject = e;
				minDist = newDist;
			}
		}
		
		// check stairs
		for (Stairs s : stairs){
			float newDist = position.getDistance((Point)s.getGeometry());
			if( newDist < minDist){
				minObject = s;
				minDist = newDist;
			}
		}
		
		// check elevators
		for (Elevator e : elevators){
			float newDist = position.getDistance((Point)e.getGeometry());
			if( newDist < minDist){
				minObject = e;
				minDist = newDist;
			}
		}
		
		// check doors
		for (Door d : doors){

			// add 10 to ease selection of stairs, elevators and entrances that might overlap with a corridor
			float newDist = ((Line) d.getGeometry()).getDistance(position) + 10;
			if( newDist < minDist){
				minObject = d;
				minDist = newDist;
			}
		}
		
		// check corridors
		for (Corridor c : corridors){
			
			for (Line l : ((MultiLine) c.getGeometry()).getLines()){
				
				// add 10 to ease selection of stairs, elevators and entrances that might overlap with a corridor
				float newDist = l.getDistance(position) + 10;
				if( newDist < minDist){
					minObject = c;
					minDist = newDist;
				}
			}
		}
		
		
		Log.v("debug", "minDist = " + minDist);
		
		if (minDist <= maxDist) return minObject;
		else return null;
	}
	
	public void delete(MapElement mapElement){
		if (mapElement.getClass() == Corridor.class){
			corridors.remove(mapElement);
		} else if (mapElement.getClass() == Door.class){
			doors.remove(mapElement);
		} else if (mapElement.getClass() == Room.class){
			
			Room room = (Room) mapElement;
			
			// remove connection if matches room coordinate				
			for (int i = doors.size() - 1; i >= 0; i--){
				if (((Line)doors.get(i).getGeometry()).getPointA().equals(room.getGeometry())){
					doors.remove(i);
				}
				else if (((Line)doors.get(i).getGeometry()).getPointB().equals(room.getGeometry())){
					doors.remove(i);
				}
			}
			
			rooms.remove(mapElement);
			
		} else if (mapElement.getClass() == EntranceOutdoor.class){
			entrances.remove(mapElement);
		} else if (mapElement.getClass() == Stairs.class){
			stairs.remove(mapElement);
		} else if (mapElement.getClass() == Elevator.class){
			elevators.remove(mapElement);
		}
	}

	public ArrayList<Elevator> getElevators() {
		return elevators;
	}

	public void setElevators(ArrayList<Elevator> elevators) {
		this.elevators = elevators;
	}
	

	
	public String[] getRoomNames() {
		return roomNames;
	}

	public void setRoomNames(String[] roomNames) {
		this.roomNames = roomNames;
	}

	public String[] getPersonNames() {
		return personNames;
	}

	public void setPersonNames(String[] personNames) {
		this.personNames = personNames;
	}

	
	
}

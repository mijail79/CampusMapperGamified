package de.ifgi.sitcom.campusmappergamified.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import de.ifgi.sitcom.campusmappergamified.indoordata.Connection;
import de.ifgi.sitcom.campusmappergamified.indoordata.Corridor;
import de.ifgi.sitcom.campusmappergamified.indoordata.Door;
import de.ifgi.sitcom.campusmappergamified.indoordata.Elevator;
import de.ifgi.sitcom.campusmappergamified.indoordata.Entrance;
import de.ifgi.sitcom.campusmappergamified.indoordata.EntranceIndoor;
import de.ifgi.sitcom.campusmappergamified.indoordata.EntranceOutdoor;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.indoordata.Person;
import de.ifgi.sitcom.campusmappergamified.indoordata.Room;
import de.ifgi.sitcom.campusmappergamified.indoordata.Stairs;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.EmptyGeometry;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Line;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.MultiLine;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;


/*
 * generates RDF from internally saved data
 * 
 */
public class RDFWriter {
	
	// URIs
	private static final String prefixURIbuilding    = "http://data.uni-muenster.de/building/";

	private static final String typeURIFloor    = "http://vocab.lodum.de/limap/floor";
	private static final String typeURIBuilding    = "http://vocab.lodum.de/limap/building";
	private static final String typeURIEscapePlan    = "http://vocab.lodum.de/limap/escapePlan";
	private static final String typeURIRoom    = "http://vocab.lodum.de/limap/room";
	private static final String typeURIPerson    = "http://vocab.lodum.de/limap/person";
	private static final String typeURIConnection    = "http://vocab.lodum.de/limap/connection";
	private static final String typeURIDoor    = "http://vocab.lodum.de/limap/door";
	private static final String typeURIElevator    = "http://vocab.lodum.de/limap/elevator";
	private static final String typeURIStairCase    = "http://vocab.lodum.de/limap/stairs";
	private static final String typeURIAccess    = "http://vocab.lodum.de/limap/access";
	private static final String typeURIEntrance    = "http://vocab.lodum.de/limap/entrance";
	private static final String typeURILocalCoordinates    = "http://vocab.lodum.de/limap/localCoordinates";
	private static final String typeURIGlobalCoordinates    = "http://vocab.lodum.de/limap/globalCoordinates";
	private static final String typeURIGeometry    = "http://data.uni-muenster.de/geometry";
	
	private static final String propertyURIHasEscapePlan    = "http://vocab.lodum.de/limap/hasEscapePlan";
	private static final String propertyURIHasFloorNumber    = "http://vocab.lodum.de/limap/hasFloorNumber";
	private static final String propertyURIHasConnection    = "http://vocab.lodum.de/limap/hasConnection";
	private static final String propertyURIHasLocalCoordinate    = "http://vocab.lodum.de/limap/hasLocalCoordinates";
	private static final String propertyURIHasGlobalCoordinates    = "http://vocab.lodum.de/limap/hasGlobalCoordinates";
	private static final String propertyURIHasRoom    = "http://vocab.lodum.de/limap/hasRoom";
	private static final String propertyURIHasFloor    = "http://vocab.lodum.de/limap/hasFloor";
	private static final String propertyURIHasGeometry    = "http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/hasGeometry";
	private static final String propertyURILat    = "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
	private static final String propertyURILong    = "http://www.w3.org/2003/01/geo/wgs84_pos#long";
	private static final String propertyURIUser    = "http://vocab.lodum.de/limap/user";
	private static final String propertyURIIsRoomIn    = "http://vocab.lodum.de/limap/isRoomIn";
	private static final String propertyURIIsLocatedIn    = "http://vocab.lodum.de/limap/isLocatedIn";
	private static final String propertyURIIsFloorIn    = "http://vocab.lodum.de/limap/isFloorIn";
	private static final String propertyURIInEscapePlan    = "http://vocab.lodum.de/limap/inEscapePlan";
	private static final String propertyURIConnects    = "http://vocab.lodum.de/limap/connects";
	private static final String propertyURIType    = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	private static final String propertyURIName    = "http://xmlns.com/foaf/0.1/name";
	private static final String propertyURIHasSourceImage    = "http://vocab.lodum.de/limap/hasSourceImage";
	private static final String propertyURIHasCroppedImage    = "http://vocab.lodum.de/limap/hasCroppedImage";
	private static final String propertyURIId    = "http://vocab.lodum.de/limap/id";
	
	// prefixes
	private static final String prefixFloor    = "/floor/";
	private static final String prefixEscapePlan    = "/escapePlan/";
	private static final String prefixRoom    = "/room/";
	private static final String prefixPerson    = "/person/";
	private static final String prefixDoor    = "/door/";
	private static final String prefixElevator    = "/elevator/";
	private static final String prefixStairCase    = "/staircase/";
	private static final String prefixAccess    = "/access/";
	private static final String prefixEntrance    = "/entrance/";
	private static final String prefixLocalCoordinates    = "/localCoordinates/";
	private static final String prefixGlobalCoordinates    = "/globalCoordinates/";
	private static final String prefixGeometry    = "/geometry/";
	
	
	
	
	
	public String floorPlanToRDF(FloorPlan floorPlan){
		
		// create an empty Model
		Model model = ModelFactory.createDefaultModel();
		 
		 /*
		  * create properties
		  */

		Property propertyHasConnection = ResourceFactory
				.createProperty(propertyURIHasConnection);
		Property propertyHasFloorNumber = ResourceFactory
				.createProperty(propertyURIHasFloorNumber);
		Property propertyHasEscapePlan = ResourceFactory
				.createProperty(propertyURIHasEscapePlan);
		Property propertyHasSourceImage = ResourceFactory
				.createProperty(propertyURIHasSourceImage);
		Property propertyHasCroppedImage = ResourceFactory
				.createProperty(propertyURIHasCroppedImage);
		Property propertyHasLocalCoordinates = ResourceFactory
				.createProperty(propertyURIHasLocalCoordinate);
		Property propertyHasGlobalCoordinates = ResourceFactory
				.createProperty(propertyURIHasGlobalCoordinates);
		Property propertyHasRoom = ResourceFactory
				.createProperty(propertyURIHasRoom);
		Property propertyHasFloor = ResourceFactory
				.createProperty(propertyURIHasFloor);
		Property propertyHasGeometry = ResourceFactory
				.createProperty(propertyURIHasGeometry);
		Property propertyLat = ResourceFactory
				.createProperty(propertyURILat);
		Property propertyLong = ResourceFactory
				.createProperty(propertyURILong);
		Property propertyUser = ResourceFactory.createProperty(propertyURIUser);
		Property propertyIsRoomIn = ResourceFactory
				.createProperty(propertyURIIsRoomIn);
		Property propertyIsLocatedIn = ResourceFactory
				.createProperty(propertyURIIsLocatedIn);
		Property propertyIsFloorIn = ResourceFactory
				.createProperty(propertyURIIsFloorIn);
		Property propertyConnects = ResourceFactory
				.createProperty(propertyURIConnects);
		Property propertyType = ResourceFactory
				.createProperty(propertyURIType);
		Property propertyName = ResourceFactory
				.createProperty(propertyURIName);
		Property propertyInEscapePlan = ResourceFactory
				.createProperty(propertyURIInEscapePlan);
		Property propertyId = ResourceFactory
				.createProperty(propertyURIId);
 
		// create basic uri (the buildings uri)
		String basicURI = null;
		if(floorPlan.getBuildingURI() != null){
			basicURI = floorPlan.getBuildingURI();			
		}
		else {
			basicURI = prefixURIbuilding + floorPlan.getBuildingName().replaceAll("\\s","");			
		}
		// create basic URI escape plan (used as prefix for floors, rooms, etc)
		String basicURIEscapePlan = basicURI + prefixFloor + floorPlan.getFloorNumber() + prefixEscapePlan + floorPlan.getId();
		
		
		
		//create floor
		Resource floor = model.createResource(basicURI + prefixFloor + floorPlan.getFloorNumber());
		// Floor hasFloorNumber
		floor.addProperty(propertyHasFloorNumber, Integer.toString(floorPlan.getFloorNumber()));
		// Floor type Floor
		floor.addProperty(propertyType, model.createResource(typeURIFloor));
			//create escape plan 
			Resource escapePlan = model.createResource(basicURIEscapePlan);
		 	// EscapePlan type EsacpePlan
			escapePlan.addProperty(propertyType, model.createResource(typeURIEscapePlan));
			// EscapePlan hasSourceImage
			if (floorPlan.getSourceFloorPlanImageUri() != null)
			escapePlan.addProperty(propertyHasSourceImage, floorPlan.getSourceFloorPlanImageUri().toString());			
			// EscapepPlan hasCroppedImage
			if (floorPlan.getCroppedFloorPlanImageUri() != null)
			escapePlan.addProperty(propertyHasCroppedImage, floorPlan.getCroppedFloorPlanImageUri().toString());
			// EscapepPlan id
			escapePlan.addProperty(propertyId, floorPlan.getId());
		// Floor hasEsacpePlan
		floor.addProperty(propertyHasEscapePlan, escapePlan);
		
		if(floorPlan.getBuildingURI() != null){
			// Floor isIn Building
			floor.addProperty(propertyIsFloorIn, model.createResource(floorPlan.getBuildingURI()));	
		} else {
			// create building
			Resource building = model.createResource(basicURI);
		 	// Building type Building
			building.addProperty(propertyType, model.createResource(typeURIBuilding));
			// Building name
			building.addProperty(propertyName, floorPlan.getBuildingName());
		 	// Building hasAccess
		 		// Access type Access
		// Floor isIn Building
		floor.addProperty(propertyIsFloorIn, building);			
		}

		
		/*
		 *
		 * create rooms
		 * create all connections
		 * than add references from rooms to connections
		 * and than from connections to rooms
		 */
		
		// create empty lists to put references to rooms and connections
		ArrayList<Resource> resourcesRooms = new ArrayList<Resource>();
		ArrayList<Resource> resourcesCorridors = new ArrayList<Resource>();
		ArrayList<Resource> resourcesDoors = new ArrayList<Resource>();
		ArrayList<Resource> resourcesEntrances = new ArrayList<Resource>();
		ArrayList<Resource> resourcesStairs = new ArrayList<Resource>();
		ArrayList<Resource> resourcesElevators = new ArrayList<Resource>();
		
		// create rooms
		int roomIndex = 0;
		for (Room r : floorPlan.getRooms()){

			roomIndex++;

					//create Room
					String roomURI = null;
					if(r.getName() != null && r.getName().length() > 0){
						roomURI = basicURIEscapePlan + prefixRoom + r.getName().replaceAll("\\s","");						
					} else {
						roomURI = basicURIEscapePlan + prefixRoom + roomIndex;
					}

					Resource room = model.createResource(roomURI);
					
					// Room type Room
					room.addProperty(propertyType, model.createResource(typeURIRoom));
					// Room name name
					if (r.getName() != null)
					room.addProperty(propertyName, r.getName());

					//create LocalCoordinates
					Resource localCoordinates = model.createResource(roomURI +  prefixLocalCoordinates);
					// LocalCoordinates type LocalCoordinates
					localCoordinates.addProperty(propertyType, model.createResource(typeURILocalCoordinates));
					// LocalCoordinates inEscapePlan
					localCoordinates.addProperty(propertyInEscapePlan, escapePlan);
					// LocalCoordinates hasGeometry
					localCoordinates.addProperty(propertyHasGeometry, ((Point) r.getGeometry()).toString());
				 	// Room hasLocalCoordinates
					room.addProperty(propertyHasLocalCoordinates, localCoordinates);

					//Persons
					for (Person p : r.getPersons()){

						// create Person
						Resource person = model.createResource(basicURI + prefixPerson + p.getName().replaceAll("\\s",""));
						if(p.getUri() != null){
							person = model.createResource(p.getUri());							
						}							
					 		// Person type Person
							person.addProperty(propertyType, model.createResource(typeURIPerson));
				 			// Person name
							person.addProperty(propertyName, p.getName());
							// Room user *
							room.addProperty(propertyUser, person);							
//						}
					}
					
					// Floor hasRoom *
					floor.addProperty(propertyHasRoom, room);
					// add to list to make cross references later
					resourcesRooms.add(room);
		}
		
		
		// create corridors
		int i = 0; // used to give names to the corridors
		for (Corridor c : floorPlan.getCorridors()){
			i++;
			
			//create Room
			Resource room = model.createResource(basicURIEscapePlan + prefixRoom + "corridor" + i);
			// Room type Room
			room.addProperty(propertyType, model.createResource(typeURIRoom));
			
			//create LocalCoordinates
			Resource localCoordinates = model.createResource(basicURIEscapePlan + prefixRoom + "corridor" + i + prefixLocalCoordinates);
			// LocalCoordinates type LocalCoordinates
			localCoordinates.addProperty(propertyType, model.createResource(typeURILocalCoordinates));
			// LocalCoordinates inEscapePlan
			localCoordinates.addProperty(propertyInEscapePlan, escapePlan);
			for (String lineString : ((MultiLine) c.getGeometry()).toStringArray()){
				// LocalCoordinates hasGeometry
				localCoordinates.addProperty(propertyHasGeometry, lineString);				
			}			
		 	// Room hasLocalCoordinates
			room.addProperty(propertyHasLocalCoordinates, localCoordinates);

			// Floor hasRoom *
			floor.addProperty(propertyHasRoom, room);
			
			// add to list to make cross references later
			resourcesCorridors.add(room);
		}
		

		
		
		
		// create doors
		i = 0; // used to give names to the doors
		for (Door d : floorPlan.getDoors()){
		i++;	

			//create Door
			Resource door = model.createResource(basicURIEscapePlan + prefixDoor + i);
			// Door type Door
			door.addProperty(propertyType, model.createResource(typeURIDoor));
			
			//create LocalCoordinates
			Resource localCoordinates = model.createResource(basicURIEscapePlan + prefixDoor + i + prefixLocalCoordinates);
			// LocalCoordinates type LocalCoordinates
			localCoordinates.addProperty(propertyType, model.createResource(typeURILocalCoordinates));
			// LocalCoordinates inEscapePlan
			localCoordinates.addProperty(propertyInEscapePlan, escapePlan);
			// LocalCoordinates hasGeometry
			localCoordinates.addProperty(propertyHasGeometry, ((Line) d.getGeometry()).toString());
		 	// Door hasLocalCoordinates
			door.addProperty(propertyHasLocalCoordinates, localCoordinates);
			
			// add to list to make cross references later
			resourcesDoors.add(door);
		}
		

		// create entrances
		i = 0; // used to give names to the entrances
		for (Entrance e : floorPlan.getEntrances()){
			i++;	

			//create Entrance
			Resource entrance = model.createResource(basicURIEscapePlan + prefixEntrance + i);
			// Door type Door
			entrance.addProperty(propertyType, model.createResource(typeURIEntrance));
			
			//create LocalCoordinates
			Resource localCoordinates = model.createResource(basicURIEscapePlan + prefixEntrance + i + prefixLocalCoordinates);
			// LocalCoordinates type LocalCoordinates
			localCoordinates.addProperty(propertyType, model.createResource(typeURILocalCoordinates));
			// LocalCoordinates inEscapePlan
			localCoordinates.addProperty(propertyInEscapePlan, escapePlan);
			// LocalCoordinates hasGeometry
			localCoordinates.addProperty(propertyHasGeometry, ((Point) e.getGeometry()).toString());
		 	// Entrance hasLocalCoordinates
			entrance.addProperty(propertyHasLocalCoordinates, localCoordinates);
			
			// add to list to make cross references later
			resourcesEntrances.add(entrance);
			
			
			// if is entrance to outdoors, check of coordinate was set, if yes, add it
			if (e.getType() == Connection.CONNECTION_TYPE_ENTRANCE_OUTDOOR){
				EntranceOutdoor eOutdoor = (EntranceOutdoor) e;
				
				if( eOutdoor.getPositionOutdoors() != null){
					
					//create GlobalCoordinates
					Resource globalCoordinates = model.createResource(basicURIEscapePlan + prefixEntrance + i + prefixGlobalCoordinates);
					// GlobalCoordinates type GlobalCoordinates
					globalCoordinates.addProperty(propertyType, model.createResource(typeURIGlobalCoordinates));
					// GlobalCoordinates hasGeometry
					globalCoordinates.addProperty(propertyHasGeometry, "Point((" + eOutdoor.getPositionOutdoors().getLatitudeE6()/1E6 + " " + eOutdoor.getPositionOutdoors().getLongitudeE6()/1E6 + "))");
				 	// Entrance hasGlobalCoordinates
					entrance.addProperty(propertyHasGlobalCoordinates, globalCoordinates);					
					
					
				}
			} else {
				// if is indoor entrance write second position
				EntranceIndoor eIndoor = (EntranceIndoor) e;
				//create LocalCoordinates
				localCoordinates = model.createResource(basicURIEscapePlan + prefixEntrance + i + prefixLocalCoordinates + "B");
				// LocalCoordinates type LocalCoordinates
				localCoordinates.addProperty(propertyType, model.createResource(typeURILocalCoordinates));
				// LocalCoordinates inEscapePlan
				localCoordinates.addProperty(propertyInEscapePlan, model.createResource(eIndoor.getFloorPlanB().getEscapePlanURI()));
				// LocalCoordinates hasGeometry
				localCoordinates.addProperty(propertyHasGeometry, ((Point) eIndoor.getGeometryB()).toString());
			 	// Entrance hasLocalCoordinates
				entrance.addProperty(propertyHasLocalCoordinates, localCoordinates);
			}
		}		
		
		// create stairs
		i = 0; // used to give names to the stairs
		for (Stairs s : floorPlan.getStairs()){
			i++;	

			//create Entrance
			Resource stairs = model.createResource(basicURIEscapePlan + prefixStairCase + i);
			// Door type Door
			stairs.addProperty(propertyType, model.createResource(typeURIStairCase));
			
			//create LocalCoordinates
			Resource localCoordinates = model.createResource(basicURIEscapePlan + prefixStairCase + i + prefixLocalCoordinates);
			// LocalCoordinates type LocalCoordinates
			localCoordinates.addProperty(propertyType, model.createResource(typeURILocalCoordinates));
			// LocalCoordinates inEscapePlan
			localCoordinates.addProperty(propertyInEscapePlan, escapePlan);
			// LocalCoordinates hasGeometry
			localCoordinates.addProperty(propertyHasGeometry, ((Point) s.getGeometry()).toString());
		 	// Stairs hasLocalCoordinates
			stairs.addProperty(propertyHasLocalCoordinates, localCoordinates);
			
			// add destinations
			int destIndex = 1;
			for (Geometry d : s.getDestinations()){
				if(d.getClass() != EmptyGeometry.class){
					//create LocalCoordinates
					localCoordinates = model.createResource(basicURIEscapePlan + prefixElevator + i + prefixLocalCoordinates + "d" + destIndex);
					// LocalCoordinates type LocalCoordinates
					localCoordinates.addProperty(propertyType, model.createResource(typeURILocalCoordinates));
					// LocalCoordinates inEscapePlan
					localCoordinates.addProperty(propertyInEscapePlan, d.getFloorPlan().getEscapePlanURI());
					// LocalCoordinates hasGeometry
					localCoordinates.addProperty(propertyHasGeometry, ((Point) s.getGeometry()).toString());
				 	// Elevator hasLocalCoordinates
					stairs.addProperty(propertyHasLocalCoordinates, localCoordinates);				
					destIndex++;	
				}
			}
			
			// add to list to make cross references later
			resourcesStairs.add(stairs);
		}

		// create elevators
		i = 0; // used to give names to the elevators
		for (Elevator e : floorPlan.getElevators()){
			i++;	

			//create Elevator
			Resource elevator = model.createResource(basicURIEscapePlan + prefixElevator + i);
			// Elevator type Elevator
			elevator.addProperty(propertyType, model.createResource(typeURIElevator));
			
			//create LocalCoordinates
			Resource localCoordinates = model.createResource(basicURIEscapePlan + prefixElevator + i + prefixLocalCoordinates);
			// LocalCoordinates type LocalCoordinates
			localCoordinates.addProperty(propertyType, model.createResource(typeURILocalCoordinates));
			// LocalCoordinates inEscapePlan
			localCoordinates.addProperty(propertyInEscapePlan, escapePlan);
			// LocalCoordinates hasGeometry
			localCoordinates.addProperty(propertyHasGeometry, ((Point) e.getGeometry()).toString());
		 	// Elevator hasLocalCoordinates
			elevator.addProperty(propertyHasLocalCoordinates, localCoordinates);
			
			// add destinations
			int destIndex = 1;
			for (Geometry d : e.getDestinations()){
				if(d.getClass() != EmptyGeometry.class){
					//create LocalCoordinates
					localCoordinates = model.createResource(basicURIEscapePlan + prefixElevator + i + prefixLocalCoordinates + "d" + destIndex);
					// LocalCoordinates type LocalCoordinates
					localCoordinates.addProperty(propertyType, model.createResource(typeURILocalCoordinates));
					// LocalCoordinates inEscapePlan
					localCoordinates.addProperty(propertyInEscapePlan, d.getFloorPlan().getEscapePlanURI());
					// LocalCoordinates hasGeometry
					localCoordinates.addProperty(propertyHasGeometry, ((Point) e.getGeometry()).toString());
				 	// Elevator hasLocalCoordinates
					elevator.addProperty(propertyHasLocalCoordinates, localCoordinates);				
					destIndex++;	
				}
			}
			
			// add to list to make cross references later
			resourcesElevators.add(elevator);
		}		
		
		// add references from rooms to connections and vice versa
		for (int j = 0; j < resourcesRooms.size(); j++){
			Room objectRoom = floorPlan.getRooms().get(j);
			Resource resourceRoom = resourcesRooms.get(j);
			

			// check doors
			for(int k = 0; k < resourcesDoors.size(); k++){
				Door objectDoor = floorPlan.getDoors().get(k);
				Resource resourceDoor = resourcesDoors.get(k);
				
				// if they have overlapping geometries create references
				if(((Line) objectDoor.getGeometry()).getPointA().equals((Point) objectRoom.getGeometry()) || ((Line) objectDoor.getGeometry()).getPointB().equals((Point) objectRoom.getGeometry())){
					// from room to door
					resourceRoom.addProperty(propertyHasConnection, resourceDoor);
					// from door to room
					resourceDoor.addProperty(propertyConnects, resourceRoom);
				}
			}
			
			// check entrances
			for(int k = 0; k < resourcesEntrances.size(); k++){
				Entrance objectEntrance = floorPlan.getEntrances().get(k);
				Resource resourceEntrance = resourcesEntrances.get(k);
				
				// if they have overlapping geometries create references
				if(((Point) objectEntrance.getGeometry()).equals((Point) objectRoom.getGeometry())){
					// from room to door
					resourceRoom.addProperty(propertyHasConnection, resourceEntrance);
					// from door to room
					resourceEntrance.addProperty(propertyConnects, resourceRoom);
				}
			}
			
			// check stairs
			for(int k = 0; k < resourcesStairs.size(); k++){
				Stairs objectStairs = floorPlan.getStairs().get(k);
				Resource resourceStairs = resourcesStairs.get(k);
				
				// if they have overlapping geometries create references
				if(((Point) objectStairs.getGeometry()).equals((Point) objectRoom.getGeometry())){
					// from room to door
					resourceRoom.addProperty(propertyHasConnection, resourceStairs);
					// from door to room
					resourceStairs.addProperty(propertyConnects, resourceRoom);
				}
			}
			
			// check elevators
			for(int k = 0; k < resourcesElevators.size(); k++){
				Elevator objectElevator = floorPlan.getElevators().get(k);
				Resource resourceElevator = resourcesElevators.get(k);
				
				// if they have overlapping geometries create references
				if(((Point) objectElevator.getGeometry()).equals((Point) objectRoom.getGeometry())){
					// from room to door
					resourceRoom.addProperty(propertyHasConnection, resourceElevator);
					// from door to room
					resourceElevator.addProperty(propertyConnects, resourceRoom);
				}
			}
			
		}

		// add references from corridors to connections and vice versa

		// write to string
		StringWriter sw = new StringWriter();
		/*
		 * The language in which to write the model is specified by the 
		 * lang argument. Predefined values are "RDF/XML", "RDF/XML-ABBREV", 
		 * "N-TRIPLE", "TURTLE", (and "TTL") and "N3". The default value, 
		 * represented by null, is "RDF/XML".
		 */
		model.write(sw, "N-TRIPLE");	
		model.write(getOutputStreamToFile());
		return sw.toString();
	}
	
	private OutputStream getOutputStreamToFile(){
		
		// create a File object for the parent directory
		String pathSD  = Environment.getExternalStorageDirectory().getPath();
		File outputDirectory = new File(pathSD + "/CampusMapper/");
		// have the object build the directory structure, if needed.
		outputDirectory.mkdirs();
		
		String fileName = "temp.ttl";
		
		// create output
		File newTTlfile = new File(outputDirectory, fileName);
		try {
			newTTlfile.createNewFile();
		} catch (IOException e) {
			Log.e("IOException", "Exception in create new File(");
		}

		FileOutputStream fileos = null;
		try {
			fileos = new FileOutputStream(newTTlfile);

		} catch (FileNotFoundException e) {
			Log.e("FileNotFoundException", e.toString());
		}
	
		
		return fileos;
	}
	
	
	
}

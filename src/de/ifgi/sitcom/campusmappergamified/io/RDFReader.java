package de.ifgi.sitcom.campusmappergamified.io;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.osmdroid.util.GeoPoint;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.util.FileManager;

import de.ifgi.sitcom.campusmappergamified.activities.MyCampusMapperGame;
import de.ifgi.sitcom.campusmappergamified.game.PlayerAndBuilding;
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
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Line;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.MultiLine;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;

public class RDFReader {
	
    // Set the SPARQL endpoint URI
    private static final String SPARQL_ENDPOINT_URI = "http://data.uni-muenster.de:8080/openrdf-workbench/repositories/indoormapping/query";
    
//    private QueryExecution mLocalQueryExecution;

    
    public FloorPlan loadFloorPlanFromSD(FloorPlan floorPlan){
    	
    	
    	 // load model
        Model model = getLocalFloorPlanModel();
        
        if(model == null) {
        	Log.e("local model error", "could not load model");
        	return null;
        }

        
        StmtIterator iter = model.listStatements();
            while (iter.hasNext()) {
                Log.v("model", iter.nextStatement().toString());
            }


    	 floorPlan = getFloorPlanFromSD(model, floorPlan);
    	 
 		getRooms(floorPlan, model);
 		getDoors(floorPlan, model);
 		getEntrances(floorPlan, model);
 		getStairs(floorPlan, model);
 		getElevators(floorPlan, model);
    	 
//        // Important - free up resources used running the query
//		if (mLocalQueryExecution != null) mLocalQueryExecution.close(); 
 		
    	return floorPlan;
    }
    
    public boolean localDataAvailable(){
      	 // get file
     	File sdcard = Environment.getExternalStorageDirectory();
     	File file = new File(sdcard,"CampusMapper/temp.ttl");
     	
     	return file.exists();
    	
    }
    
    private FloorPlan getFloorPlanFromSD(Model model, FloorPlan floorPlan){
    	
   	 String queryString = "SELECT DISTINCT ?floor ?escapePlan ?source ?cropped ?id WHERE {"
 			+ "?floor a <http://vocab.lodum.de/limap/floor> ."
 				+ "?floor <http://vocab.lodum.de/limap/hasEscapePlan> ?escapePlan ."
 				+ "?escapePlan <http://vocab.lodum.de/limap/hasSourceImage> ?source ."
 				+ "?escapePlan <http://vocab.lodum.de/limap/hasCroppedImage> ?cropped ."
 				+ "?escapePlan <http://vocab.lodum.de/limap/id> ?id ."
 				+ "}";
    	  
		QueryExecution queryExecution = null;
		if (model != null)
			queryExecution = QueryExecutionFactory.create(queryString, model);
		ResultSet resultSet = query(queryString, queryExecution);
		if (resultSet == null)
			return null;
     	
     
     // Iterate through all resulting rows
     while (resultSet.hasNext()) {
     	
     	QuerySolution solution = resultSet.next();
     	
     	Log.v("solution", solution.toString());
     	
     	if (solution.get("escapePlan") != null) {

     		String floorURI = solution.getResource("floor").getURI();
     		String escapePlan = solution.getResource("escapePlan").getURI();
     		String source = solution.getLiteral("source").getLexicalForm();
     		String cropped = solution.getLiteral("cropped").getLexicalForm();
     		String id = solution.getLiteral("id").getLexicalForm();
     				
     		floorPlan.setFloorURI(floorURI);
     		floorPlan.setEscapePlanURI(escapePlan);
     		floorPlan.setSourceFloorPlanImageUri(Uri.parse(source));
     		floorPlan.setCroppedFloorPlanImageUri(Uri.parse(cropped));
     		floorPlan.setId(id);
     	
     		return floorPlan;
     	}
     }
     if(queryExecution != null ) queryExecution.close();
     
  	Log.e("model to floorplan error", "could not parse floorPlan from model");
   	 return floorPlan;
    }
    
    private Model getLocalFloorPlanModel(){
    	
   	 // create model
   	 Model model = ModelFactory.createDefaultModel();

   	 // get file
 	File sdcard = Environment.getExternalStorageDirectory();
 	File file = new File(sdcard,"CampusMapper/temp.ttl");
   	 
   	 // get Input stream
 	InputStream in = null;
   	 

    	try {
    		in = FileManager.get().open(file.getAbsolutePath());
         	 model.read(in, "N-TRIPLE");
    		in.close();
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    		return null;
    	}
    	
    	
    	return model;
    }

    
	
	/*
	 * returns list of all available floorplans in particular floor in building defined by URI
	 */
	public ArrayList<FloorPlan> getFloorPlanList(String buildingURI, String floorNumber, String buildingName){
		
		// getEscapePlans with imageURIs and Ids
		String queryString = "SELECT DISTINCT ?floor ?escapePlan ?source ?cropped ?id WHERE {"
				+"?floor a <http://vocab.lodum.de/limap/floor> ;"
				+"<http://vocab.lodum.de/limap/isFloorIn> <" + buildingURI +"> ;"
				+"<http://vocab.lodum.de/limap/hasFloorNumber> \"" + floorNumber + "\" ."
				+"?floor <http://vocab.lodum.de/limap/hasEscapePlan> ?escapePlan ."
				+"?escapePlan <http://vocab.lodum.de/limap/hasSourceImage> ?source ."
				+"?escapePlan <http://vocab.lodum.de/limap/hasCroppedImage> ?cropped ."
				+"?escapePlan <http://vocab.lodum.de/limap/id> ?id ."
				+"} ORDER BY ?id";
        ResultSet resultSet = query(queryString, null);
        if (resultSet == null) return null;
        
        // Setup a place to house results for output
		ArrayList<FloorPlan> floorPlans = new ArrayList<FloorPlan>();

        // Iterate through all resulting rows
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();
        	
        	Log.v("solution", solution.toString());
        	
        	if (solution.get("escapePlan") != null) {

        		String floorURI = solution.getResource("floor").getURI();
        		String escapePlan = solution.getResource("escapePlan").getURI();
        		String source = solution.getLiteral("source").getLexicalForm();
        		String cropped = solution.getLiteral("cropped").getLexicalForm();
        		String id = solution.getLiteral("id").getLexicalForm();
        				
        		FloorPlan floorPlan = new FloorPlan();
        		floorPlan.setFloorURI(floorURI);
        		floorPlan.setEscapePlanURI(escapePlan);
        		floorPlan.setSourceFloorPlanImageUri(Uri.parse(source));
        		floorPlan.setCroppedFloorPlanImageUri(Uri.parse(cropped));
        		floorPlan.setId(id);
        		floorPlan.setBuildingURI(buildingURI);
        		floorPlan.setFloorNumber(Integer.parseInt(floorNumber));
        		floorPlan.setBuildingName(buildingName);
        	
        		floorPlans.add(floorPlan);
        	}
        }
		
		return floorPlans;
	}
	
	
	/*
	 * returns all data available for one particular floorplan
	 */
	public void getFloorPlan (FloorPlan floorPlan){

		
		/*
		 *  getRooms (including corridors)
		 *  
		 *  getLocalCoordinates
		 *  can be room or corridor
		 *  
		 *  getPersons
		 *  getNames (of Persons)
		 */
		getRooms(floorPlan, null);
		
		/*
		 *  getDoors
		 *  
		 *  getLocalCoordinates
		 */
		getDoors(floorPlan, null);
		
		/*
		 *  getEntrances
		 *  
		 *  getLocalCoordinates
		 *  If more than one, check which is in which plan
		 *  
		 *  getGlobalCoordinates
		 */
		getEntrances(floorPlan, null);
		
		/*
		 *  getStairs
		 *  
		 *  getLocalCoordinates
		 *  If more than one, check which is in which plan
		 */
		getStairs(floorPlan, null);
		
		/*
		 *  getElevators
		 *  
		 *  getLocalCoordinates
		 *  If more than one, check which is in which plan
		 */
		getElevators(floorPlan, null);
		
	}
	
	



	private FloorPlan getRooms(FloorPlan floorPlan, Model model){
		
		String queryString = "SELECT DISTINCT ?room WHERE {"
				+"<" + floorPlan.getFloorURI()+"> <http://vocab.lodum.de/limap/hasRoom> ?room ;"
				+"}";
		
		QueryExecution queryExecution = null;
		if(model != null)
		queryExecution = QueryExecutionFactory.create(queryString, model);
        ResultSet resultSet = query(queryString, queryExecution);
        
        ArrayList<Room> rooms = new ArrayList<Room>();
        ArrayList<Corridor> corridors = new ArrayList<Corridor>();
        
        
        // Iterate through all resulting rows
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();
        	
        	Log.v("solution rooms", solution.toString());
        	
        	if (solution.get("room") != null) {

        		String roomURI = solution.getResource("room").getURI();
        		ArrayList<Geometry> geometries = getLocalCoordinates(roomURI, model);
        		
        		if (geometries.size() == 1 && geometries.get(0).getClass() == Point.class){
        			// should be a room than
        			
        			// get room name
        			String roomName = getRoomName(roomURI, model);
        			
        			// get persons and add new room
        			rooms.add(new Room(geometries.get(0), roomName, getPersons(roomURI, model)));
        			
        		} else {
        			// should be a corridor than
        			ArrayList<Line> lines = new ArrayList<Line>();
        			for(Geometry g : geometries){
        				lines.add((Line) g);
        			}
        			MultiLine ml = new MultiLine(lines);
        			corridors.add(new Corridor(ml));
        		}
        		
        		
        	}
        }
        if(queryExecution != null ) queryExecution.close();
        
		floorPlan.setRooms(rooms);
		floorPlan.setCorridors(corridors);
		return floorPlan;
	}

	private ArrayList<Person> getPersons(String roomURI, Model model){
		String queryString = "SELECT DISTINCT ?user ?name WHERE {"
				+"<" + roomURI+"> <http://vocab.lodum.de/limap/user> ?user ."
				+"?user <http://xmlns.com/foaf/0.1/name> ?name ."
				+"}";
		
		QueryExecution queryExecution = null;
		if(model != null)
		queryExecution = QueryExecutionFactory.create(queryString, model);
        ResultSet resultSet = query(queryString, queryExecution);

        ArrayList<Person> persons = new ArrayList<Person>();
        
        // Iterate through all resulting rows
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();	
        	Log.v("solution person", solution.toString());
        	
        	if (solution.get("name") != null) {

        		Person person = new Person(solution.getLiteral("name").getLexicalForm());
        		person.setUri(solution.getResource("user").getURI());
        		persons.add(person);
        	}
        }
        if(queryExecution != null ) queryExecution.close();

        
        return persons;
	}
	
	private String getRoomName(String roomURI, Model model){
		String queryString = "SELECT DISTINCT ?name WHERE {"
				+"<" + roomURI+"> <http://xmlns.com/foaf/0.1/name> ?name ;"
				+"}";
		
		QueryExecution queryExecution = null;
		if(model != null)
		queryExecution = QueryExecutionFactory.create(queryString, model);
        ResultSet resultSet = query(queryString, queryExecution);

        // Iterate through all resulting rows
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();	
        	Log.v("solution room name", solution.toString());
        	
        	if (solution.get("name") != null) {

        		return solution.getLiteral("name").getLexicalForm();
        	}
        }
        if(queryExecution != null ) queryExecution.close();

        
        return null;
	}
	

	
	
	
	
	
	private ArrayList<Geometry> getLocalCoordinates(String uriString, Model model){
		ArrayList<Geometry> geometries = new ArrayList<Geometry> ();

		String queryString = "SELECT DISTINCT ?geo ?escapePlan ?floorNumber WHERE { "
				+"<" + uriString +"> <http://vocab.lodum.de/limap/hasLocalCoordinates> ?localCoordinates ."
				+"?localCoordinates <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/hasGeometry> ?geo ."
				+"?localCoordinates <http://vocab.lodum.de/limap/inEscapePlan> ?escapePlan ."
				+"?floor <http://vocab.lodum.de/limap/hasEscapePlan> ?escapePlan ."
				+"?floor <http://vocab.lodum.de/limap/hasFloorNumber> ?floorNumber ."
				+"}";
		
		QueryExecution queryExecution = null;
		if(model != null)
		queryExecution = QueryExecutionFactory.create(queryString, model);
        ResultSet resultSet = query(queryString, queryExecution);
        
        // Iterate through all resulting rows
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();
        	
        	Log.v("solution local coordinates", solution.toString());
        	
        	if (solution.get("geo") != null) {
        		/*
        		 * either this form: "Line((320 281, 353 108))"
        		 * or that: "Point((349 407))"
        		 */
        		String geoString = solution.getLiteral("geo").getLexicalForm();
        		String escapePlanURI = solution.getResource("escapePlan").getURI();
        		String floorNumber = solution.getLiteral("floorNumber").getLexicalForm();

        		// parse geo string
        		Geometry geo = stringToGeometry(geoString);
        		geo.setEscapePlanURI(escapePlanURI);
        		FloorPlan fp = new FloorPlan();
        		fp.setFloorNumber(Integer.parseInt(floorNumber));
        		geo.setFloorPlan(fp);
        		geometries.add(geo);
        	}
        }
        if(queryExecution != null ) queryExecution.close();
		
		
		return geometries;
	}
	
	private GeoPoint getGlobalCoordinates(String uriString, Model model){

		String queryString = "SELECT DISTINCT ?geo WHERE {"
				+"<" + uriString +"> <http://vocab.lodum.de/limap/hasGlobalCoordinates> ?globalCoordinates ."
				+"?globalCoordinates <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/hasGeometry> ?geo ."
				+"}";
		
		QueryExecution queryExecution = null;
		if(model != null)
		queryExecution = QueryExecutionFactory.create(queryString, model);
        ResultSet resultSet = query(queryString, queryExecution);
        
        // Iterate through all resulting rows
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();
        	
        	Log.v("solution local coordinates", solution.toString());
        	
        	if (solution.get("geo") != null) {
        		/*
        		 * form: "Point((34.232 4.232323))"
        		 */
        		String geoString = solution.getLiteral("geo").getLexicalForm();

        		// parse geo string
        		return stringToGeoPoint(geoString);
        	}
        }
        if(queryExecution != null ) queryExecution.close();
		
		return null;
	}

	private GeoPoint stringToGeoPoint(String geoString){
		
		GeoPoint geo = null;
	
        // find last appearance of "("
		int start = geoString.lastIndexOf("(") + 1;
        // find first appearance of ")"
		int end = geoString.indexOf(")");
		
		// create substring
		// in between we have our coordinates
		geoString = geoString.substring(start, end);
		
		
			// it is a point than
			// split the pair at the blank to get x respectively y
			try {
				String [] latLong = geoString.split(" ");
				geo = new GeoPoint(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0]));		
			} catch (NumberFormatException e) {
				Log.e("debug", "error while parsing Point.");
			}
			
		return  geo;
	}
	
	private Geometry stringToGeometry(String geoString){
		
		Geometry geo = null;
		
        // find last appearance of "("
		int start = geoString.lastIndexOf("(") + 1;
        // find first appearance of ")"
		int end = geoString.indexOf(")");
		
		// create substring
		// in between we have our coordinates
		geoString = geoString.substring(start, end);
		
		// split where we have ", " to get pairs of lat and long
		String [] xyPairs = geoString.split(", ");
		
		if(xyPairs.length == 1){
			// it is a point than
			// split the pair at the blank to get x respectively y
			try {
				String [] xAndY1 = xyPairs[0].split(" ");
				geo = new Point(Integer.parseInt(xAndY1[0]), Integer.parseInt(xAndY1[1]));		
			} catch (NumberFormatException e) {
				Log.e("debug", "error while parsing Point.");
			}
		} else {
			// it is a line than
			// split the pairs at the blank to get x respectively y
				try {
					String [] xAndY1 = xyPairs[0].split(" ");
					String [] xAndY2 = xyPairs[1].split(" ");
					geo = new Line(new Point(Integer.parseInt(xAndY1[0]), Integer.parseInt(xAndY1[1])), 
							new Point(Integer.parseInt(xAndY2[0]), Integer.parseInt(xAndY2[1])));		
				} catch (NumberFormatException e) {
					Log.e("debug", "error while parsing Line.");
				}
		}
			
		return  geo;
	}
	

	private void getElevators(FloorPlan floorPlan, Model model) {

        /*
         * elevator with local coordinates in the appropriate escape plan...
         * 
         * same goes for the other conections
         */
		String queryString = "SELECT DISTINCT ?elevator WHERE {"
		        + "?elevator a <http://vocab.lodum.de/limap/elevator> ."
		        + "?elevator <http://vocab.lodum.de/limap/hasLocalCoordinates> ?locCoords ."
		        + "?locCoords <http://vocab.lodum.de/limap/inEscapePlan> <" + floorPlan.getEscapePlanURI() + "> ."
		        + "}";
		
		QueryExecution queryExecution = null;
		if(model != null)
		queryExecution = QueryExecutionFactory.create(queryString, model);
        ResultSet resultSet = query(queryString, queryExecution);
        
        // Iterate through all resulting rows
        ArrayList<Elevator> elevators = new ArrayList<Elevator>();
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();	
        	Log.v("solution elevator", solution.toString());
        	
        	if (solution.get("elevator") != null) {
        		String elevatorURI = solution.getResource("elevator").getURI();
        		
        		
        		// get the local coordinates
        		ArrayList<Geometry> geometries = getLocalCoordinates(elevatorURI, model);
        		
        		Geometry geometryA = null;
        		ArrayList<Geometry> destinations = new ArrayList<Geometry>();
        		
        		for(Geometry g : geometries){
        			if (g.getEscapePlanURI().equals(floorPlan.getEscapePlanURI())){
        				geometryA = g;
        			} else {
        				destinations.add(g);
        			}
        		}
        		
        		elevators.add(new Elevator(geometryA, destinations));
        	}
        }
        if(queryExecution != null ) queryExecution.close();
        
		floorPlan.setElevators(elevators);
	}


	private void getStairs(FloorPlan floorPlan, Model model) {

		String queryString = "SELECT DISTINCT ?stairs WHERE {"
		        + "?stairs a <http://vocab.lodum.de/limap/stairs> ."
		        + "?stairs <http://vocab.lodum.de/limap/hasLocalCoordinates> ?locCoords ."
		        + "?locCoords <http://vocab.lodum.de/limap/inEscapePlan> <" + floorPlan.getEscapePlanURI() + "> ."
		        + "}";
		
		QueryExecution queryExecution = null;
		if(model != null)
		queryExecution = QueryExecutionFactory.create(queryString, model);
        ResultSet resultSet = query(queryString, queryExecution);
        
        // Iterate through all resulting rows
        ArrayList<Stairs> stairs = new ArrayList<Stairs>();
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();	
        	Log.v("solution stairs", solution.toString());
        	
        	if (solution.get("stairs") != null) {
        		String stairsURI = solution.getResource("stairs").getURI();
        		
        		
        		// get the local coordinates
        		ArrayList<Geometry> geometries = getLocalCoordinates(stairsURI, model);
        		
        		Geometry geometryA = null;
        		ArrayList<Geometry> destinations = new ArrayList<Geometry>();

        		
        		for(Geometry g : geometries){
        			if (g.getEscapePlanURI().equals(floorPlan.getEscapePlanURI())){
        				geometryA = g;
        			} else {
        				destinations.add(g);
        			}
        		}
        		
        		stairs.add(new Stairs(geometryA, destinations));
        	}
        }
        if(queryExecution != null ) queryExecution.close();
        
        floorPlan.setStairs(stairs);
	}


	private void getEntrances(FloorPlan floorPlan, Model model) {
		
		String queryString = "SELECT DISTINCT ?entrance WHERE {"
		        + "?entrance a <http://vocab.lodum.de/limap/entrance> ."
		        + "?entrance <http://vocab.lodum.de/limap/hasLocalCoordinates> ?locCoords ."
		        + "?locCoords <http://vocab.lodum.de/limap/inEscapePlan> <" + floorPlan.getEscapePlanURI() + "> ."
		        + "}";
		
		QueryExecution queryExecution = null;
		if(model != null)
		queryExecution = QueryExecutionFactory.create(queryString, model);
        ResultSet resultSet = query(queryString, queryExecution);
        
        // Iterate through all resulting rows
        ArrayList<Entrance> entrances = new ArrayList<Entrance>();
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();	
        	Log.v("solution entrance", solution.toString());
        	
        	if (solution.get("entrance") != null) {
        		String entranceURI = solution.getResource("entrance").getURI();
        		
        		
        		// get the local coordinates
        		ArrayList<Geometry> geometries = getLocalCoordinates(entranceURI, model);
        		
        		FloorPlan floorPlanB = null;
        		Geometry geometryA = null;
        		Geometry geometryB = null;
        		
        		for(Geometry g : geometries){
        			if (g.getEscapePlanURI().equals(floorPlan.getEscapePlanURI())){
        				geometryA = g;
        			} else {
        				geometryB = g;
        				floorPlanB = new FloorPlan();
        				floorPlanB.setEscapePlanURI(g.getEscapePlanURI());
        			}
        		}
        		
        		if(geometryB != null){
            		entrances.add(new EntranceIndoor(geometryA, geometryB, floorPlanB));        			
        		} else {

                	// get global coordinates
        			GeoPoint globalCoordinates = getGlobalCoordinates(entranceURI, model);
        			if (globalCoordinates != null){
                		entrances.add(new EntranceOutdoor(geometryA, globalCoordinates));
        			}
        			
        		}        	
        	}
        }
        if(queryExecution != null ) queryExecution.close();
        
        floorPlan.setEntrances(entrances);
	}


	private void getDoors(FloorPlan floorPlan, Model model) {
		
		String queryString = "SELECT DISTINCT ?door WHERE {"
		        + "?door a <http://vocab.lodum.de/limap/door> ."
		        + "?door <http://vocab.lodum.de/limap/hasLocalCoordinates> ?locCoords ."
		        + "?locCoords <http://vocab.lodum.de/limap/inEscapePlan> <" + floorPlan.getEscapePlanURI() + "> ."
		        + "}";
		
		QueryExecution queryExecution = null;
		if(model != null)
		queryExecution = QueryExecutionFactory.create(queryString, model);
        ResultSet resultSet = query(queryString, queryExecution);
        
		ArrayList<Door> doors = new ArrayList<Door>();

		// Iterate through all resulting rows
		while (resultSet.hasNext()) {

			QuerySolution solution = resultSet.next();

			Log.v("solution doors", solution.toString());

			if (solution.get("door") != null) {

				String doorURI = solution.getResource("door").getURI();
				ArrayList<Geometry> geometries = getLocalCoordinates(doorURI, model);
				Line l = (Line) geometries.get(0);
				doors.add(new Door(l));

			}
		}
		if(queryExecution != null ) queryExecution.close();
		
		floorPlan.setDoors(doors);
	}
	
	
	/*
	 * returns list of all available floorplans in particular floor in building defined by URI
	 */
	public ArrayList<PlayerAndBuilding> getPlayersAndBuildings(){
		
		String queryString = "PREFIX prv: <http://purl.org/net/provenance/ns/> " +
							"PREFIX indoor:<http://data.uni-muenster.de/context/indoormapping/> " +
							"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+ 
							"SELECT ?player ?building  " + 
							"WHERE " +
							"{ " +
							"  ?player indoor:hasRelationToBuilding ?relationId . " +
							"  ?relationId rdf:type prv:BuildingScoreRelation .  " +
							"  ?relationId indoor:hasBuilding ?building . " +
							"  ?relationId indoor:hasPlayerScore ?buildingScore . " + 
							"} " +
							"ORDER BY ?building DESC(?buildingScore) ";		
	
		ResultSet resultSet = query(queryString, null);
	//	System.out.println("query performed...."+ resultSet.getRowNumber());
        if (resultSet == null) 
        	return null;
        
        // Setup a place to house results for output
		ArrayList<PlayerAndBuilding> myOwnersBuildings = new ArrayList<PlayerAndBuilding>();

        // Iterate through all resulting rows
		String buildingCodeTemp =  "";
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();
        	
        	Log.v("solution", solution.toString());
        	
        	if (solution.get("player") != null) {

        		String player = solution.get("player").toString();
        		String buildingCode = solution.get("building").toString();
        	//	Integer buildingScore = solution.getLiteral("buildingScore").getInt();
        		
        		if (!buildingCode.equals(buildingCodeTemp))
        		{
	        		PlayerAndBuilding myPlayerPerBuilding = new PlayerAndBuilding();
	        		myPlayerPerBuilding.setPlayerId(player);
	        		myPlayerPerBuilding.setBuildingId(buildingCode);
	        //		myPlayerPerBuilding.setBuildingScore(buildingScore);
	        	
	        		myOwnersBuildings.add(myPlayerPerBuilding);
        			buildingCodeTemp = buildingCode;
        		}
        	}
        }
		
		return myOwnersBuildings;
	}
	
	
	
	private ResultSet query(String queryString, QueryExecution queryExecution){
		if (queryExecution != null) return localQuery(queryString, queryExecution);
		else return hTTPQuery(queryString);
	}
	
	private ResultSet hTTPQuery(String queryString){
		
    	// Create a Query instance
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        Log.v("http query", query.toString());

        // Query uses an external SPARQL endpoint for processing
        QueryEngineHTTP qe = new QueryEngineHTTP(SPARQL_ENDPOINT_URI, query);
        String pw = "!nd00rmapping";
        qe.setBasicAuthentication("indoormapping", pw.toCharArray());
        ResultSet rs = null;
            rs = qe.execSelect();	
	        // Important - free up resources used running the query
	        qe.close();
        return rs;
	}
	
	private ResultSet localQuery(String queryString, QueryExecution queryExecution){

        ResultSet rs = null;

        try {
            // Execute the query and obtain results
            rs = queryExecution.execSelect();
		} catch (Exception e) {

			Log.e("debug", "error while querying model from file");
			e.printStackTrace();

			return null;
		}
        
        return rs;
	}
}


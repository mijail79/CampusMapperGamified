package de.ifgi.sitcom.campusmappergamified.io;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;


import android.graphics.drawable.Drawable;
import android.util.Log;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.activities.MyCampusMapperGame;
import de.ifgi.sitcom.campusmappergamified.game.PlayerAndBuilding;
import de.ifgi.sitcom.campusmappergamified.indoordata.Person;
import de.ifgi.sitcom.campusmappergamified.outdoordata.Building;

/*
 * queries to lodum triple store
 * 
 */
public class TripleStoreQueries {
	
	private ArrayList<PlayerAndBuilding> playersAndBuildings = new ArrayList<PlayerAndBuilding>();
	private ArrayList<PlayerAndBuilding> buildingsFlag = new ArrayList<PlayerAndBuilding>();

    // Set the SPARQL endpoint URI
    private static final String SPARQL_ENDPOINT_URI = "http://data.uni-muenster.de/sparql";

    
    public Building queryBuildingShape(String buildingName){
    	
    	String queryString = 
    			"prefix foaf: <http://xmlns.com/foaf/0.1/>"
    			+ "prefix lodum: <http://vocab.lodum.de/helper/>"
				+ "prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>"

				+ "SELECT DISTINCT ?lat ?long ?wkt WHERE {"

				+ "?hs a foaf:Organization ;"
       			+ "lodum:building ?building ."
       			+ "?building geo:lat ?lat ."
       			+ "?building geo:long ?long ."
       			+ "?building foaf:name ?buildingname ."
       			+ "OPTIONAL{ ?building <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/hasGeometry> ?geo ."
       			+ "?geo <http://www.opengis.net/ont/OGC-GeoSPARQL/1.0/asWKT> ?wkt ."
       			+ "}"
				+ "FILTER regex(?buildingname, '" + buildingName + "')"
				+ "}";


    	Log.v("debug", queryString);


        // Create a Query instance
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        // Query uses an external SPARQL endpoint for processing
        // This is the syntax for that type of query
        QueryExecution qe = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT_URI, query);

        ResultSet resultSet = null;
        try {

            // Execute the query and obtain results
            resultSet = qe.execSelect();	
		} catch (Exception e) {

			Log.e("debug", "error while querying LODUM store");
			return null;
		}

        // Setup a place to house results for output
        GeoPoint center = null;
        ArrayList<GeoPoint> shape = null;

        
        if(resultSet.hasNext()){
        	QuerySolution solution = resultSet.next();
        	List<String> columnNames = resultSet.getResultVars();

        	
        	// get the center
        	if (solution.get(columnNames.get(0)) != null && solution.get(columnNames.get(1)) != null) {
        		String lat = solution.getLiteral(columnNames.get(0)).getLexicalForm();
        		String lon = solution.getLiteral(columnNames.get(1)).getLexicalForm();
        		
        		try {
					center = new GeoPoint(Float.parseFloat(lat), Float.parseFloat(lon));
				} catch (NumberFormatException e) {

					Log.e("debug", "error while parsing lat long. " + lat +" " + lon);
				}
        	}
        	// get the shape
        	if (solution.get(columnNames.get(columnNames.size()-1)) != null) {
        		String literal = solution.getLiteral(columnNames.get(columnNames.size()-1)).getLexicalForm();
        		shape = polygonStringToPoints(literal);        	
        	}
        }

        // Important - free up resources used running the query
        qe.close();
        
        // Return the results as a String []
        return new Building(shape, center);
    	
    }

    /*
     *  get all the buildings with the center points to draw on the ownership activity
     */
    public ArrayList<PlayerAndBuilding> queryBuildingsCenter(){

    	RDFReader myReader = new RDFReader();
    	
    	String queryString = 
    			"prefix foaf: <http://xmlns.com/foaf/0.1/>"
    			+ "prefix lodum: <http://vocab.lodum.de/helper/>"
				+ "prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>"

				+ "SELECT DISTINCT ?building ?lat ?long WHERE {"

				+ "?hs a foaf:Organization ;"
       			+ "lodum:building ?building ."
       			+ "?building geo:lat ?lat ."
       			+ "?building geo:long ?long ."
       			+ "?building foaf:name ?buildingname ."
				+ "}";


    	Log.v("debug", queryString);


        // Create a Query instance
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        // Query uses an external SPARQL endpoint for processing
        // This is the syntax for that type of query
        QueryExecution qe = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT_URI, query);

        ResultSet resultSet = null;
        try {

            // Execute the query and obtain results
            resultSet = qe.execSelect();	
		} catch (Exception e) {

			Log.e("debug", "error while querying LODUM store");
			return null;
		}

        // Setup a place to house results for output
        GeoPoint center = null;
   //     ArrayList<GeoPoint> shape = null;
        String buildingCode = null;
        
        // call RDFReader to retrieve players and buildings
        playersAndBuildings = myReader.getPlayersAndBuildings();
        		
        while(resultSet.hasNext()){
        	QuerySolution solution = resultSet.next();
        	List<String> columnNames = resultSet.getResultVars();

        	
        	// get the center
        	if (solution.get(columnNames.get(0)) != null && solution.get(columnNames.get(1)) != null && solution.get(columnNames.get(2)) != null) {
        		buildingCode = solution.get("building").toString();
        		buildingCode = buildingCode.substring(buildingCode.lastIndexOf("/") + 1);
        		String lat = solution.get("lat").toString();
        		String lon = solution.get("long").toString();
        		
        		try {
					center = new GeoPoint(Float.parseFloat(lat), Float.parseFloat(lon));
				} catch (NumberFormatException e) {

					Log.e("debug", "error while parsing lat long. " + lat +" " + lon);
				}
        	}

        //	myBuildings.add(new Building(name, shape, center));
        	Boolean forFlag = true;
    		PlayerAndBuilding myPlayerAndBuilding = new PlayerAndBuilding();
    		
        	for(PlayerAndBuilding pb : playersAndBuildings)
        	{
        		if(buildingCode.equals(pb.getBuildingId()) && MyCampusMapperGame.getInstance().getPlayerEmail().equals(pb.getPlayerId().substring(pb.getPlayerId().indexOf(":") + 1)) )
				{
					myPlayerAndBuilding.setBuildingId(buildingCode);
					myPlayerAndBuilding.setCenter(center);
					myPlayerAndBuilding.setFlag("ic_me_flag");
					myPlayerAndBuilding.setPlayerId(pb.getPlayerId());
					forFlag = false;
					break;
				}
				else if(buildingCode.equals(pb.getBuildingId()) && !MyCampusMapperGame.getInstance().getPlayerEmail().equals(pb.getPlayerId().substring(pb.getPlayerId().indexOf(":"))) )
				{
					myPlayerAndBuilding.setBuildingId(buildingCode);
					myPlayerAndBuilding.setCenter(center);
					myPlayerAndBuilding.setFlag("ic_hasowner_flag");
					myPlayerAndBuilding.setPlayerId(pb.getPlayerId());
					forFlag = false;
					break;
				}
        	}
        	
        	if (forFlag == true)
        	{
				myPlayerAndBuilding.setBuildingId(buildingCode);
				myPlayerAndBuilding.setCenter(center);
				myPlayerAndBuilding.setFlag("ic_free_flag");
				myPlayerAndBuilding.setPlayerId("No_Owner");
        	}
        	
        	buildingsFlag.add(myPlayerAndBuilding);
        }

        // Important - free up resources used running the query
        qe.close();
        return buildingsFlag;
    }    
    
    /*
     * String should be in the format "Polygon((7.6068032 51.9598972, 7.6068615 51.9599266, ..., 7.6068032 51.9598972)) ]]>
     * filter to only have the coordinates
     */
    public ArrayList<GeoPoint> polygonStringToPoints(String polygon){

    	
    	Log.v("debug", polygon);
    	
        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();

        // find last appearance of "("
		int start = polygon.lastIndexOf("(") + 1;
        // find first appearance of ")"
		int end = polygon.indexOf(")");
		
		// create substring
		// in between we have our coordinates
		polygon = polygon.substring(start, end);
		
		// split where we have ", " to get pairs of lat and long
		String [] latLongPairs = polygon.split(", ");
		
		// split the pairs at the blank to get lat respectively long
		for (String latLong : latLongPairs){
			try {
				String [] latAndLong = latLong.split(" ");
				points.add(new GeoPoint(Float.parseFloat(latAndLong[1]), Float.parseFloat(latAndLong[0])));				
			} catch (NumberFormatException e) {
				Log.e("debug", "error while parsing lat long. " + latLong);
			}

		}
		
    	return points;
    }
    
    
	public ArrayList<Person> queryPersons(String buildingName){

    	String queryString = 
    			"prefix foaf: <http://xmlns.com/foaf/0.1/>"
    			+ "prefix lodum: <http://vocab.lodum.de/helper/>"

				+ "SELECT DISTINCT ?person ?member WHERE {"

				+ "?hs a foaf:Organization ;"
       			+ "lodum:building ?building ;"
       			+ "foaf:member ?member ."
       			+ "?member foaf:name ?person ."
       			+ "?building foaf:name ?buildingname ."
				+ "FILTER regex(?buildingname, '" + buildingName + "')"
				+ "}";
		
    	
    	Log.v("debug", queryString);


        // Create a Query instance
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        // Query uses an external SPARQL endpoint for processing
        // This is the syntax for that type of query
        QueryExecution qe = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT_URI, query);

        ResultSet resultSet = null;
        try {

            // Execute the query and obtain results
            resultSet = qe.execSelect();	
		} catch (Exception e) {

			Log.e("debug", "error while querying LODUM store");
			return null;
		}

        // Setup a place to house results for output
        ArrayList<Person> persons = new ArrayList<Person>();

        // Iterate through all resulting rows
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();
        	
        	if (solution.get("person") != null) {

        		String personName = solution.getLiteral("person").getLexicalForm();
        		String personURI = solution.getResource("member").getURI();
        		persons.add(new Person(personName, personURI));
        	}
        }

        // Important - free up resources used running the query
        qe.close();
        
        // Return the results as a String []
        return persons;

	}
	
	
    public ArrayList<Building> queryBuildings() {
        /**
         * Use the SPARQL engine and report the results
         * 
         * @return The number of resulting rows
         */
  
    	
    	String queryString = 
    			"prefix foaf: <http://xmlns.com/foaf/0.1/>"
    			+ "prefix db: <http://dbpedia.org/ontology/>"

				+ "SELECT DISTINCT * WHERE {"

				+ "?building a db:building ;"
       			+ "foaf:name ?name ."
				+ "} ORDER BY ?name";

        // Create a Query instance
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        // Query uses an external SPARQL endpoint for processing
        // This is the syntax for that type of query
        QueryExecution qe = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT_URI, query);
        
        ResultSet resultSet = null;
        try {

            // Execute the query and obtain results
            resultSet = qe.execSelect();	
		} catch (Exception e) {

			Log.e("debug", "error while querying LODUM store");
			return null;
		}

        // Setup a place to house results for output      
        ArrayList<Building> buildings = new ArrayList<Building>();

        // Iterate through all resulting rows
        while (resultSet.hasNext()) {
        	
        	QuerySolution solution = resultSet.next();
        	if (solution.get("name") != null) {        	

        		// only accept solutions that do not contain "^^"
        		if(!solution.getLiteral("name").toString().contains("^^")){
            		String buildingName = solution.getLiteral("name").getLexicalForm();
            		String buildingURI = solution.getResource("building").getURI();
            		
            		buildings.add(new Building(buildingName, buildingURI));        			
        		}
        	}
        }

        // Important - free up resources used running the query
        qe.close();
        
        return buildings;
    }
	
	
}

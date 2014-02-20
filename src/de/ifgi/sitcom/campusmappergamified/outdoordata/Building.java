package de.ifgi.sitcom.campusmappergamified.outdoordata;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import de.ifgi.sitcom.campusmappergamified.indoordata.Person;

public class Building {

	

	private String buildingName;
	// URI used in LODUM
	private String buildingURI;
	// the persons working in this building
	private ArrayList<Person> persons;
	private ArrayList<GeoPoint> shape;
	private GeoPoint center;
	
	
	public Building(){
		
	}
	
	public Building(String buildingName, String buildingURI) {
		this.buildingName = buildingName;
		this.buildingURI = buildingURI;
	}
	

	public ArrayList<GeoPoint> getShape() {
		return shape;
	}
	public void setShape(ArrayList<GeoPoint> shape) {
		this.shape = shape;
	}
	public GeoPoint getCenter() {
		return center;
	}
	public void setCenter(GeoPoint center) {
		this.center = center;
	}
	public Building(ArrayList<GeoPoint> shape, GeoPoint center) {
		super();
		this.shape = shape;
		this.center = center;
	}
	
	//new constructor to initialize the building with the name, this is used for the ownership map
	public Building(String name, ArrayList<GeoPoint> shape, GeoPoint center) {
		super();
		this.shape = shape;
		this.center = center;
		this.buildingName = name;
	}


	public String getBuildingName() {
		return buildingName;
	}


	public void setBuildingName(String buildingName) {
		this.buildingName = buildingName;
	}


	public String getBuildingURI() {
		return buildingURI;
	}


	public void setBuildingURI(String buildingURI) {
		this.buildingURI = buildingURI;
	}


	public ArrayList<Person> getPersons() {
		return persons;
	}


	public void setPersons(ArrayList<Person> persons) {
		this.persons = persons;
	}
	
	
	
	
}
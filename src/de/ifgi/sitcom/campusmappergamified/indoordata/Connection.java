package de.ifgi.sitcom.campusmappergamified.indoordata;

import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;

/*
 * connections can be doors, entrances (real entrances; i.e. entrance outdoor, or entrances that
 * connect to plans situated in the same floor; i.e. entrance indoor), stairs or elevators
 */
public class Connection extends MapElement{

	public static final int CONNECTION_TYPE_DOOR = 1;
	public static final int CONNECTION_TYPE_ENTRANCE_OUTDOOR = 2;
	public static final int CONNECTION_TYPE_ENTRANCE_INDOOR = 3;
	public static final int CONNECTION_TYPE_STAIRS = 4;
	public static final int CONNECTION_TYPE_ELEVATOR = 5;
	
	protected int type;
	
	public Connection(int type, Geometry geometry) {
		
		super(geometry);
		
		this.type = type;
		this.geometry = geometry;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}

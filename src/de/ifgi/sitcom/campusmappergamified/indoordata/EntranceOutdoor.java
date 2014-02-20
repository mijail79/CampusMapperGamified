package de.ifgi.sitcom.campusmappergamified.indoordata;


import org.osmdroid.util.GeoPoint;

import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;

/*
 * connects a room or corridor with a point outdoors (to be chosen on osm map), 
 */
public class EntranceOutdoor extends Entrance{
	
	
	private GeoPoint positionOutdoors;

	public EntranceOutdoor(Geometry geometry) {
		super(Connection.CONNECTION_TYPE_ENTRANCE_OUTDOOR, geometry);
	}
	
	

	public EntranceOutdoor(Geometry geometry, GeoPoint positionOutdoors) {
		super(Connection.CONNECTION_TYPE_ENTRANCE_OUTDOOR, geometry);
		this.positionOutdoors = positionOutdoors;
	}



	public GeoPoint getPositionOutdoors() {
		return positionOutdoors;
	}



	public void setPositionOutdoors(GeoPoint positionOutdoors) {
		this.positionOutdoors = positionOutdoors;
	}


	
	
}

package de.ifgi.sitcom.campusmappergamified.indoordata;


import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;

/*
 * connects a room or corridor with another room/ corridor on a different 
 * floor plan on the same floor
 */
public class EntranceIndoor extends Entrance{
	
	private Geometry geometryB;
	private FloorPlan floorPlanB;

	public EntranceIndoor(Geometry geometry) {
		super(Connection.CONNECTION_TYPE_ENTRANCE_INDOOR, geometry);
	}

	
	
	public EntranceIndoor(Geometry geometry, Geometry geometryB,
			FloorPlan floorPlanB) {
		super(Connection.CONNECTION_TYPE_ENTRANCE_INDOOR, geometry);
		this.geometryB = geometryB;
		this.floorPlanB = floorPlanB;
	}


	public Geometry getGeometryB() {
		return geometryB;
	}

	public void setGeometryB(Geometry geometryB) {
		this.geometryB = geometryB;
	}

	public FloorPlan getFloorPlanB() {
		return floorPlanB;
	}

	public void setFloorPlanB(FloorPlan floorPlanB) {
		this.floorPlanB = floorPlanB;
	}
	
	
}

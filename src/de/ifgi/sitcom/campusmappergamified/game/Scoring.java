package de.ifgi.sitcom.campusmappergamified.game;

public enum Scoring {
	
	DOOR(20), DOOR_WITH_ROOM(40), ROOM(20), STAIR(80), ELEVATOR(80), CORRIDOR(50), ENTRANCE(100), SELECT_TARGET(80), ROOMDESCRIPTION(50);
	
	private int value;
	
	private Scoring(int value) {
		    this.value = value;
	}

	public int getValue() {
		return value;
	}
}

package de.ifgi.sitcom.campusmappergamified.indoordata;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Geometry;

/*
 * rooms have a name
 * a room´s geometry is a point
 * a room can have an infinite number of persons
 */
public class Room extends IndoorSpace {

	private String name;
	private ArrayList<Person> persons;

	public Room(Geometry geometry, String name, ArrayList<Person> persons) {
		super(geometry);
		this.name = name;
		this.persons = persons;
		
		if (this.persons == null) this.persons = new ArrayList<Person>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Person> getPersons() {
		return persons;
	}

	public void setPersons(ArrayList<Person> persons) {
		this.persons = persons;
	}

	public void draw(Canvas canvas, boolean selected){
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		if(selected){
			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(24);
			geometry.draw(canvas, paint);
		}
		
		
		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(20);
		geometry.draw(canvas, paint);
	}
	
}

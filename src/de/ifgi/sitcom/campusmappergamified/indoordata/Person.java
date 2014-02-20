package de.ifgi.sitcom.campusmappergamified.indoordata;


/*
 * represents one person
 * persons can be added to rooms
 */
public class Person {

	private String name;
	private String uri; // the uri used in lodum store

	public Person(String name) {
		super();
		this.name = name;
	}
	
	public Person(String name, String personURI) {
		super();
		this.name = name;
		this.uri = personURI;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
	
}

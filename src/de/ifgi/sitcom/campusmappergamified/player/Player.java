package de.ifgi.sitcom.campusmappergamified.player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;
import de.ifgi.sitcom.campusmappergamified.activities.MyCampusMapperGame;
import de.ifgi.sitcom.campusmappergamified.exceptions.UploadException;
import de.ifgi.sitcom.campusmappergamified.io.RDFUpload;

public class Player  {

	public String name;
	public String email;
	public String username;
	public String registrationDate;
	public String badge = "beginner";

	public Player(String playerUsername) {
		this.setName("playername");
		this.setEmail("playeremail");
		this.setUsername(playerUsername);
		this.setBadge("beginner");
		this.setRegistrationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH).format(new Date()));
	}
	
	public String registerUser() {

        String RDFRegisterInsert;
        RDFUpload upload = new RDFUpload();
        
		final AccountManager manager = AccountManager.get(MyCampusMapperGame
				.getContext());
		final Account[] accounts = manager.getAccountsByType("com.google");

		if (accounts[0].name != null) {
			

			registrationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH).format(new Date());
			setEmail(accounts[0].name);
				
				Log.v(" Got contacts", "Username: " + username + " Email : " + email
						+ "date:" + registrationDate + "badge: " + badge);

	        RDFRegisterInsert = 
				"<indoor:"+this.getEmail()+"> rdf:type prv:Player . "+
				"<indoor:"+this.getEmail()+"> foaf:nick '" + this.getUsername() + "' . "+
				"<indoor:"+this.getEmail()+"> foaf:mbox '" + this.getEmail() + "' . "+ 
				"<indoor:"+this.getEmail()+"> indoor:hasRegistrationDate '" + this.getRegistrationDate() + "'^^xsd:dateTime . " +
				"<indoor:"+this.getEmail()+"> indoor:hasTotalPlayerScore '" + MyCampusMapperGame.getInstance().getMyScore().toString() +"'^^xsd:integer . "+
				"<indoor:"+this.getEmail()+"> indoor:hasPersonBadge '"+this.getBadge()+ "' . ";
			
		        try {
					upload.uploadRDF(RDFRegisterInsert);			
				} catch (UploadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
		}
        return email;		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBadge() {
		return badge;
	}

	public void setBadge(String badge) {
		this.badge = badge;
	}

	public String getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}

}

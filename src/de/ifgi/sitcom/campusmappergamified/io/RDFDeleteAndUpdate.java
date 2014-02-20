package de.ifgi.sitcom.campusmappergamified.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.xerces.impl.dv.util.Base64;

import de.ifgi.sitcom.campusmappergamified.exceptions.UploadException;

import android.util.Log;

/*
 * upload of sparql/ rdf data to lodum triple store
 */
public class RDFDeleteAndUpdate {

	/************* server path ****************/
	private static final String UPLOAD_SERVER_URI = "http://data.uni-muenster.de:8080/openrdf-workbench/repositories/indoormapping/update";

	public void DeleteAndUpdateRDF(String rdfString) throws UploadException{

		String DeleteAndUpdateScript =
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX foaf:<http://xmlns.com/foaf/0.1/> " +
				"PREFIX prv: <http://purl.org/net/provenance/ns/> " +
				"PREFIX indoor:<http://data.uni-muenster.de/context/indoormapping/>	" +
				rdfString;
		
		Log.v("DeleteAndUpdateScript", DeleteAndUpdateScript);

		// Construct data
		String data = null;
		try {
			data = URLEncoder.encode("update", "UTF-8") + "="
					+ URLEncoder.encode(DeleteAndUpdateScript, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new UploadException();
		}

		
		// Send data
		try {
			URL url = new URL(UPLOAD_SERVER_URI);
			URLConnection conn = url.openConnection();
			String userpass = "test:test"; // username:password
					
					String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes()));
					conn.setRequestProperty ("Authorization", basicAuth);

			// Allow Outputs
			 conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			wr.write(data);
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				Log.v("response", line);
			}
			
			// close streams
			wr.close();
			rd.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new UploadException();
		}
	}
}

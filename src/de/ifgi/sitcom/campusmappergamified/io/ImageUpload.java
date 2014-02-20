package de.ifgi.sitcom.campusmappergamified.io;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.ifgi.sitcom.campusmappergamified.exceptions.UploadException;
import android.util.Log;


/*
 * (floor plan) image upload to server
 */
public class ImageUpload {

	/************* Php script path ****************/
	private static final String UPLOAD_SERVER_URI = "http://giv-lodum.uni-muenster.de/php/uploadtoserver.php";



	public void uploadFile(String sourceUriString, String targetFileName) throws UploadException {

		try {
			Log.v("sourceURI", sourceUriString);
			
			// create file input stream for image file
			FileInputStream fileInputStream = new FileInputStream(sourceUriString);
			
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";
			String Tag = "Image Upload";

			URL connectURL = null;
			try {
				connectURL = new URL(UPLOAD_SERVER_URI);
			} catch (Exception ex) {
				Log.i("URL FORMATION", "MALFORMATED URL");
				throw new UploadException();
			}

			try {
				// ------------------ CLIENT REQUEST

				// Open a HTTP connection to the URL
				HttpURLConnection conn = (HttpURLConnection) connectURL
						.openConnection();

				// Allow Inputs
				conn.setDoInput(true);

				// Allow Outputs
				conn.setDoOutput(true);

				// Don't use a cached copy.
				conn.setUseCaches(false);

				// Use a post method.
				conn.setRequestMethod("POST");

				conn.setRequestProperty("Connection", "Keep-Alive");

				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);

				DataOutputStream dos = new DataOutputStream(
						conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
						+ targetFileName + "\"" + lineEnd);
				dos.writeBytes(lineEnd);

				Log.v(Tag, "Headers are written");

				// create a buffer of maximum size
				int bytesAvailable = fileInputStream.available();
				int maxBufferSize = 1024;
				int bufferSize = Math.min(bytesAvailable, maxBufferSize);
				byte[] buffer = new byte[bufferSize];

				// read file and write it into form...
				int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// close streams
				Log.v(Tag, "File is written");
				fileInputStream.close();
				dos.flush();

				InputStream is = conn.getInputStream();
				// retrieve the response from server
				int ch;

				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				String s = b.toString();
				Log.i("Response", s);
				dos.close();

			} catch (MalformedURLException ex) {
				Log.e(Tag, "error: " + ex.getMessage(), ex);
				throw new UploadException();
			}

			catch (IOException ioe) {
				Log.e(Tag, "error: " + ioe.getMessage(), ioe);
				throw new UploadException();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}

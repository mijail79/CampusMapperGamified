package de.ifgi.sitcom.campusmappergamified.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class InternalMappingTxtLog {

	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	System.out.println("mijail: media is mounted"+state.toString());
	        return true;
	    }
	    return false;
	}
	
	public void appendLog(String text)
	{       
		
	if (isExternalStorageWritable())
	{
		String fullPath = Environment.getExternalStorageDirectory() + "/CampusMapperLog/";
		   File dir = new File(fullPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			File logFile = new File(fullPath,"mappingLog.txt");
			System.out.println("mijail:"+logFile.getAbsolutePath().toString());
		   if (!logFile.exists())
		   {
		      try
		      {
		    	//  System.out.println("mijail: new log created");
		         logFile.createNewFile();
		      } 
		      catch (IOException e)
		      {
		   			Log.e("cannotCreateFileExternalStorage()", e.getMessage());
		      }
		   }
		   try
		   {
		      //BufferedWriter for performance, true to set append to file flag
		      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		      buf.append(text);
		 //     System.out.println("mijail write the text:"+text);
		      buf.newLine();
		 //     buf.write(text);
		      buf.flush();
		      buf.close();
		   }
		   catch (IOException e)
		   {
   			Log.e("saveUserMappingToExternalStorage()", e.getMessage());
		   } 
		
		}
	}
}

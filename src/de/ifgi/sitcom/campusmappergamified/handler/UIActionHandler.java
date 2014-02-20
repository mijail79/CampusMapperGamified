package de.ifgi.sitcom.campusmappergamified.handler;

import android.app.Activity;
import android.graphics.Canvas;
//import android.view.Menu;
import android.view.MotionEvent;

import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.views.ImageViewBase;

/*
 * defined form of classes to control ui behaviour when certain 
 * data type (such as corridor or room) is selected
 */
public abstract class UIActionHandler extends Activity{
	
	
	public abstract void handleMenuAction(MenuItem item);
	
	public abstract int getMenu();
	
//	public abstract boolean onPrepareOptionsMenu(Menu menu);
	
	public abstract void draw(Canvas canvas, float scaleFactor);
	
	public abstract boolean handleTouchEvent(MotionEvent ev, float scaleFactor, int xPos, int yPos);
	
	public abstract void init(ImageViewBase imageViewBase);
	
}

package de.ifgi.sitcom.campusmappergamified.views;


import java.util.ConcurrentModificationException;

import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class ImageViewBase extends SurfaceView implements
		SurfaceHolder.Callback, Runnable {
	private static final String TAG = "Sample::SurfaceView";

	private SurfaceHolder mHolder;
	private int mImageWidth = 0;
	private int mImageHeight = 0;
	private boolean mThreadRun;
	private int frameWidth = 320;
	private int frameHeigth = 240;

	// touch stuff
	private int mImageXPosition = 0;
	private int mImageYPosition = 0;
	private float mScaleFactor = 0.5f; // indicate the scalling. Use this to adjust
								// graphics
	
	UIActionHandler touchHandler;

	

	public ImageViewBase(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);

		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	
	
	public UIActionHandler getUIActionHandler() {
		return touchHandler;
	}

	public void setTouchHandler(UIActionHandler touchHandler) {
		if (touchHandler!= null) touchHandler.init(this);
		this.touchHandler = touchHandler;
	}

	public void initImage(int w, int h) {

		this.mImageWidth = w;
		this.mImageHeight = h;
		
		zoomToImage();
		
		if (touchHandler!= null) touchHandler.init(this);
	}
	
	/*
	 * to be called when image (size) has changed
	 */
	public void zoomToImage() {
		
		// find appropriate scaleFactor, i.e. the whole image is shown on the screen
		mScaleFactor = Math.min((float) frameWidth / mImageWidth, (float) frameHeigth / mImageHeight);
	}
	
	

	public int getImageWidth() {
		return mImageWidth;
	}

	public void setImageWidth(int mImageWidth) {
		this.mImageWidth = mImageWidth;
	}

	public int getImageHeight() {
		return mImageHeight;
	}

	public void setImageHeight(int mImageHeight) {
		this.mImageHeight = mImageHeight;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public void setFrameWidth(int frameWidth) {
		this.frameWidth = frameWidth;
	}

	public int getFrameHeigth() {
		return frameHeigth;
	}

	public void setFrameHeigth(int frameHeigth) {
		this.frameHeigth = frameHeigth;
	}

	public void surfaceChanged(SurfaceHolder _holder, int format, int width,
			int height) {
		Log.i(TAG, "surfaceChanged");
		
			frameWidth = width;
			frameHeigth = height;
			
			if (mImageHeight != 0 && mImageWidth != 0) zoomToImage();
				
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		

		(new Thread(this)).start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");
		mThreadRun = false;
	}

	protected abstract Bitmap processFrame();

	public void run() {

		mThreadRun = true;
		Log.i(TAG, "Starting processing thread");
		while (mThreadRun) {
			Bitmap bmp = null;
			
			bmp = processFrame();

				Canvas canvas = mHolder.lockCanvas();
				if (canvas != null) {
					canvas.drawColor(0, Mode.CLEAR);
					canvas.scale(mScaleFactor, mScaleFactor);
					canvas.translate(mImageXPosition, mImageYPosition);
					
					if (bmp != null && !bmp.isRecycled()) canvas.drawBitmap(bmp, 0, 0, null);
					
					try {
						if(touchHandler != null) touchHandler.draw(canvas, mScaleFactor);						
					} catch (ConcurrentModificationException e) {
						Log.e("debug", "Concurrent Modification while trying to draw");
					}

					
					mHolder.unlockCanvasAndPost(canvas);
			}
		}
	}

	
	public int getImageXPosition() {
		return mImageXPosition;
	}

	public void setImageXPosition(int mImageXPosition) {
		this.mImageXPosition = mImageXPosition;
	}

	public int getImageYPosition() {
		return mImageYPosition;
	}

	public void setImageYPosition(int mImageYPosition) {
		this.mImageYPosition = mImageYPosition;
	}

	public float getScaleFactor() {
		return mScaleFactor;
	}

	public void setScaleFactor(float mScaleFactor) {
		this.mScaleFactor = mScaleFactor;
	}
	


	
	/*
	 * touch stuff
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		
		if(touchHandler != null) touchHandler.handleTouchEvent(ev, mScaleFactor, mImageXPosition, mImageYPosition);

		return true;
	} // ends onTouch

}

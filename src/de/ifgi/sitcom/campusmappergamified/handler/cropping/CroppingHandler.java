package de.ifgi.sitcom.campusmappergamified.handler.cropping;


import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import de.ifgi.sitcom.campusmappergamified.views.ImageViewBase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

/*
 * ui handler for cropping activity
 */
public class CroppingHandler extends UIActionHandler {

	private static final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;

	/*
	 * the mask to indicate the ground plan area
	 * 
	 * A---B 
	 * C---D
	 */
	private int mCircleRadius = 25; // radius of the circles
	private float mCircleAX = 0;
	private float mCircleAY = 0;
	private float mCircleBX = 0;
	private float mCircleBY = 0;
	private float mCircleCX = 0;
	private float mCircleCY = 0;
	private float mCircleDX = 0;
	private float mCircleDY = 0;

	public static final int CORNER_NONE = 0;
	public static final int CORNER_A = 1;
	public static final int CORNER_B = 2;
	public static final int CORNER_C = 3;
	public static final int CORNER_D = 4;
	private int mSelectedCorner = CORNER_NONE;
	
	private static final int MODE_SELECT = 1;
	private static final int MODE_RESULT = 2;
	private static final int MODE_RESET = 3;
	private int mMode = MODE_SELECT;

	@Override
	public void init(ImageViewBase imageViewBase) {

		if (mMode != MODE_SELECT) {
			mMode = MODE_SELECT;
			return;
		}
		
		
		/*
		 * init mask corner points
		 */
		int borderDist = (int) (imageViewBase.getImageWidth() * 0.1);
		mCircleRadius = (int) (25 / imageViewBase.getScaleFactor());
		
		mCircleAX = borderDist;
		mCircleAY = borderDist;
		mCircleBX = imageViewBase.getImageWidth() - borderDist;
		mCircleBY = borderDist;
		mCircleCX = borderDist;
		mCircleCY = imageViewBase.getImageHeight() - borderDist;
		mCircleDX = imageViewBase.getImageWidth() - borderDist;
		mCircleDY = imageViewBase.getImageHeight() - borderDist;
	}

	@Override
	public void draw(Canvas canvas, float scaleFactor) {

		if (mMode == MODE_RESULT) return;
		
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw rectangle
		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(3);
		canvas.drawLine(mCircleAX, mCircleAY, mCircleBX, mCircleBY, paint); // AB
		canvas.drawLine(mCircleBX, mCircleBY, mCircleDX, mCircleDY, paint); // BD
		canvas.drawLine(mCircleDX, mCircleDY, mCircleCX, mCircleCY, paint); // DC
		canvas.drawLine(mCircleCX, mCircleCY, mCircleAX, mCircleAY, paint); // CA

		// draw corners
		paint.setColor(Color.RED);
		canvas.drawCircle(mCircleAX, mCircleAY, mCircleRadius, paint);
		canvas.drawCircle(mCircleBX, mCircleBY, mCircleRadius, paint);
		canvas.drawCircle(mCircleCX, mCircleCY, mCircleRadius, paint);
		canvas.drawCircle(mCircleDX, mCircleDY, mCircleRadius, paint);

		// draw selected corner and lines touched green
		Paint paintSelected = new Paint(Paint.ANTI_ALIAS_FLAG);		
		paintSelected.setColor(Color.GREEN);
		paintSelected.setStrokeWidth(5);
		float touchFactor = 1.5f;
		switch (mSelectedCorner) {
		case CORNER_A:
			canvas.drawCircle(mCircleAX, mCircleAY, mCircleRadius * touchFactor,
					paintSelected);

			canvas.drawLine(mCircleAX, mCircleAY, mCircleBX, mCircleBY, paintSelected); // AB
			canvas.drawLine(mCircleCX, mCircleCY, mCircleAX, mCircleAY, paintSelected); // CA
			
			canvas.drawCircle(mCircleBX, mCircleBY, mCircleRadius, paint);
			canvas.drawCircle(mCircleCX, mCircleCY, mCircleRadius, paint);
			break;
		case CORNER_B:
			canvas.drawCircle(mCircleBX, mCircleBY, mCircleRadius * touchFactor,
					paintSelected);

			canvas.drawLine(mCircleAX, mCircleAY, mCircleBX, mCircleBY, paintSelected); // AB
			canvas.drawLine(mCircleBX, mCircleBY, mCircleDX, mCircleDY, paintSelected); // BD
			
			canvas.drawCircle(mCircleAX, mCircleAY, mCircleRadius, paint);
			canvas.drawCircle(mCircleDX, mCircleDY, mCircleRadius, paint);
			break;
		case CORNER_C:
			canvas.drawCircle(mCircleCX, mCircleCY, mCircleRadius * touchFactor,
					paintSelected);

			canvas.drawLine(mCircleDX, mCircleDY, mCircleCX, mCircleCY, paintSelected); // DC
			canvas.drawLine(mCircleCX, mCircleCY, mCircleAX, mCircleAY, paintSelected); // CA
			
			canvas.drawCircle(mCircleAX, mCircleAY, mCircleRadius, paint);
			canvas.drawCircle(mCircleDX, mCircleDY, mCircleRadius, paint);
			break;
		case CORNER_D:
			canvas.drawCircle(mCircleDX, mCircleDY, mCircleRadius * touchFactor,
					paintSelected);

			canvas.drawLine(mCircleBX, mCircleBY, mCircleDX, mCircleDY, paintSelected); // BD
			canvas.drawLine(mCircleDX, mCircleDY, mCircleCX, mCircleCY, paintSelected); // DC
			
			canvas.drawCircle(mCircleBX, mCircleBY, mCircleRadius, paint);
			canvas.drawCircle(mCircleCX, mCircleCY, mCircleRadius, paint);
			break;
		}
	}

	@Override
	public boolean handleTouchEvent(MotionEvent ev, float scaleFactor, int xPos, int yPos) {
		
		if (mMode == MODE_RESULT) return true;
		
		
		// let our gesture detectors process the events
		// mScaleDetector.onTouchEvent(ev);
		final int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX()/ scaleFactor - xPos;
			final float y = ev.getY()/ scaleFactor - yPos;

			// Remember where we started
			mActivePointerId = ev.getPointerId(0);

			/*
			 * if this is at the position of one of the frame corners, do not
			 * pan: allowPanning = false;
			 * 
			 * or just dont pan and zoom when mask is used
			 */
			if (getDistance(CORNER_A, x, y) <= mCircleRadius * 3)
				mSelectedCorner = CORNER_A;
			else if (getDistance(CORNER_B, x, y) <= mCircleRadius * 3)
				mSelectedCorner = CORNER_B;
			else if (getDistance(CORNER_C, x, y) <= mCircleRadius * 3)
				mSelectedCorner = CORNER_C;
			else if (getDistance(CORNER_D, x, y) <= mCircleRadius * 3)
				mSelectedCorner = CORNER_D;

			break;
		}
		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = ev.findPointerIndex(mActivePointerId);

			if (pointerIndex < 0)
				return false;

			final float x = ev.getX(pointerIndex)/ scaleFactor - xPos;
			final float y = ev.getY(pointerIndex)/ scaleFactor - yPos;

			// move selected mask corner
			switch (mSelectedCorner) {
			case CORNER_A:
				mCircleAX = x;
				mCircleAY = y;
				break;
			case CORNER_B:
				mCircleBX = x;
				mCircleBY = y;
				break;
			case CORNER_C:
				mCircleCX = x;
				mCircleCY = y;
				break;
			case CORNER_D:
				mCircleDX = x;
				mCircleDY = y;
				break;
			}

			// }
			break;
		}
		case MotionEvent.ACTION_UP: {
			mActivePointerId = INVALID_POINTER_ID;

			// deselect mask corner
			mSelectedCorner = CORNER_NONE;
			break;
		}
		case MotionEvent.ACTION_CANCEL: {
			mActivePointerId = INVALID_POINTER_ID;
			break;
		}
		case MotionEvent.ACTION_POINTER_UP: {
			// Extract the index of the pointer that left the touch sensor
			final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = ev.getPointerId(pointerIndex);
			if (pointerId == mActivePointerId) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mActivePointerId = ev.getPointerId(newPointerIndex);
			}
			break;
		}
		} // and switch statement
		return true;
	} // ends onTouch

	private double getDistance(int corner, float x, float y) {
		switch (corner) {
		case CORNER_A:
			return getDistance(mCircleAX, mCircleAY, x, y);
		case CORNER_B:
			return getDistance(mCircleBX, mCircleBY, x, y);
		case CORNER_C:
			return getDistance(mCircleCX, mCircleCY, x, y);
		case CORNER_D:
			return getDistance(mCircleDX, mCircleDY, x, y);
		}

		return 0;
	}

	private double getDistance(float x1, float y1, float x2, float y2) {

		return Math.sqrt((x1 - x2) * (x1 - x2) - (y1 - y2) * (y1 - y2));

	}

	public float getCircleAX() {
		return mCircleAX;
	}

	public void setCircleAX(float circleAX) {
		this.mCircleAX = circleAX;
	}

	public float getCircleAY() {
		return mCircleAY;
	}

	public void setCircleAY(float circleAY) {
		this.mCircleAY = circleAY;
	}

	public float getCircleBX() {
		return mCircleBX;
	}

	public void setCircleBX(float circleBX) {
		this.mCircleBX = circleBX;
	}

	public float getCircleBY() {
		return mCircleBY;
	}

	public void setCircleBY(float circleBY) {
		this.mCircleBY = circleBY;
	}

	public float getCircleCX() {
		return mCircleCX;
	}

	public void setCircleCX(float circleCX) {
		this.mCircleCX = circleCX;
	}

	public float getCircleCY() {
		return mCircleCY;
	}

	public void setCircleCY(float circleCY) {
		this.mCircleCY = circleCY;
	}

	public float getCircleDX() {
		return mCircleDX;
	}

	public void setCircleDX(float circleDX) {
		this.mCircleDX = circleDX;
	}

	public float getCircleDY() {
		return mCircleDY;
	}

	public void setCircleDY(float circleDY) {
		this.mCircleDY = circleDY;
	}


	
	@Override
	public void handleMenuAction(MenuItem item) {

		
		switch (item.getItemId()) {

	    
		case R.id.action_ok_transformation:

			// update behaviour
			mMode = MODE_RESULT;

			return;

			
		case R.id.action_undo:

			// update behaviour
			mMode = MODE_RESET;
			
			return;

	    }

	}

	@Override
	public int getMenu() {

	if (mMode == MODE_RESULT) return R.menu.transformation_result;
	else return R.menu.transformation;
		
	}
	
	public void undo (){
		this.mMode = MODE_RESET;
	}


}

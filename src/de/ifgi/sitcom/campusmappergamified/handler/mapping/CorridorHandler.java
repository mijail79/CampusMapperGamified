package de.ifgi.sitcom.campusmappergamified.handler.mapping;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.activities.MappingActivity;
import de.ifgi.sitcom.campusmappergamified.activities.MyCampusMapperGame;
import de.ifgi.sitcom.campusmappergamified.game.Scoring;
import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import de.ifgi.sitcom.campusmappergamified.indoordata.Corridor;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Line;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;
import de.ifgi.sitcom.campusmappergamified.io.InternalMappingTxtLog;
import de.ifgi.sitcom.campusmappergamified.views.ImageViewBase;

/*
 * ui behavior for corridor mode
 * 
 * started when + button is pressed while corridor tab is selected
 */
public class CorridorHandler extends UIActionHandler {

	private static final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;

	private FloorPlan mFloorPlan;
	private Corridor mCurrentCorridor;
	private InternalMappingTxtLog log = new InternalMappingTxtLog();

	int mStartX = 0;
	int mStartY = 0;
	int mEndX = 0;
	int mEndY = 0;

	private static final int MODE_DEFAULT = 1;
	private static final int MODE_NEW_LINE = 2;
	private static final int MODE_NEW_CORRIDOR = 3;
	private static final int MODE_REMOVE_LINE = 4;
	private static final int MODE_CANCEL = 5;

	private int mMode = MODE_DEFAULT;
	
	private MappingActivity mActivity;
	
	public CorridorHandler (FloorPlan floorPlan, MappingActivity activity){
		super();
		
		this.mActivity = activity;
		this.mFloorPlan = floorPlan;
		mCurrentCorridor = new Corridor();
	}
	
	
	@Override
	public boolean handleTouchEvent(MotionEvent ev, float scaleFactor, int xPos, int yPos) {

		
		if(mMode != MODE_DEFAULT) return true;
		
		// let our gesture detectors process the events
		// mScaleDetector.onTouchEvent(ev);
		final int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();

			// Remember where we started
			mActivePointerId = ev.getPointerId(0);

			/*
			 * create start point and end point
			 */

			mStartX = (int) (x/ scaleFactor - xPos);
			mStartY = (int) (y/ scaleFactor - yPos);
			mEndX = mStartX;
			mEndY = mStartY;

			break;
		}
		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = ev.findPointerIndex(mActivePointerId);

			if (pointerIndex < 0)
				return false;

			final float x = ev.getX(pointerIndex);
			final float y = ev.getY(pointerIndex);


			mEndX = (int) (x/ scaleFactor - xPos);
			mEndY = (int) (y/ scaleFactor - yPos);

			break;
		}
		case MotionEvent.ACTION_UP: {
			mActivePointerId = INVALID_POINTER_ID;

			mMode = MODE_NEW_LINE;

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

		return false;
	}

	public void updateData() {
		switch (mMode) {
		case MODE_NEW_LINE: // trigerred when touch has stopped
			Line updatedLine = mCurrentCorridor.addLine(new Line(new Point(mStartX, mStartY),
					new Point(mEndX, mEndY)));
			
			if (updatedLine != null){
				mStartX = updatedLine.getPointA().getX();
				mStartY = updatedLine.getPointA().getY();
				mEndX = updatedLine.getPointB().getX();
				mEndY = updatedLine.getPointB().getY();
			} else {
				hideCurrentLine();
			}

			mMode = MODE_DEFAULT;
			break;

		case MODE_NEW_CORRIDOR: // trigerred by done action button

			mFloorPlan.getCorridors().add(mCurrentCorridor);
			mCurrentCorridor = new Corridor();
			hideCurrentLine();
			
			mActivity.triggerEditMode();
		
			break;
			
		case MODE_CANCEL:  // trigerred by cancel action

			mCurrentCorridor = new Corridor();
			hideCurrentLine();
			
			mActivity.triggerEditMode();
			break;
			
		case MODE_REMOVE_LINE: // trigerred by undo action

			mCurrentCorridor.removeNewestLine();
			hideCurrentLine();
			break;
			
		case MODE_DEFAULT: // default
			return;
		}

		mMode = MODE_DEFAULT;
	}
	
	private void hideCurrentLine(){
		mStartX = 0;
		mStartY = 0;
		mEndX = 0;
		mEndY = 0;		
	}

	@Override
	public void draw(Canvas canvas, float scaleFactor) {

		updateData();

		// draw all the elements encapsulated by the floorplan object
		mFloorPlan.draw(canvas);

		// draw the current corridor, if there is one
		mCurrentCorridor.draw(canvas, true);

		// draw current segment
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.YELLOW);
		paint.setStrokeWidth(3);
		canvas.drawLine(mStartX, mStartY, mEndX, mEndY, paint);
	}

	@Override
	public void init(ImageViewBase imageViewBase) {

		Log.v("debug", "init corridor touch handler");

		mCurrentCorridor = new Corridor();
		mMode = MODE_DEFAULT;
		hideCurrentLine();
	}

	@Override
	public void handleMenuAction(MenuItem item) {

		
		switch (item.getItemId()) {

		case R.id.action_undo:
			mMode = MODE_REMOVE_LINE;
			return;
			
		case R.id.action_done:
			mMode = MODE_NEW_CORRIDOR;
		//	increases the score for every time the user draws corridors
			MyCampusMapperGame.getInstance().setMyScore(Scoring.CORRIDOR.getValue() + MyCampusMapperGame.getInstance().getMyScore());
			MyCampusMapperGame.getInstance().setMybuildingScore(Scoring.CORRIDOR.getValue() + MyCampusMapperGame.getInstance().getMybuildingScore());
			 log.appendLog("Corridor;"+Scoring.CORRIDOR.getValue());
			return;
			
		case R.id.action_cancel:
			mMode = MODE_CANCEL;
			return;
		}
		
		
		
		
	}

	@Override
	public int getMenu() {
		return R.menu.mapping_corridor;
	}
}

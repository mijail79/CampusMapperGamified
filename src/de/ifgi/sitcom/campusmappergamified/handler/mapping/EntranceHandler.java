package de.ifgi.sitcom.campusmappergamified.handler.mapping;

import com.actionbarsherlock.view.MenuItem;

import android.graphics.Canvas;
import android.view.MotionEvent;
import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.activities.MappingActivity;
import de.ifgi.sitcom.campusmappergamified.activities.MyCampusMapperGame;
import de.ifgi.sitcom.campusmappergamified.dialogs.EntranceDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.EntranceDialog.EntranceDialogListener;
import de.ifgi.sitcom.campusmappergamified.game.Scoring;
import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;
import de.ifgi.sitcom.campusmappergamified.io.InternalMappingTxtLog;
import de.ifgi.sitcom.campusmappergamified.views.ImageViewBase;

/*
 * ui behavior for entrance mode
 * 
 * started when + button is pressed while entrance tab is selected
 */
public class EntranceHandler extends UIActionHandler implements EntranceDialogListener{

	private static final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;
	
	private FloorPlan mFloorPlan;
	private InternalMappingTxtLog log = new InternalMappingTxtLog();
	
	int mCurrentX = 0;
	int mCurrentY = 0;
	
	private static final int MODE_DEFAULT = 1;
	private static final int MODE_NEW_ENTRANCE = 2;
	private static final int MODE_NEW_ENTRANCE_DIALOG = 3;

	private int mMode = MODE_DEFAULT;
	
	private MappingActivity mActivity;
	private EntranceDialog mEntranceDialogFragment;
	
	public EntranceHandler (FloorPlan floorPlan, MappingActivity activity){
		super();
		
		this.mActivity = activity;
		this.mFloorPlan = floorPlan;
	}
	
	
	@Override
	public boolean handleTouchEvent(MotionEvent ev, float scaleFactor,
			int xPos, int yPos) {

		// let our gesture detectors process the events
		// mScaleDetector.onTouchEvent(ev);
		final int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();

			// Remember where we started
			mActivePointerId = ev.getPointerId(0);

			mCurrentX = (int) (x / scaleFactor - xPos);
			mCurrentY = (int) (y / scaleFactor - yPos);

			mMode = MODE_NEW_ENTRANCE_DIALOG;
			break;
		}
		case MotionEvent.ACTION_UP: {
			mActivePointerId = INVALID_POINTER_ID;
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

	@Override
	public void draw(Canvas canvas, float scaleFactor) {
		updateData();

		// draw all the elements encapsulated by the floorplan object
		mFloorPlan.draw(canvas);
	}

	private void updateData() {
		switch (mMode) {
		case MODE_NEW_ENTRANCE_DIALOG: // trigerred when touch has started

			mEntranceDialogFragment = new EntranceDialog();
			mEntranceDialogFragment.attachHandler(this);
			mEntranceDialogFragment.init(mFloorPlan);
			mEntranceDialogFragment.show(mActivity.getSupportFragmentManager(), "");
			mMode = MODE_DEFAULT;

			break;
		
		case MODE_NEW_ENTRANCE: // trigerred when touch has started

			mFloorPlan.addEntrance(new Point(mCurrentX, mCurrentY), mEntranceDialogFragment.getType(), true);
			mEntranceDialogFragment.updateEntrance(mFloorPlan.getEntrances().get(mFloorPlan.getEntrances().size() - 1));
			mMode = MODE_DEFAULT;
			
			mActivity.triggerEditMode();
			MyCampusMapperGame.getInstance().setMyScore(Scoring.ENTRANCE.getValue() + MyCampusMapperGame.getInstance().getMyScore());
			MyCampusMapperGame.getInstance().setMybuildingScore(Scoring.ENTRANCE.getValue() + MyCampusMapperGame.getInstance().getMybuildingScore());
			
			//writes the score to the log
			log.appendLog("Entrance;"+Scoring.ENTRANCE.getValue());

			
			break;
			
		case MODE_DEFAULT: // default
			return;
		}

		mMode = MODE_DEFAULT;
	}
	



	@Override
	public void init(ImageViewBase imageViewBase) {		
	}

	@Override
	public void handleMenuAction(MenuItem item) {
		switch (item.getItemId()) {
		}
	}


	@Override
	public int getMenu() {
		return R.menu.mapping_entrance;
	}


	@Override
	public void onEntranceDialogPositiveClick(EntranceDialog dialog) {
		mMode = MODE_NEW_ENTRANCE;
	}


	@Override
	public void onEntranceDialogNegativeClick(EntranceDialog dialog) {
	}

}

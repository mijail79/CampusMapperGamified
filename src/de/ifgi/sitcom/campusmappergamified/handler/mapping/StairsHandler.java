package de.ifgi.sitcom.campusmappergamified.handler.mapping;

import com.actionbarsherlock.view.MenuItem;

import android.graphics.Canvas;
import android.view.MotionEvent;
import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.activities.MappingActivity;
import de.ifgi.sitcom.campusmappergamified.activities.MyCampusMapperGame;
import de.ifgi.sitcom.campusmappergamified.dialogs.StairsDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.StairsDialog.StairsDialogListener;
import de.ifgi.sitcom.campusmappergamified.game.Scoring;
import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import de.ifgi.sitcom.campusmappergamified.indoordata.Connection;
import de.ifgi.sitcom.campusmappergamified.indoordata.Elevator;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.indoordata.Stairs;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;
import de.ifgi.sitcom.campusmappergamified.io.InternalMappingTxtLog;
import de.ifgi.sitcom.campusmappergamified.views.ImageViewBase;

/*
 * ui behavior for stairs mode
 * 
 * started when + button is pressed while stairs tab is selected
 */
public class StairsHandler extends UIActionHandler implements StairsDialogListener{

	private static final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;
	
	private FloorPlan floorPlan;
	private InternalMappingTxtLog log = new InternalMappingTxtLog();
	
	int currentX = 0;
	int currentY = 0;
	
	private static final int MODE_DEFAULT = 1;
	private static final int MODE_NEW_STAIRS = 2;
	private static final int MODE_NEW_STAIRS_DIALOG = 3;
	private int mode = MODE_DEFAULT;
	
	private MappingActivity activity;
	private StairsDialog stairsDialogFragment;
	
	public StairsHandler (FloorPlan floorPlan, MappingActivity activity){
		super();
		
		this.activity = activity;
		this.floorPlan = floorPlan;
	}
	
	
	@Override
	public boolean handleTouchEvent(MotionEvent ev, float scaleFactor, int xPos, int yPos) {

		// let our gesture detectors process the events
		// mScaleDetector.onTouchEvent(ev);
		final int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();

			// Remember where we started
			mActivePointerId = ev.getPointerId(0);

			currentX = (int) (x/ scaleFactor - xPos);
			currentY = (int) (y/ scaleFactor - yPos);
			
			mode = MODE_NEW_STAIRS_DIALOG;
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
		floorPlan.draw(canvas);		
	}

	private void updateData() {

		switch (mode) {
		case MODE_NEW_STAIRS_DIALOG: // trigerred when touch has started

			stairsDialogFragment = new StairsDialog();
			stairsDialogFragment.attachHandler(this);
			stairsDialogFragment.setBuildingURI(floorPlan.getBuildingURI());
			stairsDialogFragment.setBasicFloorNumber(floorPlan.getFloorNumber());
			stairsDialogFragment.show(activity.getSupportFragmentManager(), "");
			
			mode = MODE_DEFAULT;
			
			break;
			
		case MODE_NEW_STAIRS: // trigerred when stairs dialog has positive callback

			// add new stairs to floorplan
			floorPlan.addStairs(new Point(currentX, currentY), stairsDialogFragment.getType(), true);

			// if extra information such as destination floor or position was entered, add it to the stairs object
			if (stairsDialogFragment.getType() == Connection.CONNECTION_TYPE_STAIRS) {
				// get the newest stairs object (the one we just created)
				Stairs stairs = floorPlan.getStairs().get(floorPlan.getStairs().size() - 1);
				stairsDialogFragment.updateVerticalConnection(stairs);


			} else { // type is elevator
				// get the newest stairs object (the one we just created)
				Elevator elevator = floorPlan.getElevators().get(
						floorPlan.getElevators().size() - 1);
				stairsDialogFragment.updateVerticalConnection(elevator);

			}
			
			mode = MODE_DEFAULT;
			activity.triggerEditMode();
			MyCampusMapperGame.getInstance().setMyScore(Scoring.STAIR.getValue() + MyCampusMapperGame.getInstance().getMyScore());
			MyCampusMapperGame.getInstance().setMybuildingScore(Scoring.STAIR.getValue() + MyCampusMapperGame.getInstance().getMybuildingScore());
			
			//writes the score to the log
			log.appendLog("Stairs;"+Scoring.STAIR.getValue());
			
			break;
			
		case MODE_DEFAULT: // default
			return;
		}

		mode = MODE_DEFAULT;
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
		return R.menu.mapping_stairs;
	}


	@Override
	public void onStairsDialogPositiveClick(StairsDialog dialog) {

		mode = MODE_NEW_STAIRS;
	}


	@Override
	public void onStairsDialogNegativeClick(StairsDialog dialog) {
		
	}

}

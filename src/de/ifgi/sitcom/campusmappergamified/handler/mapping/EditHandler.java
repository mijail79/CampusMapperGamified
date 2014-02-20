package de.ifgi.sitcom.campusmappergamified.handler.mapping;

import android.graphics.Canvas;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.actionbarsherlock.view.MenuItem;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.activities.MappingActivity;
import de.ifgi.sitcom.campusmappergamified.dialogs.EntranceDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.RoomDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.StairsDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.EntranceDialog.EntranceDialogListener;
import de.ifgi.sitcom.campusmappergamified.dialogs.RoomDialog.RoomDialogListener;
import de.ifgi.sitcom.campusmappergamified.dialogs.StairsDialog.StairsDialogListener;
import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import de.ifgi.sitcom.campusmappergamified.indoordata.Corridor;
import de.ifgi.sitcom.campusmappergamified.indoordata.Door;
import de.ifgi.sitcom.campusmappergamified.indoordata.Elevator;
import de.ifgi.sitcom.campusmappergamified.indoordata.Entrance;
import de.ifgi.sitcom.campusmappergamified.indoordata.EntranceIndoor;
import de.ifgi.sitcom.campusmappergamified.indoordata.EntranceOutdoor;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.indoordata.MapElement;
import de.ifgi.sitcom.campusmappergamified.indoordata.Room;
import de.ifgi.sitcom.campusmappergamified.indoordata.Stairs;
import de.ifgi.sitcom.campusmappergamified.indoordata.VerticalConnection;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;
import de.ifgi.sitcom.campusmappergamified.views.ImageViewBase;

/*
 * ui behavior for edit mode (default mode of mapping activity)
 * 
 * this mode is left when + button is pressed
 */
public class EditHandler extends UIActionHandler implements RoomDialogListener, StairsDialogListener, EntranceDialogListener {
	
	
	private FloorPlan mFloorPlan;
	private MappingActivity mActivity;
	private ImageViewBase mImageViewBase;
	private MapElement mSelectedObject;
	private MapElement mObjectToSelect;
	
	private static final int MODE_DEFAULT = 1;
	private static final int MODE_NEW_SELECTION = 2;
	private int mMode = MODE_DEFAULT;
	
	
	// touch stuff
	private float mLastTouchX;
	private float mLastTouchY;
	private static final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;
	private ScaleGestureDetector mScaleDetector;
	private GestureDetector mGestureDetector;

	public EditHandler (FloorPlan floorPlan, MappingActivity activity){
		super();
		
		this.mFloorPlan = floorPlan;
		this.mActivity = activity;

		// touch stuff
		mScaleDetector = new ScaleGestureDetector(activity, new ScaleListener());
		mGestureDetector = new GestureDetector(activity, new SingleTapListener());
	}

	@Override
	public boolean handleTouchEvent(MotionEvent ev, float scaleFactor, int xPos, int yPos) {
		
		mScaleDetector.onTouchEvent(ev);
		mGestureDetector.onTouchEvent(ev);
		
		final int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();

			// Remember where we started
			mLastTouchX = x;
			mLastTouchY = y;
			mActivePointerId = ev.getPointerId(0);			
			
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = ev.findPointerIndex(mActivePointerId);
			
			if (pointerIndex < 0) return false;
			
			final float x = ev.getX(pointerIndex);
			final float y = ev.getY(pointerIndex);

			if (!mScaleDetector.isInProgress()) {
				// Calculate the distance moved
				final float dx = x - mLastTouchX;
				final float dy = y - mLastTouchY;

				// Move the object
				pan(dx, dy, scaleFactor, xPos, yPos);
				
				// Remember this touch position for the next move event
				mLastTouchX = x;
				mLastTouchY = y;

			}
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
				mLastTouchX = ev.getX(newPointerIndex);
				mLastTouchY = ev.getY(newPointerIndex);
				mActivePointerId = ev.getPointerId(newPointerIndex);
			}
			break;
		}
		} // and switch statement
		return true;
	}
	
	
	private void pan(float dx, float dy, float scaleFactor, int xPos, int yPos){
		
		
		// compute new position
		xPos += dx / scaleFactor;
		yPos += dy / scaleFactor;
		
		// check if we meet border
		xPos = checkBordersX(scaleFactor, xPos);
		yPos = checkBordersY(scaleFactor, yPos);
		
		// update position
		mImageViewBase.setImageXPosition(xPos);
		mImageViewBase.setImageYPosition(yPos);
			
	}
	
	private int checkBordersX(float scaleFactor, int xPos){

		if ((mImageViewBase.getImageWidth() + xPos) * scaleFactor < mImageViewBase.getFrameWidth()) {
			xPos = (int) (mImageViewBase.getFrameWidth()/ scaleFactor - mImageViewBase.getImageWidth());
		}

		if (xPos > 0) {
			xPos = 0;
		}
		
		return xPos;
	}
	
	private int checkBordersY(float scaleFactor, int yPos){

		if ((mImageViewBase.getImageHeight() + yPos) * scaleFactor < mImageViewBase.getFrameHeigth()) {
			yPos = (int) (mImageViewBase.getFrameHeigth()/ scaleFactor - mImageViewBase.getImageHeight());
		}
		
		if (yPos > 0) {
			yPos = 0;
		}
		
		
		return yPos;
	}
	
	
	

	@Override
	public void handleMenuAction(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_delete:

			mFloorPlan.delete(mSelectedObject);
			mActivity.triggerEditMode();

			return;
			
		case R.id.action_cancel:

			mActivity.triggerEditMode();

			return;
			
		case R.id.action_edit_room:
			RoomDialog roomDialogFragment = new RoomDialog();
			roomDialogFragment.attachHandler(this);
			roomDialogFragment.showCreateDoorCheckBox(false);
			roomDialogFragment.setRoom((Room) mSelectedObject);
			roomDialogFragment.setPersonNames(mFloorPlan.getPersonNames());
			roomDialogFragment.show(mActivity.getSupportFragmentManager(), "Edit Room");			
			return;
			
		case R.id.action_edit_stairs:
			StairsDialog stairsDialogFragment = new StairsDialog();
			stairsDialogFragment.attachHandler(this);
			stairsDialogFragment.setBuildingURI(mFloorPlan.getBuildingURI());
			stairsDialogFragment.setBasicFloorNumber(mFloorPlan.getFloorNumber());
			stairsDialogFragment.setVerticalConnection((VerticalConnection) mSelectedObject);
			stairsDialogFragment.show(mActivity.getSupportFragmentManager(), "Edit Stairs");
			return;
			
		case R.id.action_edit_entrance:
			EntranceDialog entranceDialogFragment = new EntranceDialog();
			entranceDialogFragment.attachHandler(this);
			entranceDialogFragment.setBasicFloorPlan(mFloorPlan);
			entranceDialogFragment.setEntrance((Entrance) mSelectedObject);		
			entranceDialogFragment.show(mActivity.getSupportFragmentManager(), "Edit Entrance");
			return;
		}
		
	}
	
	
	private void updateData() {

		switch (mMode) {
		case MODE_NEW_SELECTION:
			mSelectedObject = mObjectToSelect;
			break;

			
		case MODE_DEFAULT: // default
			return;
		}

		mMode = MODE_DEFAULT;
	}

	@Override
	public void draw(Canvas canvas, float scaleFactor) {

		updateData();
		
		// draw all the elements encapsulated by the floorplan object
		mFloorPlan.draw(canvas);
		
		// draw selected object, if we have one
		if(mSelectedObject != null){
			mSelectedObject.draw(canvas, true);
		}
	}

	@Override
	public void init(ImageViewBase imageViewBase) {
		this.mImageViewBase = imageViewBase;
		mSelectedObject = null;
	}

	@Override
	public int getMenu() {
		return R.menu.mapping;
	}

	
	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {

			float mScaleFactor = mImageViewBase.getScaleFactor();
			float oldScaleFactor = mScaleFactor;
			mScaleFactor *= detector.getScaleFactor();
			
			// Don't let the object get too small or too large.
			float minValue = Math.min(
					(((float) mImageViewBase.getFrameWidth()) / mImageViewBase
							.getImageWidth()), (((float) mImageViewBase
							.getFrameHeigth()) / mImageViewBase
							.getImageHeight()));
			mScaleFactor = Math.max(minValue, Math.min(mScaleFactor, 10.0f));
			mImageViewBase.setScaleFactor(mScaleFactor);
			
			// let the old center remain on the center
			int xPos = mImageViewBase.getImageXPosition();
			int yPos = mImageViewBase.getImageYPosition();

			float focusX = detector.getFocusX()/ oldScaleFactor - xPos;
			float focusY = detector.getFocusY()/ oldScaleFactor - yPos;
			
			xPos = (int)((oldScaleFactor/mScaleFactor) * (focusX + xPos) - focusX);
			yPos = (int)((oldScaleFactor/mScaleFactor) * (focusY + yPos) - focusY);
			
			// check if we meet border
			xPos = checkBordersX(mScaleFactor, xPos);
			yPos = checkBordersY(mScaleFactor, yPos);
			
			mImageViewBase.setImageXPosition(xPos);
			mImageViewBase.setImageYPosition(yPos);
			
			return true;
		}
	}
	
	private class SingleTapListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {

			Log.v("debug", "select item");
			
			int positionX = (int) (e.getX() / mImageViewBase.getScaleFactor() - mImageViewBase.getImageXPosition());
			int positionY = (int) (e.getY()/ mImageViewBase.getScaleFactor() - mImageViewBase.getImageYPosition());
			
			Point position = new Point(positionX, positionY);
			mObjectToSelect = mFloorPlan.selectObject(60, position);
			
			if (mObjectToSelect != null){
				
				if (mObjectToSelect.getClass() == Corridor.class){
					mActivity.sendHandlerMessage(MappingActivity.MESSAGE_SELECT_CORRIDOR);
					
				} else if (mObjectToSelect.getClass() == Door.class){
					mActivity.sendHandlerMessage(MappingActivity.MESSAGE_SELECT_DOOR);
					
				} else if (mObjectToSelect.getClass() == Room.class){
					mActivity.sendHandlerMessage(MappingActivity.MESSAGE_SELECT_ROOM);
					
				} else if (mObjectToSelect.getClass() == EntranceOutdoor.class || mObjectToSelect.getClass() == EntranceIndoor.class){
					mActivity.sendHandlerMessage(MappingActivity.MESSAGE_SELECT_ENTRANCE);
					
				} else if (mObjectToSelect.getClass() == Stairs.class || mObjectToSelect.getClass() == Elevator.class){
					mActivity.sendHandlerMessage(MappingActivity.MESSAGE_SELECT_STAIRS);
				}
			} else mActivity.triggerEditMode();
			
			mMode = MODE_NEW_SELECTION;
			
			
			return true;

		}
	}

	@Override
	public void onRoomDialogPositiveClick(RoomDialog dialog) {
		dialog.updateRoom(dialog.getRoom(), mFloorPlan.getBuilding().getPersons());
	}

	@Override
	public void onRoomDialogNegativeClick(RoomDialog dialog) {
		
	}

	@Override
	public void onStairsDialogPositiveClick(StairsDialog dialog) {
		dialog.updateVerticalConnection(dialog.getVerticalConnection());
		if(mSelectedObject == null){
			mObjectToSelect = dialog.getVerticalConnection();
			mMode = MODE_NEW_SELECTION;
		}
	}

	@Override
	public void onStairsDialogNegativeClick(StairsDialog dialog) {
		
	}

	@Override
	public void onEntranceDialogPositiveClick(EntranceDialog dialog) {
		dialog.updateEntrance(dialog.getEntrance());
		if(mSelectedObject == null){
			mObjectToSelect = dialog.getEntrance();
			mMode = MODE_NEW_SELECTION;
		}
	}

	@Override
	public void onEntranceDialogNegativeClick(EntranceDialog dialog) {
		
	}

}

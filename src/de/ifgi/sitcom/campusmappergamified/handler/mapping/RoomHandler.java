package de.ifgi.sitcom.campusmappergamified.handler.mapping;


import com.actionbarsherlock.view.MenuItem;

import android.graphics.Canvas;
import android.view.MotionEvent;
import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.activities.MappingActivity;
import de.ifgi.sitcom.campusmappergamified.activities.MyCampusMapperGame;
import de.ifgi.sitcom.campusmappergamified.dialogs.RoomDialog;
import de.ifgi.sitcom.campusmappergamified.dialogs.RoomDialog.RoomDialogListener;
import de.ifgi.sitcom.campusmappergamified.game.Scoring;
import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import de.ifgi.sitcom.campusmappergamified.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmappergamified.indoordata.Room;
import de.ifgi.sitcom.campusmappergamified.indoordata.geometry.Point;
import de.ifgi.sitcom.campusmappergamified.io.InternalMappingTxtLog;
import de.ifgi.sitcom.campusmappergamified.views.ImageViewBase;


/*
 * ui behavior for room mode
 * 
 * started when + button is pressed while room tab is selected
 */
public class RoomHandler extends UIActionHandler implements RoomDialogListener {

	private static final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;
	
	private FloorPlan mFloorPlan;
	private InternalMappingTxtLog log = new InternalMappingTxtLog();
	
	int mCurrentX = 0;
	int mCurrentY = 0;
	
	private static final int MODE_DEFAULT = 1;
	private static final int MODE_NEW_ROOM_DIALOG = 2;
	private static final int MODE_NEW_ROOM = 3;
	private static final int MODE_NEW_ROOM_AND_DOOR = 5;
	
	private MappingActivity mActivity;

	private int mMode = MODE_DEFAULT;
	
	private RoomDialog mRoomDialogFragment;
	
	
	public RoomHandler (FloorPlan floorPlan, MappingActivity activity){
		super();
		
		this.mFloorPlan = floorPlan;
		this.mActivity = activity;
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

			mCurrentX = (int) (x/ scaleFactor - xPos);
			mCurrentY = (int) (y/ scaleFactor - yPos);
			
			mMode = MODE_NEW_ROOM_DIALOG;
			


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
		case MODE_NEW_ROOM_DIALOG: // trigerred when touch has started
			mRoomDialogFragment = new RoomDialog();
			mRoomDialogFragment.attachHandler(this);
			mRoomDialogFragment.setPersonNames(mFloorPlan.getPersonNames());
			mRoomDialogFragment.show(mActivity.getSupportFragmentManager(), "room_information");
			
			mMode = MODE_DEFAULT;
			
			break;
			
		case MODE_NEW_ROOM: // trigerred when new room dialog has positive callback
			Room newRoom = new Room(new Point(mCurrentX, mCurrentY), null, null);
			mRoomDialogFragment.updateRoom(newRoom, mFloorPlan.getBuilding().getPersons());
			mFloorPlan.getRooms().add(newRoom);
			mMode = MODE_DEFAULT;
			
			mActivity.triggerEditMode();
			MyCampusMapperGame.getInstance().setMyScore(Scoring.ROOM.getValue() + MyCampusMapperGame.getInstance().getMyScore());
			MyCampusMapperGame.getInstance().setMybuildingScore(Scoring.ROOM.getValue() + MyCampusMapperGame.getInstance().getMybuildingScore());

			//writes the score to the log
			log.appendLog("Room;"+Scoring.ROOM.getValue());
			
			break;
			
		case MODE_NEW_ROOM_AND_DOOR: // trigerred when new room dialog has positive callback

			newRoom = mFloorPlan.addRoom(new Point(mCurrentX, mCurrentY));
			mRoomDialogFragment.updateRoom(newRoom, mFloorPlan.getBuilding().getPersons());
			MyCampusMapperGame.getInstance().setMyScore(Scoring.ROOM.getValue() + MyCampusMapperGame.getInstance().getMyScore());
			MyCampusMapperGame.getInstance().setMybuildingScore(Scoring.ROOM.getValue() + MyCampusMapperGame.getInstance().getMybuildingScore());
			
			//writes the score to the log
			log.appendLog("Room;"+Scoring.ROOM.getValue());
			
			mMode = MODE_DEFAULT;
			mActivity.triggerEditMode();
			
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
	public void onRoomDialogPositiveClick(RoomDialog dialog) {

		if(dialog.createDoor()) mMode = MODE_NEW_ROOM_AND_DOOR;
		else mMode = MODE_NEW_ROOM;
	}


	@Override
	public void onRoomDialogNegativeClick(RoomDialog dialog) {
	}


	
	@Override
	public int getMenu() {
		return R.menu.mapping_room;
	}



}

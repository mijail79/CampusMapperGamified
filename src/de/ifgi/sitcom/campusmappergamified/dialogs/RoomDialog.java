package de.ifgi.sitcom.campusmappergamified.dialogs;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.ifgi.sitcom.campusmappergamified.R;
import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;
import de.ifgi.sitcom.campusmappergamified.indoordata.Person;
import de.ifgi.sitcom.campusmappergamified.indoordata.Room;

/*
 *  dialog  shown when room was created/ edited
 */
public class RoomDialog extends SherlockDialogFragment {

	private LinearLayout mLl;
	private int mPersonIndex = 0;
	private CheckBox mCreateDoorCheckBox;
	private Room mRoom;
	private String[] mPersonNames;

	private EditText mEditRoomName;
	private ArrayList<EditText> mPersonEdits;
	private Button mAddPersonButton;

	private boolean mShowCreateDoorCheckBox = true;

	// Use this instance of the interface to deliver action events
	private RoomDialogListener mListener;

	public interface RoomDialogListener {
		public void onRoomDialogPositiveClick(RoomDialog dialog);

		public void onRoomDialogNegativeClick(RoomDialog dialog);
	}

	public void showCreateDoorCheckBox(boolean show) {
		mShowCreateDoorCheckBox = show;

		if (mCreateDoorCheckBox != null) {
			if (!mShowCreateDoorCheckBox)
				mCreateDoorCheckBox.setVisibility(View.GONE);
			else
				mCreateDoorCheckBox.setVisibility(View.VISIBLE);
		}
	}

	public void attachHandler(UIActionHandler mappingTouchHandler) {

		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (RoomDialogListener) mappingTouchHandler;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(mappingTouchHandler.toString()
					+ " must implement NoticeDialogListener");
		}

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				getSherlockActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View dialogView = inflater.inflate(R.layout.dialog_room, null);
		builder.setView(dialogView)
				// Add title
				.setTitle(R.string.title_room_information)
				// Add action buttons
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								if (mListener != null)
									mListener
											.onRoomDialogPositiveClick(RoomDialog.this);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (mListener != null)
									mListener
											.onRoomDialogNegativeClick(RoomDialog.this);
							}
						});

		// acces view view elements
		mLl = (LinearLayout) dialogView
				.findViewById(R.id.linearLayout_room_dialog);
		mCreateDoorCheckBox = (CheckBox) dialogView
				.findViewById(R.id.checkBox_create_door);
		mEditRoomName = (EditText) dialogView.findViewById(R.id.room_name);
		mAddPersonButton = (Button) dialogView
				.findViewById(R.id.button_add_person);

		mAddPersonButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addPerson("");

			}
		});

		if (mRoom != null) {
			mEditRoomName.setText(mRoom.getName());

			// create Person fields and give them their data
			for (Person p : mRoom.getPersons()) {
				addPerson(p.getName());
			}

		}

		if (!mShowCreateDoorCheckBox)
			mCreateDoorCheckBox.setVisibility(View.GONE);

		return builder.create();
	}

	public void updateRoom(Room r, ArrayList<Person> persons) {

		r.setName(mEditRoomName.getText().toString());

		if (mPersonEdits != null) {
			r.setPersons(new ArrayList<Person>());
			for (EditText et : mPersonEdits) {
				
				String personName = et.getText().toString();
				String uri = null;
				
				/*
				 * check if person already exists in lodum
				 * if so, use its uri
				 */
				if (mPersonNames != null){
					for (int i = 0; i < mPersonNames.length; i++){
						if (personName.equals(mPersonNames[i])){
//							r.getPersons().add(new Person(personName, persons.get(i).getUri()));
							uri = persons.get(i).getUri();
							break;
						}
					}					
				}
				r.getPersons().add(new Person(personName, uri));
			}
		}

	}

	public void addPerson(String name) {

		mPersonIndex++;
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(4, 4, 4, 4);

		// add AutoCompleteTextView for the person´s name
		AutoCompleteTextView editText = new AutoCompleteTextView(
				getSherlockActivity());
		editText.setText(name);
		editText.setLayoutParams(llp);

		// if we know the person names from LODUM, add auto complete stuff
		if (mPersonNames != null && mPersonNames.length > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getSherlockActivity(),
					android.R.layout.simple_dropdown_item_1line, mPersonNames);
			editText.setAdapter(adapter);
			editText.setThreshold(1);
			editText.setSingleLine();

			editText.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((AutoCompleteTextView) v).showDropDown();
				}
			});

			// add listeners to handle done, enter, next, etc.
			editText.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {

					if (event != null) {
						if (event.getAction() == KeyEvent.KEYCODE_ENTER
								|| event.getAction() == KeyEvent.ACTION_DOWN) {
							mAddPersonButton.requestFocus();
							return true;
						}
					}
					return false;
				}
			});
		}

		editText.requestFocus();
		mLl.addView(editText);
		editText.showDropDown();
		// show keyboard
		InputMethodManager imm = (InputMethodManager) getSherlockActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(editText, 0);
		}

		if (mPersonEdits == null)
			mPersonEdits = new ArrayList<EditText>();
		mPersonEdits.add(editText);
	}

	public boolean createDoor() {
		return mCreateDoorCheckBox.isChecked();
	}

	/*
	 * getters and setters
	 */

	public void setRoom(Room room) {
		this.mRoom = room;
	}

	public void setPersonNames(String[] personNames) {
		this.mPersonNames = personNames;
	}

	public Room getRoom() {
		return this.mRoom;
	}

}

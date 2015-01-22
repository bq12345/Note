package com.bq.note;

import java.util.HashMap;
import java.util.Map;

import org.bq.app.NoteApplication;
import org.bq.db.Note;
import org.bq.db.NoteDao;
import org.bq.tool.ResourceParser;
import org.bq.tool.ResourceParser.NoteBgResources;
import org.bq.tool.ResourceParser.NoteItemBgResources;

import com.bq.widget.NoteWidgetProvider_4x;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class NoteEditActivity extends Activity implements OnClickListener {
	private class HeadViewHolder {
		public TextView tvModified;

		public ImageView ibSetBgColor;
	}

	private static final Map<Integer, Integer> sBgSelectorBtnsMap = new HashMap<Integer, Integer>();
	static {
		sBgSelectorBtnsMap.put(R.id.iv_bg_yellow, ResourceParser.YELLOW);
		sBgSelectorBtnsMap.put(R.id.iv_bg_red, ResourceParser.RED);
		sBgSelectorBtnsMap.put(R.id.iv_bg_blue, ResourceParser.BLUE);
		sBgSelectorBtnsMap.put(R.id.iv_bg_green, ResourceParser.GREEN);
		sBgSelectorBtnsMap.put(R.id.iv_bg_white, ResourceParser.WHITE);
	}

	private static final Map<Integer, Integer> sBgSelectorSelectionMap = new HashMap<Integer, Integer>();
	static {
		sBgSelectorSelectionMap.put(ResourceParser.YELLOW,
				R.id.iv_bg_yellow_select);
		sBgSelectorSelectionMap.put(ResourceParser.RED, R.id.iv_bg_red_select);
		sBgSelectorSelectionMap
				.put(ResourceParser.BLUE, R.id.iv_bg_blue_select);
		sBgSelectorSelectionMap.put(ResourceParser.GREEN,
				R.id.iv_bg_green_select);
		sBgSelectorSelectionMap.put(ResourceParser.WHITE,
				R.id.iv_bg_white_select);
	}
	private static final String TAG = "NoteEditActivity";

	private HeadViewHolder mNoteHeaderHolder;

	private View mHeadViewPanel;

	private View mNoteBgColorSelector;

	private EditText mNoteEditor;

	private View mNoteEditorPanel;

	private NoteDao noteDao;

	private Note workingNote;

	private int bgId = 0;

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.note_edit);
		noteDao = NoteApplication.daoSession.getNoteDao();
		mAppWidgetId = getIntent().getIntExtra("org.bq.note.widget_id",
				AppWidgetManager.INVALID_APPWIDGET_ID);
		bgId = getIntent().getIntExtra("org.bq.note.bg_id", 0);
		if (savedInstanceState == null && !initActivityState(getIntent())) {
			finish();
			return;
		}
		Log.i("AppWidgetId&bgId", mAppWidgetId + "%%%%" + bgId);
		initResources();
	}

	/**
	 * Current activity may be killed when the memory is low. Once it is killed,
	 * for another time user load this activity, we should restore the former
	 * state
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(Intent.EXTRA_UID)) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.putExtra(Intent.EXTRA_UID,
					savedInstanceState.getLong(Intent.EXTRA_UID));
			if (!initActivityState(intent)) {
				finish();
				return;
			}
			Log.d(TAG, "Restoring from killed activity");
		}
	}

	private boolean initActivityState(Intent intent) {
		long noteId = intent.getLongExtra(Intent.EXTRA_UID, 0);
		Log.i("NoteId", String.valueOf(noteId));

		if (noteId == 0L) {
			workingNote = new Note();
			if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				workingNote.setType(bgId);
			} else {
				workingNote.setType(ResourceParser.getDefaultBgId(this));
			}
			return true;
		} else {
			workingNote = noteDao.load(noteId);
			mAppWidgetId = workingNote.getWidget();
			if (workingNote == null) {
				Log.e(TAG, "load note failed with note id:" + noteId);
				finish();
				return false;
			}
			return true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		initNoteScreen();
	}

	private void initNoteScreen() {
		mNoteEditor.setText(workingNote.getText());
		mNoteEditor.setSelection(mNoteEditor.getText().length());
		for (Integer id : sBgSelectorSelectionMap.keySet()) {
			findViewById(sBgSelectorSelectionMap.get(id)).setVisibility(
					View.GONE);
		}
		mHeadViewPanel.setBackgroundResource(NoteBgResources
				.getNoteTitleBgResource(workingNote.getType()));
		mNoteEditorPanel.setBackgroundResource(NoteBgResources
				.getNoteBgResource(workingNote.getType()));

		mNoteHeaderHolder.tvModified.setText(DateUtils.formatDateTime(this,
				workingNote.getDate().getTime(), DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_NUMERIC_DATE
						| DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_YEAR));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		initActivityState(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		/**
		 * For new note without note id, we should firstly save it to generate a
		 * id. If the editing note is not worth saving, there is no id which is
		 * equivalent to create new note
		 */
		if (noteDao.load(workingNote.getId()) == null) {
			saveNote();
		}
		outState.putLong(Intent.EXTRA_UID, workingNote.getId());
		Log.d(TAG, "Save working note id: " + workingNote.getId()
				+ " onSaveInstanceState");
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mNoteBgColorSelector.getVisibility() == View.VISIBLE
				&& !inRangeOfView(mNoteBgColorSelector, ev)) {
			mNoteBgColorSelector.setVisibility(View.GONE);
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}

	private boolean inRangeOfView(View view, MotionEvent ev) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1];
		if (ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y
				|| ev.getY() > (y + view.getHeight())) {
			return false;
		}
		return true;
	}

	private void initResources() {
		mHeadViewPanel = findViewById(R.id.note_title);
		mNoteHeaderHolder = new HeadViewHolder();
		mNoteHeaderHolder.tvModified = (TextView) findViewById(R.id.tv_modified_date);
		mNoteHeaderHolder.ibSetBgColor = (ImageView) findViewById(R.id.btn_set_bg_color);
		mNoteHeaderHolder.ibSetBgColor.setOnClickListener(new SetBgColor());
		mNoteEditor = (EditText) findViewById(R.id.note_edit_view);
		mNoteEditorPanel = findViewById(R.id.sv_note_edit);
		mNoteBgColorSelector = findViewById(R.id.note_bg_color_selector);
		for (int id : sBgSelectorBtnsMap.keySet()) {
			ImageView iv = (ImageView) findViewById(id);
			iv.setOnClickListener(new SetBgColor());
		}
	}

	class SetBgColor implements OnClickListener {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.btn_set_bg_color) {
				mNoteBgColorSelector.setVisibility(View.VISIBLE);
				findViewById(sBgSelectorSelectionMap.get(workingNote.getType()))
						.setVisibility(-View.VISIBLE);
			} else if (sBgSelectorBtnsMap.containsKey(id)) {
				findViewById(sBgSelectorSelectionMap.get(workingNote.getType()))
						.setVisibility(View.GONE);
				workingNote.setType(sBgSelectorBtnsMap.get(id));
				mNoteBgColorSelector.setVisibility(View.GONE);
				mNoteEditorPanel.setBackgroundResource(NoteBgResources
						.getNoteBgResource(workingNote.getType()));
				mHeadViewPanel.setBackgroundResource(NoteBgResources
						.getNoteTitleBgResource(workingNote.getType()));
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void updateWidget() {

		Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.setClass(this, NoteWidgetProvider_4x.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				new int[] { mAppWidgetId });
		sendBroadcast(intent);
	}

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_set_bg_color) {
			mNoteBgColorSelector.setVisibility(View.VISIBLE);
			findViewById(
					sBgSelectorSelectionMap.get(NoteItemBgResources
							.getNoteBgNormalRes(workingNote.getType())))
					.setVisibility(-View.VISIBLE);
		} else if (sBgSelectorBtnsMap.containsKey(id)) {
			findViewById(sBgSelectorSelectionMap.get(workingNote.getType()))
					.setVisibility(View.GONE);
			workingNote.setType(sBgSelectorBtnsMap.get(id));
			mNoteBgColorSelector.setVisibility(View.GONE);
		}
	}

	@Override
	public void onBackPressed() {
		saveNote();
		updateWidget();
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
	}

	private void getWorkingText() {
		workingNote.setText(mNoteEditor.getText().toString());
	}

	private boolean saveNote() {
		getWorkingText();
		workingNote.setWidget(mAppWidgetId);
		if (workingNote.getId() != null) {
			noteDao.update(workingNote);
		} else {
			if (TextUtils.isEmpty(workingNote.getText())) {
				return false;
			}
			long saved = noteDao.insert(workingNote);
			Log.i("SaveNewNote",
					"Inserted new note, ID: " + workingNote.getId());
			if (saved != 0) {
				setResult(RESULT_OK);
				return true;
			}
		}
		return false;
	}
}

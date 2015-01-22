package com.bq.note;

import java.util.HashMap;
import java.util.Map;

import org.bq.app.NoteApplication;
import org.bq.db.Note;
import org.bq.db.NoteDao;
import org.bq.tool.ResourceParser;
import org.bq.tool.ResourceParser.NoteBgResources;
import org.bq.tool.ResourceParser.NoteItemBgResources;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;

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

	private static final int SHORTCUT_ICON_TITLE_MAX_LEN = 10;

	public static final String TAG_CHECKED = String.valueOf('\u221A');
	public static final String TAG_UNCHECKED = String.valueOf('\u25A1');

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.note_edit);
		noteDao = NoteApplication.daoSession.getNoteDao();
		if (savedInstanceState == null && !initActivityState(getIntent())) {
			finish();
			return;
		}
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
		long noteId = intent.getLongExtra(Intent.EXTRA_UID, 0L);
		Log.i(TAG, String.valueOf(noteId));

		if (noteId == 0L) {
			workingNote = new Note();
			workingNote.setType(ResourceParser.getDefaultBgId(this));
			return true;
		} else {
			workingNote = noteDao.load(noteId);
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

	/*
	 * private void updateWidget() { Intent intent = new
	 * Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE); if
	 * (workingNote.getWidgetType() == Notes.TYPE_WIDGET_2X) {
	 * intent.setClass(this, NoteWidgetProvider_2x.class); } else if
	 * (workingNote.getWidgetType() == Notes.TYPE_WIDGET_4X) {
	 * intent.setClass(this, NoteWidgetProvider_4x.class); } else { Log.e(TAG,
	 * "Unspported widget type"); return; }
	 * 
	 * intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {
	 * workingNote.getWidgetId() });
	 * 
	 * sendBroadcast(intent); setResult(RESULT_OK, intent); }
	 */

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
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*
		 * switch (item.getItemId()) { case R.id.menu_share: getWorkingText();
		 * sendTo(this, workingNote.getContent()); break; case
		 * R.id.menu_send_to_desktop: sendToDesktop(); break; default: break; }
		 */
		return true;
	}

	/**
	 * Share note to apps that support {@link Intent#ACTION_SEND} action and
	 * {@text/plain} type
	 */
	private void sendTo(Context context, String info) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, info);
		intent.setType("text/plain");
		context.startActivity(intent);
	}

	public void onWidgetChanged() {
		// updateWidget();
	}

	private void getWorkingText() {
		workingNote.setText(mNoteEditor.getText().toString());
	}

	private boolean saveNote() {
		getWorkingText();
		if (workingNote.getId() != null) {
			noteDao.update(workingNote);
			return true;
		} else {
			if (TextUtils.isEmpty(workingNote.getText())) {
				return false;
			}
			long saved = noteDao.insert(workingNote);
			Log.d("SaveNewNote",
					"Inserted new note, ID: " + workingNote.getId());
			if (saved != 0) {
				setResult(RESULT_OK);
				return true;
			}
		}

		return false;
	}

	private void sendToDesktop() {

		if (noteDao.load(workingNote.getId()) != null) {
			saveNote();
		}

		if (workingNote.getId() > 0) {
			Intent sender = new Intent();
			Intent shortcutIntent = new Intent(this, NoteEditActivity.class);
			shortcutIntent.setAction(Intent.ACTION_VIEW);
			shortcutIntent.putExtra(Intent.EXTRA_UID, workingNote.getId());
			sender.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			sender.putExtra(Intent.EXTRA_SHORTCUT_NAME,
					makeShortcutIconTitle(workingNote.getText()));
			sender.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
					Intent.ShortcutIconResource.fromContext(this,
							R.drawable.icon_app));
			sender.putExtra("duplicate", true);
			sender.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
			showToast(R.string.info_note_enter_desktop);
			sendBroadcast(sender);
		} else {
			/**
			 * There is the condition that user has input nothing (the note is
			 * not worthy saving), we have no note id, remind the user that he
			 * should input something
			 */
			Log.e(TAG, "Send to desktop error");
			showToast(R.string.error_note_empty_for_send_to_desktop);
		}
	}

	private String makeShortcutIconTitle(String content) {
		content = content.replace(TAG_CHECKED, "");
		content = content.replace(TAG_UNCHECKED, "");
		return content.length() > SHORTCUT_ICON_TITLE_MAX_LEN ? content
				.substring(0, SHORTCUT_ICON_TITLE_MAX_LEN) : content;
	}

	private void showToast(int resId) {
		showToast(resId, Toast.LENGTH_SHORT);
	}

	private void showToast(int resId, int duration) {
		Toast.makeText(this, resId, duration).show();
	}
}

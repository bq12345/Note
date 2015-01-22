package com.bq.note;

import java.util.HashSet;

import org.bq.app.NoteApplication;
import org.bq.db.Note;
import org.bq.db.NoteDao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class NoteActivity extends Activity {
	// private static final String TAG = "NoteActivity";
	private SQLiteDatabase db;

	private ListView noteListView;
	private ModeCallback mModeCallBack;
	private NoteDao noteDao;
	private Cursor cursor;
	private Button newNote;
	private NoteAdapter adapter;
	private String textColumn = NoteDao.Properties.Text.columnName;
	private String ORDER_BY = textColumn + " COLLATE LOCALIZED ASC";

	@SuppressLint("InflateParams")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		noteDao = NoteApplication.daoSession.getNoteDao();
		db = NoteApplication.getDB();
		noteListView = (ListView) findViewById(R.id.notes_list);
		newNote = (Button) findViewById(R.id.btn_new_note);
		newNote.setOnClickListener(new NewNoteOnClickListener());
		cursor = db.query(noteDao.getTablename(), noteDao.getAllColumns(),
				null, null, null, null, ORDER_BY);
		noteListView.addFooterView(
				LayoutInflater.from(this).inflate(R.layout.note_list_footer,
						null), null, false);
		adapter = new NoteAdapter(this, cursor, true);
		noteListView.setAdapter(adapter);
		noteListView.setOnItemClickListener(new OnMyItemClickListener());
		noteListView
				.setOnItemLongClickListener(new OnMyItemLongClickListener());
		mModeCallBack = new ModeCallback();
	}

	@Override
	protected void onResume() {
		super.onResume();
		cursor.requery();
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		if (noteListView != null) {
			noteListView.setOnCreateContextMenuListener(null);
		}
		super.onContextMenuClosed(menu);
	}

	class ModeCallback implements ListView.MultiChoiceModeListener,
			OnMenuItemClickListener {
		private ActionMode mActionMode;

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getMenuInflater().inflate(R.menu.note_list_options, menu);
			menu.findItem(R.id.delete).setOnMenuItemClickListener(this);

			mActionMode = mode;
			adapter.setChoiceMode(true);
			noteListView.setLongClickable(false);
			newNote.setVisibility(View.GONE);
			return true;
		}

		public void finishActionMode() {
			mActionMode.finish();
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			adapter.setChoiceMode(false);
			noteListView.setLongClickable(true);
			newNote.setVisibility(View.VISIBLE);
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

			return false;
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			if (adapter.getSelectedCount() == 0) {
				Toast.makeText(NoteActivity.this,
						getString(R.string.menu_select_none),
						Toast.LENGTH_SHORT).show();
				return true;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(
					NoteActivity.this);
			builder.setTitle(getString(R.string.alert_title_delete));
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setMessage(getString(R.string.alert_message_delete_notes,
					adapter.getSelectedCount()));
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							batchDelete();
						}
					});
			builder.setNegativeButton(android.R.string.cancel, null);
			builder.show();
			return false;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			adapter.setCheckedItem(position, checked);
		}

	}

	private void batchDelete() {
		new AsyncTask<Integer, Integer, Integer>() {
			protected Integer doInBackground(Integer... params) {
				HashSet<Long> ids = adapter.getSelectedItemIds();
				noteDao.deleteByKeyInTx(ids);
				return ids.size();
			}

			@Override
			protected void onPostExecute(Integer i) {
				cursor = db.query(noteDao.getTablename(),
						noteDao.getAllColumns(), null, null, null, null,
						ORDER_BY);
				adapter.changeCursor(cursor);
				mModeCallBack.finishActionMode();
			}
		}.execute();
	}

	class NewNoteOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(NoteActivity.this,
					NoteEditActivity.class);
			intent.putExtra(Intent.EXTRA_UID, 0);
			startActivity(intent);
		}

	}

	class OnMyItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (adapter.isInChoiceMode()) {
				position = position - noteListView.getHeaderViewsCount();
				mModeCallBack.onItemCheckedStateChanged(null, position, id,
						!adapter.isSelectedItem(position));
			} else {
				Note item = ((NotesListItem) view).getItemData();
				Intent intent = new Intent(NoteActivity.this,
						NoteEditActivity.class);
				intent.putExtra(Intent.EXTRA_UID, item.getId());
				startActivity(intent);
			}
			return;
		}

	}

	class OnMyItemLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			if (view instanceof NotesListItem) {
				position = position - noteListView.getHeaderViewsCount();
				if (noteListView.startActionMode(mModeCallBack) != null) {
					mModeCallBack.onItemCheckedStateChanged(null, position, id,
							!adapter.isSelectedItem(position));
					noteListView
							.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				}
				return true;
			}
			return false;
		}
	}

}
package com.bq.note;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bq.app.NoteApplication;
import org.bq.db.Note;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class NoteAdapter extends CursorAdapter {
	private HashMap<Integer, Boolean> mSelectedIndex;
	private boolean mChoiceMode;

	public NoteAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mSelectedIndex = new HashMap<Integer, Boolean>();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (view instanceof NotesListItem) {
			long id = cursor.getLong(0);
			Note data = NoteApplication.daoSession.getNoteDao().load(id);
			((NotesListItem) view).bind(context, data, mChoiceMode,
					isSelectedItem(cursor.getPosition()));
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new NotesListItem(context);
	}

	public void setChoiceMode(boolean mode) {
		mSelectedIndex.clear();
		mChoiceMode = mode;
	}

	public void setCheckedItem(final int position, final boolean checked) {
		mSelectedIndex.put(position, checked);
		notifyDataSetChanged();
	}

	public boolean isInChoiceMode() {
		return mChoiceMode;
	}

	public HashSet<Long> getSelectedItemIds() {
		HashSet<Long> itemSet = new HashSet<Long>();
		for (Integer position : mSelectedIndex.keySet()) {
			if (mSelectedIndex.get(position) == true) {
				Long id = getItemId(position);
				itemSet.add(id);
			}
		}
		return itemSet;
	}

	public int getSelectedCount() {
		Collection<Boolean> values = mSelectedIndex.values();
		if (null == values) {
			return 0;
		}
		Iterator<Boolean> iter = values.iterator();
		int count = 0;
		while (iter.hasNext()) {
			if (true == iter.next()) {
				count++;
			}
		}
		return count;
	}

	public boolean isSelectedItem(final int position) {
		if (null == mSelectedIndex.get(position)) {
			return false;
		}
		return mSelectedIndex.get(position);
	}

}

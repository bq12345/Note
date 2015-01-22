package com.bq.note;

import org.bq.db.Note;
import org.bq.tool.ResourceParser.NoteItemBgResources;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotesListItem extends LinearLayout {
	private TextView mTitle;
	private TextView mTime;
	private CheckBox mCheckBox;
	private Note data = null;

	public NotesListItem(Context context) {
		super(context);
		inflate(context, R.layout.note_item, this);
		mTitle = (TextView) findViewById(R.id.tv_title);
		mTime = (TextView) findViewById(R.id.tv_time);
		mCheckBox = (CheckBox) findViewById(android.R.id.checkbox);
	}

	public void bind(Context context, Note data, boolean choiceMode,
			boolean checked) {
		this.data = data;
		if (choiceMode) {
			mCheckBox.setVisibility(View.VISIBLE);
			mCheckBox.setChecked(checked);
		} else {
			mCheckBox.setVisibility(View.GONE);
		}

		mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem);

		mTitle.setText(data.getText());

		mTime.setText(DateUtils.getRelativeTimeSpanString(data.getDate()
				.getTime()));
		setBackgroundResource(NoteItemBgResources.getNoteBgNormalRes(data
				.getType()));
	}

	public Note getItemData() {
		return data;
	}

}

package com.bq.widget;

import java.util.List;

import org.bq.app.NoteApplication;
import org.bq.db.Note;
import org.bq.db.NoteDao;
import org.bq.tool.ResourceParser;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.bq.note.NoteEditActivity;
import com.bq.note.R;

public abstract class NoteWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "NoteWidgetProvider";
	private NoteDao noteDao;

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {

	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		update(context, appWidgetManager, appWidgetIds);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	protected void update(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		noteDao = NoteApplication.daoSession.getNoteDao();
		for (int i = 0; i < appWidgetIds.length; i++) {
			Log.i(TAG,appWidgetIds[i]+"");
			if (appWidgetIds[i] != AppWidgetManager.INVALID_APPWIDGET_ID) {
				int bgId = ResourceParser.getDefaultBgId(context);
				Intent intent = new Intent(context, NoteEditActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.setAction(Intent.ACTION_VIEW);
				List<Note> list = noteDao.queryRaw("where widget = ?",
						appWidgetIds[i] + "");
				RemoteViews rv = new RemoteViews(context.getPackageName(),
						getLayoutId());
				if (list.size() == 0) {
					rv.setTextViewText(R.id.widget_text, "µã»÷´´½¨±ãÇ©");
					
				} else {
					Note note = list.get(0);
					rv.setTextViewText(R.id.widget_text, note.getText());
					intent.putExtra(Intent.EXTRA_UID, note.getId());
					bgId = note.getType();
				}
				rv.setImageViewResource(R.id.widget_bg_image,
						getBgResourceId(bgId));
				
				intent.putExtra("org.bq.note.widget_id", appWidgetIds[i]);
				intent.putExtra("org.bq.note.bg_id", bgId);
				PendingIntent pendingIntent = null;
				pendingIntent = PendingIntent.getActivity(context,
						appWidgetIds[i], intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.widget_text, pendingIntent);
				appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
			}
		}
	}

	protected abstract int getBgResourceId(int bgId);

	protected abstract int getLayoutId();

	protected abstract int getWidgetType();
}

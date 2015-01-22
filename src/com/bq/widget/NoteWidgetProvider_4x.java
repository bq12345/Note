package com.bq.widget;

import org.bq.tool.ResourceParser;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import com.bq.note.R;


public class NoteWidgetProvider_4x extends NoteWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.update(context, appWidgetManager, appWidgetIds);
    }

    protected int getLayoutId() {
        return R.layout.widget_4x;
    }

    @Override
    protected int getBgResourceId(int bgId) {
        return ResourceParser.WidgetBgResources.getWidget4xBgResource(bgId);
    }

    @Override
    protected int getWidgetType() {
        return 1;
    }
}

package org.bq.app;

import org.bq.db.DaoMaster;
import org.bq.db.DaoSession;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class NoteApplication extends Application {
	public static Context applicationContext;
	private static NoteApplication instance;
	/**
	 * 持有的greenDao生成的数据库实例
	 */
	public static DaoSession daoSession;
	private static SQLiteDatabase database;

	@Override
	public void onCreate() {
		super.onCreate();
		applicationContext = this;
		instance = this;

		database = new DaoMaster.DevOpenHelper(this, "notes-db", null)
				.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(database);
		daoSession = daoMaster.newSession();
	}

	public static NoteApplication getInstance() {
		return instance;
	}

	public static SQLiteDatabase getDB() {
		return database;
	}
}

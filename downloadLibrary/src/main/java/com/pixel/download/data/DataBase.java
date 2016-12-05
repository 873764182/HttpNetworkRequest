package com.pixel.download.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/11/3.
 */

public class DataBase extends SQLiteOpenHelper {

    public DataBase(Context context) {
        super(context, "pixel_download.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS DownEntity" +
                "(" +
                " _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " downUrl VARCHAR, " +
                " savePath VARCHAR, " +
                " fileSize VARCHAR, " +
                " downSize VARCHAR " +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**************************************************** DAO操作 ****************************************************/

    private static SQLiteDatabase database = null;

    private static SQLiteDatabase getDatabase(Context context) {
        if (database == null) {
            database = new DataBase(context).getWritableDatabase();
        }
        return database;
    }

    /**
     * 执行非查询语句
     */
    public static void executeUpdate(Context context, String sql, String... params) {
        SQLiteDatabase db = getDatabase(context);
        try {
            db.beginTransaction();
            db.execSQL(sql, params);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 执行查询语句
     */
    public static Cursor executeQuery(Context context, String sql, String... params) {
        return getDatabase(context).rawQuery(sql, params);
    }

    public static Cursor executeQuery(Context context, String sql) {
        return getDatabase(context).rawQuery(sql, null);
    }
}

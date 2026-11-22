package com.journalite.journalite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    public AdminSQLiteOpenHelper(Context context) {
        super(context, "journalite.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE homeworks (" +
                "homework_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "subject TEXT NOT NULL, " +
                "description TEXT, " +
                "review_date TEXT NOT NULL, " +
                "has_notifications INTEGER DEFAULT 0, " +
                "status TEXT NOT NULL DEFAULT '" + HomeWork.STATUS_PENDING + "', " +
                "photo_path TEXT, " +
                "photo_date TEXT" +
                ")");
        db.execSQL("CREATE INDEX idx_homeworks_subject ON HomeWorks(subject)");
        db.execSQL("CREATE INDEX idx_homeworks_status ON HomeWorks(status)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS homeworks");
        onCreate(db);
    }

    public long insertHomeWork(HomeWork homeWork) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", homeWork.getTitle());
        values.put("subject", homeWork.getSubject());
        values.put("description", homeWork.getDescription());
        values.put("review_date", homeWork.getReviewDate());
        values.put("has_notifications", homeWork.hasNotifications() ? 1 : 0);
        values.put("status", HomeWork.STATUS_PENDING);

        long id = db.insert("HomeWorks", null, values);
        return id;
    }

    public HomeWork getHomeWork(int id) {
        SQLiteDatabase db = getReadableDatabase();
        HomeWork homeWork = null;
        String[] columns = {"homework_id", "title", "subject", "description", "review_date", "has_notifications", "status", "photo_path", "photo_date"};
        String[] params = {String.valueOf(id)};
        Cursor cursor = db.query("homeworks", columns, "homework_id = ?", params, null, null, null);
        if (cursor.moveToFirst()) {
            homeWork = new HomeWork(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5) == 1,
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
        }
        cursor.close();
        return homeWork;
    }

    public int updateEvidence(int id, String photoPath, String photoDate) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("photo_path", photoPath);
        values.put("photo_date", photoDate);
        values.put("status", HomeWork.STATUS_COMPLETED);
        return db.update("homeworks", values, "homework_id = ?", new String[]{String.valueOf(id)});
    }

    public int deleteHomeWork(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("homeworks", "homework_id = ?", new String[]{String.valueOf(id)});
    }
}

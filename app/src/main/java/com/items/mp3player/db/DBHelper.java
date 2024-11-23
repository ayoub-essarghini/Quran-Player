package com.items.mp3player.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.items.mp3player.model.AudioModel;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PATH = "path";
    public static final String COLUMN_FILE_NAME = "fileName";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_TITLE = "title";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PATH + " TEXT, " +
                    COLUMN_FILE_NAME + " TEXT, " +
                    COLUMN_DURATION + " INTEGER, " +
                    COLUMN_ARTIST + " TEXT, " +
                    COLUMN_TITLE + " TEXT);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    // Add a favorite
    public void addFavorite(AudioModel audio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATH, audio.getPath());
        values.put(COLUMN_FILE_NAME, audio.getFileName());
        values.put(COLUMN_DURATION, audio.getDuration());
        values.put(COLUMN_ARTIST, audio.getArtist());
        values.put(COLUMN_TITLE, audio.getTitle());

        db.insert(TABLE_FAVORITES, null, values);
        db.close();

    }

    // Get all favorites
    public ArrayList<AudioModel> getAllFavorites() {
        ArrayList<AudioModel> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String path = cursor.getString(cursor.getColumnIndex(COLUMN_PATH));
                String fileName = cursor.getString(cursor.getColumnIndex(COLUMN_FILE_NAME));
                long duration = cursor.getLong(cursor.getColumnIndex(COLUMN_DURATION));
                String artist = cursor.getString(cursor.getColumnIndex(COLUMN_ARTIST));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));

                favorites.add(new AudioModel(path, fileName, duration, artist, title));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return favorites;
    }

    // Delete a favorite
    public void deleteFavorite(String fileName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, COLUMN_FILE_NAME + " = ?", new String[]{fileName});
        db.close();
    }

    // Check if a favorite song exists in the database
    public boolean isFavorite(String fileName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + COLUMN_FILE_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{fileName});

        boolean exists = cursor.getCount() > 0; // If count is greater than 0, the song exists

        cursor.close();
        db.close();
        return exists;
    }

}

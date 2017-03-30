package com.roberts.adrian.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.roberts.adrian.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by Adrian on 10/03/2017.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";

    public static final int DATABASE_VERSION = 4;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE =
                "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                        MovieEntry.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY, " +
                        MovieEntry.COLUMN_MOVIE_ORIGINAL_TITLE + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_MOVIE_ALTERNATIVE_TITLE + " STRING, " +
                        MovieEntry.COLUMN_MOVIE_RELEASE_DATE + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_MOVIE_SYNOPSIS + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_MOVIE_POSTER + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_MOVIE_THUMBNAIL + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_MOVIE_RATING + " DOUBLE NOT NULL, " +
                        MovieEntry.COLUMN_MOVIE_RUNTIME + " INTEGER, " +
                        MovieEntry.COLUMN_MOVIE_BY_POPULARITY + " INTEGER DEFAULT 0, " +
                        MovieEntry.COLUMN_MOVIE_BY_RATING + " INTEGER DEFAULT 0, " +
                        MovieEntry.COLUMN_MOVIE_BY_FAVOURITE + " INTEGER DEFAULT 0, " +
                        MovieEntry.COLUMN_STORED_MOVIE_THUMBNAIL + " BLOB, " +
                        MovieEntry.COLUMN_STORED_MOVIE_POSTER + " BLOB" +
                        ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

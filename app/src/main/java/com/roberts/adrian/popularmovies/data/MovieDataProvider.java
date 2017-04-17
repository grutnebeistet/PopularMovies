package com.roberts.adrian.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.roberts.adrian.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by Adrian on 10/03/2017.
 */

public class MovieDataProvider extends ContentProvider {
    private static final String LOG_TAG = MovieDataProvider.class.getSimpleName();

    MovieDbHelper mDbHelper;
    /**
     * URI matcher codes for the content URI:
     * MOVIES for general table query
     * MOVIE_ID for query on a specific movie
     */
    private static final int MOVIES = 100;
    private static final int MOVIE_ID = 101;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        mUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES, MOVIES);

        mUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES + "/#", MOVIE_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        int uriMatch = mUriMatcher.match(uri);

        Cursor returnCursor;

        switch (uriMatch) {
            case MOVIES:
                returnCursor = db.query(MovieContract.MovieEntry.TABLE_NAME, projection, selection, selArgs, null, null, sortOrder);
                break;
            case MOVIE_ID:
                selection = MovieEntry.COLUMN_MOVIE_ID + "=?";
                selArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                returnCursor = db.query(MovieContract.MovieEntry.TABLE_NAME, projection, selection, selArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query given uri: " + uri);
        }
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long newRowId;
        newRowId = db.insert(MovieEntry.TABLE_NAME, null, contentValues);
        if (newRowId == -1) {
            Log.e(LOG_TAG, "insertion failed for " + uri);
            return null;
        }
        // Return the Uri for the newly added Movie
        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int deletedRows;
        switch (mUriMatcher.match(uri)) {
            case MOVIES:
                deletedRows = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_ID:
                selection = MovieEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deletedRows = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Failed to delete: " + uri);
        }
        if (deletedRows != 0) getContext().getContentResolver().notifyChange(uri, null);
        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        // return early if there's no values to update
        if (contentValues.size() == 0) return 0;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated;

        switch (mUriMatcher.match(uri)) {
            case MOVIES:
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case MOVIE_ID:
                selection = MovieEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                // Log.i(LOG_TAG, "updated movie");
                break;
            default:
                throw new IllegalArgumentException("Cannot update for given uri " + uri);
        }
        //  if (rowsUpdated != 0) getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final SQLiteDatabase rDb = mDbHelper.getReadableDatabase();

        switch (mUriMatcher.match(uri)) {

            case MOVIES:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        // To not try insertion on existing movie
                        if (value.get(MovieEntry.COLUMN_MOVIE_BY_FAVOURITE).equals(0)){
                            long _id = db.insert(MovieEntry.TABLE_NAME, null, value);

                            if (_id != -1) {

                                rowsInserted++;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                Log.i(LOG_TAG, "inserted: " + rowsInserted + " rows");
                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = mUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return MovieEntry.CONTENT_MOVIE_LIST_TYPE;
            case MOVIE_ID:
                return MovieEntry.CONTENT_MOVIE_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

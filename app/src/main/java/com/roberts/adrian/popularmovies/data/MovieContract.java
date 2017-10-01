package com.roberts.adrian.popularmovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.roberts.adrian.popularmovies.BuildConfig;

/**
 * Created by Adrian on 10/03/2017.
 */

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.roberts.adrian.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    /**
     * Constants; parts of the query to tMDB
     */
    public static final String BASE_MDB_URL = "http://api.themoviedb.org/3/movie/";
    public final static String API_LABEL = "api_key";
    public final static String API_KEY = BuildConfig.API_KEY;

    public static final String BASE_POSTER_PATH_URL = "http://image.tmdb.org/t/p/";
    public static final String DEFAULT_POSTER_SIZE = "w185";
    public static final String DEFAULT_BACKPOSTER_SIZE = "w500";

    public static final String MOVIE_TRAILERS_PATH = "videos";
    public static final String MOVIE_REVIEWS_PATH = "reviews";
    public static final String MOVIE_IMAGES_PATH = "images";


    public static final class MovieEntry implements BaseColumns {
        public static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of movies.
         */
        public static final String CONTENT_MOVIE_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single movie.
         */
        public static final String CONTENT_MOVIE_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        /**
         * Extend the favorites ContentProvider to store the movie poster, synopsis, user rating,
         * and release date, and display them even when offline.
         */
        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_MOVIE_ORIGINAL_TITLE = "title";

        public static final String COLUMN_MOVIE_ALTERNATIVE_TITLE = "alt_title";

        public static final String COLUMN_MOVIE_SYNOPSIS = "synopsis";

        public static final String COLUMN_MOVIE_RELEASE_DATE = "release_date";

        public static final String COLUMN_MOVIE_THUMBNAIL = "thumbnail";

        public static final String COLUMN_MOVIE_POSTER = "poster";

        public static final String COLUMN_MOVIE_RATING = "rating";

        public static final String COLUMN_MOVIE_RUNTIME = "runtime";

        public static final String COLUMN_MOVIE_BY_RATING = "by_rating";

        public static final String COLUMN_MOVIE_BY_POPULARITY = "by_popularity";

        public static final String COLUMN_MOVIE_BY_FAVOURITE = "by_favourite";

        public static final String COLUMN_STORED_MOVIE_THUMBNAIL = "stored_thumbnail";

        public static final String COLUMN_STORED_MOVIE_POSTER = "stored_poster";

    }
}

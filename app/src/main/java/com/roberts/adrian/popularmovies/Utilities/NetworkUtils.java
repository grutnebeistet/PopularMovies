package com.roberts.adrian.popularmovies.utilities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.roberts.adrian.popularmovies.MovieExtraData;
import com.roberts.adrian.popularmovies.MovieReview;
import com.roberts.adrian.popularmovies.MovieTrailer;
import com.roberts.adrian.popularmovies.R;
import com.roberts.adrian.popularmovies.data.MovieContract;
import com.roberts.adrian.popularmovies.data.MovieDbHelper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.roberts.adrian.popularmovies.data.MovieContract.API_KEY;
import static com.roberts.adrian.popularmovies.data.MovieContract.API_LABEL;
import static com.roberts.adrian.popularmovies.data.MovieContract.BASE_MDB_URL;
import static com.roberts.adrian.popularmovies.data.MovieContract.MOVIE_REVIEWS_PATH;
import static com.roberts.adrian.popularmovies.data.MovieContract.MOVIE_TRAILERS_PATH;

/**
 * Created by Adrian on 07/02/2017.
 */

public class NetworkUtils extends AsyncTask<Integer, Void, MovieExtraData> {
    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    private final static Uri baseUri = Uri.parse(BASE_MDB_URL);

    /**
     * Method for checking internet connection status
     *
     * @param context
     * @return true if the user has a internet connection, false otherwise
     */
    public static boolean workingConnection(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * Building and returning a URL based on the current user preferences (sort-order)
     *
     * @param context
     * @return
     */
    private static URL getUrl(Context context) {
        SharedPreferences userPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String orderBy = userPreferences.getString(
                context.getString(R.string.settings_order_by_key),
                context.getString(R.string.settings_order_by_default));

        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendPath(orderBy).appendQueryParameter(API_LABEL, API_KEY);

        return createUrlFromString(uriBuilder.toString());

    }

    /**
     * Just a small method to get a Url from String
     *
     * @param stringUrl
     * @return
     */
    private static URL createUrlFromString(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException me) {
            Log.e(LOG_TAG, me.getMessage());
        }
        return url;
    }

    // Updating both Popular and High Rated movie tables, called from FirebaseJobService
    public static void updateMovieLists(Context context) {
        try {
            URL popMovies = createUrlFromString(baseUri.buildUpon()
                    .appendPath("popular").appendQueryParameter(API_LABEL, API_KEY).toString());


            URL ratedMovies = createUrlFromString(baseUri.buildUpon()
                    .appendPath("top_rated").appendQueryParameter(API_LABEL, API_KEY).toString());

            String popJson = httpRequest(popMovies);
            String ratedJson = httpRequest(ratedMovies);

            ContentValues[] popValues = JsonUtils.getMovieContentValuesFromJson(context, popJson);
            ContentValues[] ratedValues = JsonUtils.getMovieContentValuesFromJson(context, ratedJson);
            ContentValues[] allMovies = new ContentValues[popValues.length + ratedValues.length];
            System.arraycopy(popValues, 0, allMovies, 0, popValues.length);
            System.arraycopy(ratedValues, 0, allMovies, popValues.length, ratedValues.length);

            if (allMovies.length != 0){
                ContentResolver contentResolver = context.getContentResolver();
                // Look through IDs, add new ones

                MovieDbHelper dbHelper = new MovieDbHelper(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                for (ContentValues values : allMovies) {
                    int id = values.getAsInteger(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                    String where = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
                    String[] arg = {"" + id};
                    Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                            null,
                            where,
                            arg,
                            null,
                            null,
                            null);
                    if (cursor == null || cursor.getCount() == 0) {
                        contentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, values);
                    }
                    cursor.close();
                }
            }

        } catch (
                Exception e)

        {
            Log.e(LOG_TAG, e.getMessage());
        }

    }

    /**
     * Main fetching method, used to acquire and insert contentValues from main query
     * http://api.themoviedb.org/3/movie/popular or .../movie/top_rated
     *
     * @param context
     */

    public static void fetchMovieData(Context context) {
        try {
            URL movieQueryUrl = getUrl(context);
            String jsonResponse = httpRequest(movieQueryUrl);
            Log.i(LOG_TAG, "setter opp content, caller JSON");
            ContentValues[] moviesContentValues = JsonUtils.getMovieContentValuesFromJson(context, jsonResponse);

            if (moviesContentValues != null && moviesContentValues.length != 0) {
                ContentResolver contentResolver = context.getContentResolver();


                contentResolver.bulkInsert(
                        MovieContract.MovieEntry.CONTENT_URI,
                        moviesContentValues
                );

                /** download poster images. The idea is to reduce lag on initial startup by going for poster images AFTER
                 * the main query is done (so that the movie thumbnail can start displaying while still downloading posters)
                 *       - bad/messy practice?*/

                Log.i(LOG_TAG, "i gang med postere inni main fetch");
                for (ContentValues values : moviesContentValues) {
                    String url = values.getAsString(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER);
                    int id = values.getAsInteger(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                    Uri uri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id);
                    // Log.i(LOG_TAG, "getasstring: " + url);

                    Bitmap poster = null;
                    try {
                        InputStream is = new java.net.URL(url).openStream();
                        poster = BitmapFactory.decodeStream(is);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    poster.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] posterByteArray = stream.toByteArray();
                    ContentValues updateValue = new ContentValues();
                    updateValue.put(MovieContract.MovieEntry.COLUMN_STORED_MOVIE_POSTER, posterByteArray);
                    context.getContentResolver().update(uri, updateValue, null, null);

                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Initializing the main query, setting up a background thread if conditions are appropriate
     * before calling fetchMovieData
     *
     * @param context
     */
    public static void initializeQueryMain(final Context context) {
        Log.i(LOG_TAG, "initializeQueryMain");
        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {

                SharedPreferences userPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                String prefMovieList = userPreferences.getString(
                        context.getString(R.string.settings_order_by_key),
                        context.getString(R.string.settings_order_by_default));

                /* URI for every row of movie data*/
                Uri moviesQueryUri = MovieContract.MovieEntry.CONTENT_URI;

                String[] projectionColumns = {MovieContract.MovieEntry.COLUMN_MOVIE_ID};

                String selectionStatement = null;

                /** If somehow the user settings are set to show favourite movies, there's nothing to query for - return*/
                if (prefMovieList.equals(context.getString(R.string.settings_order_by_favourite_value))) {
                    return;

                } else if (prefMovieList.equals(context.getString(R.string.settings_order_by_rating_value))) {
                    selectionStatement = MovieContract.MovieEntry.COLUMN_MOVIE_BY_RATING + " == 1";
                } else if (prefMovieList.equals(context.getString(R.string.settings_order_by_popularity_value))) {
                    selectionStatement = MovieContract.MovieEntry.COLUMN_MOVIE_BY_POPULARITY + " == 1";
                }

                /* Here, we perform the query to check to see if we have any  data */
                Cursor cursor = context.getContentResolver().query(
                        moviesQueryUri,
                        projectionColumns,
                        selectionStatement,
                        null,
                        null);

                if (null == cursor || cursor.getCount() == 0) {
                    fetchMovieData(context);
                }
                cursor.close();
            }
        });
        checkForEmpty.start();
    }

    /**
     * Method for fetching additional data, trailers and reviews, used in the movie details view
     *
     * @param integers
     * @return
     */
    @Override
    protected MovieExtraData doInBackground(Integer... integers) {
        int movieId = integers[0];

        Uri.Builder uriBuilderTrailers
                = baseUri.buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(MOVIE_TRAILERS_PATH)
                .appendQueryParameter(API_LABEL, API_KEY);

        Uri.Builder uriBuilderReviews
                = baseUri.buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(MOVIE_REVIEWS_PATH)
                .appendQueryParameter(API_LABEL, API_KEY);


        String trailersUrl = uriBuilderTrailers.toString();
        Log.i(LOG_TAG, trailersUrl);
        String reviewsUrl = uriBuilderReviews.toString();
        Log.i(LOG_TAG, reviewsUrl);

        URL queryUrlTrailers = createUrlFromString(trailersUrl);
        URL queryUrlReviews = createUrlFromString(reviewsUrl);

        String trailersJson = "";
        String reviewsJson = "";

        try {
            trailersJson = httpRequest(queryUrlTrailers);
            reviewsJson = httpRequest(queryUrlReviews);
            Log.i(LOG_TAG, "TrailerJSON " + trailersJson);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, ioe.getMessage());
        }
        ArrayList<MovieTrailer> videoTrailers = JsonUtils.parseJsonTrailers(trailersJson);
        ArrayList<MovieReview> videoReviews = JsonUtils.parseJsonReviews(reviewsJson);

        return new MovieExtraData(videoTrailers, videoReviews);
    }

    @Override
    protected void onPostExecute(MovieExtraData movieExtraData) {
        super.onPostExecute(movieExtraData);

    }

    /**
     * This method is for getting a movies runtime based on a single movie's (ID) JSON response
     */
    public static MovieExtraData initializeQueryExtraDetails(final Context context, final int movieId) {
        Thread fetchExtraDetails = new Thread(new Runnable() {
            @Override
            public void run() {
                Uri.Builder uriBuilderExtra
                        = baseUri.buildUpon()
                        .appendPath(String.valueOf(movieId))
                        .appendQueryParameter(API_LABEL, API_KEY);
                String detailsUrl = uriBuilderExtra.toString();

                URL queryUrlDetails = createUrlFromString(detailsUrl);


                String detailsJson = "";
                try {
                    detailsJson = httpRequest(queryUrlDetails);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ContentValues extraValues = JsonUtils.parseMovieRuntime(detailsJson);

                ContentResolver contentResolver = context.getContentResolver();
                Uri contentUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, movieId);

                contentResolver.update(contentUri, extraValues, null, null);
            }
        });
        fetchExtraDetails.start();

        AsyncTask<Integer, Void, MovieExtraData> task = new NetworkUtils();
        task.execute(movieId);

        MovieExtraData extraData = null;
        try {
            extraData = task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean ex = extraData == null;
        Log.i(LOG_TAG, "ExtraData == null " + ex);
        return extraData;
    }


    /**
     * Establishes connection to tMDB and fetches an inputStream with data
     *
     * @return a string of raw JSON data
     */
    private static String httpRequest(URL queryUrl) throws IOException {
        String jsonResponse = "";
        // return early if URL is empty
        if (queryUrl == null) return jsonResponse;

        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            connection = (HttpURLConnection) queryUrl.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.connect();

            // Log error on unsuccessful connection
            if (connection.getResponseCode() != 200) {
                Log.e(LOG_TAG, "ResponseCode: " + connection.getResponseCode());
            }
            inputStream = connection.getInputStream();
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                String line = reader.readLine();
                while (line != null) {
                    //output.append(line);
                    jsonResponse += line;
                    line = reader.readLine();
                }
            }
        } catch (IOException ioe) {
            Log.e(LOG_TAG, ioe.getMessage());
        } finally {
            if (connection != null) connection.disconnect();
            if (inputStream != null) inputStream.close();
        }
        return jsonResponse;
    }
}


package com.roberts.adrian.popularmovies.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import com.roberts.adrian.popularmovies.MovieReview;
import com.roberts.adrian.popularmovies.MovieTrailer;
import com.roberts.adrian.popularmovies.R;
import com.roberts.adrian.popularmovies.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static com.roberts.adrian.popularmovies.data.MovieContract.BASE_POSTER_PATH_URL;
import static com.roberts.adrian.popularmovies.data.MovieContract.DEFAULT_BACKPOSTER_SIZE;
import static com.roberts.adrian.popularmovies.data.MovieContract.DEFAULT_POSTER_SIZE;

/**
 * Created by Adrian on 13/03/2017.
 */

public class JsonUtils {
    private static final String LOG_TAG = JsonUtils.class.getSimpleName();

    public static ContentValues[] getMovieContentValuesFromJson(Context context, String jsonResponse)
            throws JSONException {

        SharedPreferences userPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String orderBy = userPreferences.getString(
                context.getString(R.string.settings_order_by_key),
                context.getString(R.string.settings_order_by_default));

        JSONObject baseResponse = new JSONObject(jsonResponse);
        JSONArray movieResults = baseResponse.getJSONArray("results");
        ContentValues[] contentValues = new ContentValues[movieResults.length()];

        for (int i = 0; i < movieResults.length(); i++) {
            JSONObject movieJSON = movieResults.getJSONObject(i);
            ContentValues values = new ContentValues();

            int movieId = movieJSON.getInt("id");
            String englishTitle = movieJSON.getString("title");
            String orgTitle = movieJSON.getString("original_title");
            double rating = movieJSON.getDouble("vote_average");
            String releaseDate = movieJSON.getString("release_date");
            String thumbnailKey = movieJSON.getString("poster_path");
            String backposterKey = movieJSON.getString("backdrop_path");
            String overview = movieJSON.getString("overview");
            final int STORE_AS_FAVOURITE = 0;
            int store_as_rated = orderBy.equals(context.getString(R.string.settings_order_by_rating_value)) ? 1 : 0;
            int store_as_popular = orderBy.equals(context.getString(R.string.settings_order_by_popularity_value)) ? 1 : 0;

            String thumbnailUrl = BASE_POSTER_PATH_URL.concat(DEFAULT_POSTER_SIZE).concat(thumbnailKey);
           // Log.i(LOG_TAG, thumbnailUrl);
            String backposterUrl = BASE_POSTER_PATH_URL.concat(DEFAULT_BACKPOSTER_SIZE).concat(backposterKey);
            Bitmap thumbnailBitmap = null;

           try {
                InputStream in = new java.net.URL(thumbnailUrl).openStream();
                thumbnailBitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            ByteArrayOutputStream bosThumb = new ByteArrayOutputStream();
            thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bosThumb);

            byte[] thumbnailByteArray = bosThumb.toByteArray();

            values.put(MovieEntry.COLUMN_STORED_MOVIE_THUMBNAIL, thumbnailByteArray);
            values.put(MovieEntry.COLUMN_MOVIE_ID, movieId);
            values.put(MovieEntry.COLUMN_MOVIE_ORIGINAL_TITLE, orgTitle);
            values.put(MovieEntry.COLUMN_MOVIE_ALTERNATIVE_TITLE, englishTitle);
            values.put(MovieEntry.COLUMN_MOVIE_SYNOPSIS, overview);
            values.put(MovieEntry.COLUMN_MOVIE_RELEASE_DATE, releaseDate);
            values.put(MovieEntry.COLUMN_MOVIE_THUMBNAIL, thumbnailUrl);
            values.put(MovieEntry.COLUMN_MOVIE_POSTER, backposterUrl);
            values.put(MovieEntry.COLUMN_MOVIE_RATING, rating);
            values.put(MovieEntry.COLUMN_MOVIE_BY_FAVOURITE, STORE_AS_FAVOURITE);
            values.put(MovieEntry.COLUMN_MOVIE_BY_RATING, store_as_rated);
            values.put(MovieEntry.COLUMN_MOVIE_BY_POPULARITY, store_as_popular);


            contentValues[i] = values;
        }
        return contentValues;
    }


    public static ArrayList<MovieTrailer> parseJsonTrailers(String json) {
        // Map<String, String> videoTrailers = new HashMap<>();
        ArrayList<MovieTrailer> videoTrailers = new ArrayList<>();

        try {
            JSONObject baseResponse = new JSONObject(json);
            JSONArray videosResults = baseResponse.getJSONArray("results");

            for (int i = 0; i < videosResults.length(); i++) {
                JSONObject video = videosResults.getJSONObject(i);
                if (video.getString("type").equals("Trailer")) {
                    String videoName = video.getString("name");
                    String videoKey = video.getString("key");
                    int videoSize = video.getInt("size");

                    videoTrailers.add(new MovieTrailer(videoName, videoKey, videoSize));
                    Log.i(LOG_TAG, "added trailer " + videoName);
                }

            }
        } catch (JSONException je) {
            Log.e(LOG_TAG, "ParseJson" + je.toString());
        }
        return videoTrailers;
    }

    public static ContentValues parseMovieRuntime(String json) {
        ContentValues values = new ContentValues();
        int runtime;
        try {
            JSONObject base = new JSONObject(json);
            runtime = base.getInt("runtime");
            values.put(MovieEntry.COLUMN_MOVIE_RUNTIME, runtime);
        } catch (JSONException je) {

        }
        return values;
    }

    public static ArrayList<MovieReview> parseJsonReviews(String json) {
        // HashMap<String, String> videoReviews = new HashMap<>();
        ArrayList<MovieReview> videoReviews = new ArrayList<>();
        try {
            JSONObject baseResponse = new JSONObject(json);
            JSONArray reviewsResults = baseResponse.getJSONArray("results");

            for (int i = 0; i < reviewsResults.length(); i++) {
                JSONObject review = reviewsResults.getJSONObject(i);
                String author = review.getString("author");
                String content = review.getString("content");
                String url = review.getString("url");
                videoReviews.add(new MovieReview(author, content, url));
                // videoReviews.put(author, content);
            }
        } catch (JSONException je) {
            Log.e(LOG_TAG, "ParseJson" + je.toString());
        }
        return videoReviews;
    }

}

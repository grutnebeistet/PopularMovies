package com.roberts.adrian.popularmovies.Utilities;

import android.util.Log;

import com.roberts.adrian.popularmovies.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrian on 07/02/2017.
 */

public class NetworkUtils {
    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();


    /** Calls httpRequest() to get the JSON response from the Url, after which the data is parsed to Movie objects - parseJson
     * Returns a list of Movie objects to the UI thread(?)
     * @param stringUrl the url to make the query on.
     * @return a List<Movie>
     */
    public static List<Movie> fetchMovieData(String stringUrl) {
        URL queryUrl = createUrl(stringUrl);
        if (queryUrl == null) return null;

        String jsonResponse = "";
        try {
            jsonResponse = httpRequest(queryUrl);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, ioe.getMessage());
        }
        List<Movie> movies = parseJson(jsonResponse);
        return movies;
    }

    // Parsing of JSON data which is used to create Movie objects and at the same time store these objects in an ArrayList
    private static List<Movie> parseJson(String json) {
        List<Movie> movies = new ArrayList<>();
        try {
            JSONObject baseResponse = new JSONObject(json);
            JSONArray movieResults = baseResponse.getJSONArray("results");
            for (int i = 0; i < movieResults.length(); i++) {
                JSONObject movieJSON = movieResults.getJSONObject(i);
                // movieId can be used later to fetch more data (STAGE 2? TODO)
                int movieId = movieJSON.getInt("id");

                String title = movieJSON.getString("title");
                String orgTitle = movieJSON.getString("original_title");
                double rating = movieJSON.getDouble("vote_average");
                String releaseDate = movieJSON.getString("release_date");
                String thumbnail = movieJSON.getString("poster_path");
                String backposter = movieJSON.getString("backdrop_path");
                String overview = movieJSON.getString("overview");
                int runtime = 123;//getRuntime(movieId);
                //movieJSON.getInt("runtime");
                Log.i(LOG_TAG, "ID: " + movieJSON.getInt("id") + "\nntitle " + title);
                movies.add(new Movie(title, orgTitle, overview, releaseDate, thumbnail, backposter, rating, runtime));
            }

        } catch (JSONException je) {
            Log.e(LOG_TAG, "ParseJson" +je.toString());
        }
        return movies;
    }
    /**
     * TODO find a better way
     * Seems to be a bad solution to start a new query for every movie
     *
     * @param movieId
     * @return
     */
    private static int getRuntime(int movieId) {
        String movieQuery = "http://api.themoviedb.org/3/movie/" + movieId + "?api_key=0f4813b07b3a6aa68a3d632f384f7faa";
        URL movieUrl = createUrl(movieQuery);
        String movieJson = "";
        try {
            movieJson = httpRequest(movieUrl);
        } catch (IOException ioe) {

        }
        int runtime = 0;
        try {
            JSONObject base = new JSONObject(movieJson);
            runtime = base.getInt("runtime");
        } catch (JSONException je) {

        }
        return runtime;
    }
    // Just a small method to get a Url from String
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException me) {
            Log.e(LOG_TAG, me.getMessage());
        }
        return url;
    }

    /**
     * Establishes connection to tMDB and fetches an inputstream with data
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

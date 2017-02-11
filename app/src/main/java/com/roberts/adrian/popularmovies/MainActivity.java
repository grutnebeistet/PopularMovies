package com.roberts.adrian.popularmovies;

import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>> {
    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    /**
     * Constant value for the Movie loader ID.
     */
    private static final int MOVIE_LOADER_ID = 1;

    /**
     * Constants; parts of the query to tMDB
     */
    private static final String BASE_MDB_URL = "http://api.themoviedb.org/3/movie/";
    private final static String API_LABEL = "api_key";
    private final static String API_KEY = ""; // TODO

    private SharedPreferences userPreferences;
    private GridView moviesGridView;
    private MovieAdapter movieAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private ImageView noConnectionImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

        moviesGridView = (GridView) findViewById(R.id.moviesGridView);
        movieAdapter = new MovieAdapter(this, new ArrayList<Movie>());

        moviesGridView.setAdapter(movieAdapter);
        emptyView = (TextView) findViewById(R.id.emptyView);
        moviesGridView.setEmptyView(emptyView);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        noConnectionImageView = (ImageView) findViewById(R.id.noWifiImageView);

        if (isConnected) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(MOVIE_LOADER_ID, null, this);
        }
        if (!isConnected) {
            progressBar.setVisibility(View.GONE);
           // noConnectionImageView.setImageResource((R.drawable.ic_no_wifi));
            emptyView.setText(R.string.empty_view_no_connection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_popular:
                userPreferences.edit().putString(getString(R.string.settings_order_by_key),
                        getString(R.string.settings_order_by_popularity_value)).apply();
                getLoaderManager().restartLoader(0, null, this);
                return true;
            case R.id.sort_rating:
                userPreferences.edit().putString(getString(R.string.settings_order_by_key),
                        getString(R.string.settings_order_by_rating_value)).apply();
                getLoaderManager().restartLoader(0, null, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        movieAdapter.clear();
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int i, Bundle bundle) {
        userPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = userPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        Log.i(LOG_TAG, "OrderBy: " + orderBy);

        Uri baseUri = Uri.parse(BASE_MDB_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendPath(orderBy).appendQueryParameter(API_LABEL, API_KEY);
        Log.i(LOG_TAG, "URI: " + uriBuilder.toString());
        return new MovieLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        movieAdapter.clear();
        if (movies != null && !movies.isEmpty()) {
            movieAdapter.addAll(movies);
        }
        progressBar.setVisibility(View.GONE);
        emptyView.setText(R.string.empty_view_text);
    }
}

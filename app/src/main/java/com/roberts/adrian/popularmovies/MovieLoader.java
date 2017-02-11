package com.roberts.adrian.popularmovies;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.roberts.adrian.popularmovies.Utilities.NetworkUtils;

import java.util.List;

/**
 * Created by Adrian on 07/02/2017.
 *
 */

public class MovieLoader extends AsyncTaskLoader<List<Movie>> {

    private final static String LOG_TAG = MovieLoader.class.getName();
    /** Query URL */
    private String mUrl;

    private List<Movie> mMovieList;

    public MovieLoader(Context context, String url) {
        super(context);
        mUrl = url;
        Log.i(LOG_TAG, "mUrl = " + mUrl);
    }
    @Override
    protected void onStartLoading() {
        //super.onStartLoading();
        forceLoad();
    }

    @Override
    public List<Movie> loadInBackground() {
        if(mUrl == null) return null;
        mMovieList = NetworkUtils.fetchMovieData(mUrl);
        return mMovieList;
    }
}

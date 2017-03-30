package com.roberts.adrian.popularmovies;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.roberts.adrian.popularmovies.data.MovieContract;
import com.roberts.adrian.popularmovies.utilities.NetworkUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        MovieAdapter.MovieAdapterOnClickHandler {
    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String[] MAIN_MOVIE_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_MOVIE_THUMBNAIL,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_BY_FAVOURITE,
            MovieContract.MovieEntry.COLUMN_STORED_MOVIE_THUMBNAIL,

    };


    public static final int INDEX_MOVIE_THUMBNAIL = 0;
    public static final int INDEX_MOVIE_ID = 1;
    public static final int INDEX_IS_FAVOURITE = 2;
    public static final int INDEX_STORED_THUMBNAIL = 3;


    /**
     * Constant value for the Movie loader ID.
     */
    private static final int MOVIE_LOADER_ID = 1;

    private int mPosition = RecyclerView.NO_POSITION;


    private SharedPreferences userPreferences;

    private MovieAdapter movieAdapter;

    /**
     * fields for the sort menu and navigation drawer/
     */
    private String[] movieListTitles;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.left_drawer)
    ListView mDrawerList;

    @BindView(R.id.emptyView)
    TextView emptyView;
    @BindView(R.id.recyclerViewMain)
    RecyclerView moviesRecyclerView;
    @BindView(R.id.progressbar)
    ProgressBar progressBar;

    private final int MOVIELIST_POPULAR_INDEX = 0;
    private final int MOVIELIST_RATED_INDEX = 1;
    private final int MOVIELIST_FAVOURITES_INDEX = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        Log.i(LOG_TAG, "ONCRATE");

        userPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mTitle = mDrawerTitle = getTitle();
        movieListTitles = getResources().getStringArray(R.array.movie_list_titles);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, movieListTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);


        int gridSpan = calculateNoOfColumns(getApplicationContext());
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //gridSpan = 2;
        } else {
            //  gridSpan = 3;
        }
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), gridSpan);

        moviesRecyclerView.setLayoutManager(layoutManager);
        moviesRecyclerView.setHasFixedSize(true);


        String orderPref;
        orderPref = userPreferences.getString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));
        Log.i(LOG_TAG, "orderPref onCreate " + orderPref);
        if (savedInstanceState == null) {
            int selectItem;
            if (orderPref.equals(getString(R.string.settings_order_by_popularity_value))) {
                selectItem = MOVIELIST_POPULAR_INDEX;
            } else if (orderPref.equals(getString(R.string.settings_order_by_rating_value))) {
                selectItem = MOVIELIST_RATED_INDEX;
            } else {
                selectItem = MOVIELIST_FAVOURITES_INDEX;
            }
            selectItem(selectItem);
        }


        movieAdapter = new MovieAdapter(this, this);

        moviesRecyclerView.setAdapter(movieAdapter);


        /** Gives user the opportunity to swipe out movies from the list. Should be enabled only for favourites TODO */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                long id = (long) viewHolder.itemView.getTag();

                removeFavouriteMovie(id);

                //update the list
                movieAdapter.swapCursor(null);
            }

        }).attachToRecyclerView(moviesRecyclerView);


        boolean isConnected = NetworkUtils.workingConnection(this);
        LoaderManager loaderManager = getLoaderManager();
        // if cursor is saved use it
        if (isConnected) {
            loaderManager.initLoader(MOVIE_LOADER_ID, null, this);
            NetworkUtils.initializeQueryMain(this);

            // showLoading();
        }
        if (!isConnected) {
            loaderManager.initLoader(MOVIE_LOADER_ID, null, this);
            showEmptyView();

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String mPrefOrderBby = userPreferences.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        outState.putString("userPref", mPrefOrderBby);


    }

    /**
     * Borrowed from http://stackoverflow.com/questions/33575731/gridlayoutmanager-how-to-auto-fit-columns
     * Calculates the the span count for the gridlayout according to screen size/density
     */
    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);
        return noOfColumns;
    }

    void removeFavouriteMovie(long id) {
        ContentResolver resolver = getContentResolver();
        Uri uri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id);

        //  resolver.delete(uri, null, null);
        ContentValues newValue = new ContentValues();
        newValue.put(MovieContract.MovieEntry.COLUMN_MOVIE_BY_FAVOURITE, 0);
        resolver.update(uri, newValue, null, null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onPostCreate");
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    void onPrefChange(int prefValue) {
        userPreferences.edit().putString(getString(R.string.settings_order_by_key),
                getString(prefValue)).apply();

        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);

        moviesRecyclerView.smoothScrollToPosition(0);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, "onOPTIONS");
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.i(LOG_TAG, "onOPTIONS  truee");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "CREATE LOADER");
        showLoading();
        String mPrefOrderBby = userPreferences.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        String selection = null;
        /** Checking the users preferred sort order and making a selection statement accordingly*/
        if (mPrefOrderBby.equals(getString(R.string.settings_order_by_popularity_value))) {
            selection = MovieContract.MovieEntry.COLUMN_MOVIE_BY_POPULARITY + " == 1";
        } else if (mPrefOrderBby.equals(getString(R.string.settings_order_by_rating_value))) {
            selection = MovieContract.MovieEntry.COLUMN_MOVIE_BY_RATING + " == 1";
        } else if (mPrefOrderBby.equals(getString(R.string.settings_order_by_favourite_value))) {
            selection = MovieContract.MovieEntry.COLUMN_MOVIE_BY_FAVOURITE + " == 1";
        }
        /** Sort movies by rating (regardless of which movie list user prefers)*/
        String sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_RATING + " DESC";

        Uri moviesQuery = MovieContract.MovieEntry.CONTENT_URI;

        return new CursorLoader(this,
                moviesQuery,
                MAIN_MOVIE_PROJECTION,
                selection,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        movieAdapter.swapCursor(cursor);
        //progressBar.setVisibility(View.INVISIBLE);
        Log.i(LOG_TAG, "onLoadFinished");
        Log.i(LOG_TAG, "cursor count " + cursor.getCount());
        Log.i(LOG_TAG, "adapter count " + movieAdapter.getItemCount());

        if (cursor.getCount() > 0) {
            showMoviesView();

            // NetworkUtils.downloadPosters(this, cursor);
        }
        if (cursor.getCount() == 0) {
            showEmptyView();
        }
        // if a new projection occurred, scroll to top
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;

    }

    void showLoading() {
        Log.i(LOG_TAG, "showLoading");
        progressBar.setVisibility(View.VISIBLE);
        moviesRecyclerView.setVisibility(View.INVISIBLE);

        emptyView.setVisibility(View.INVISIBLE);
    }

    void showMoviesView() {
        Log.i(LOG_TAG, "showMoviesView");
        progressBar.setVisibility(View.INVISIBLE);
        moviesRecyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.INVISIBLE);
    }

    void showEmptyView() {
        progressBar.setVisibility(View.INVISIBLE);
        moviesRecyclerView.setVisibility(View.INVISIBLE);
        emptyView.setVisibility(View.VISIBLE);
        String emptyText;
        if (!NetworkUtils.workingConnection(this)) {
            emptyText = getString(R.string.empty_view_no_connection);

        } else if (userPreferences.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default))
                .equals(getString(R.string.settings_order_by_favourite_value))) {
            emptyText = getString(R.string.empty_view_text);
        } else emptyText = getString(R.string.loading_text);

        emptyView.setText(emptyText);
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "onResume");
        super.onResume();
        getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieAdapter.swapCursor(null);

        Log.i(LOG_TAG, "onLoadRes");
    }


    @Override
    public void onClick(int movieId) {
        Intent toMovieDetails = new Intent(MainActivity.this, MovieDetailsActivity.class);
        Uri clickedMovieUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, movieId);

        /** Fetch and parse additional data only if we have a internet connection
         **/
        boolean isConnected = NetworkUtils.workingConnection(this);
        if (isConnected) {
            MovieExtraData trailersReviews = NetworkUtils.initializeQueryExtraDetails(this, movieId);
            toMovieDetails.putExtra("trailersReviews", trailersReviews);

        }

        toMovieDetails.setData(clickedMovieUri);
        toMovieDetails.putExtra("movieId", movieId);

        startActivity(toMovieDetails);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        /**
         * Swaps list of movies to appear in the recyclerView according to users chosen preference.
         * A call to initializeQueryMain is done to check if we need to download the preferred list or not
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i(LOG_TAG, "onItem CLick");


            switch (position) {
                case MOVIELIST_POPULAR_INDEX:
                    onPrefChange(R.string.settings_order_by_popularity_value);
                    Log.i(LOG_TAG, "initialize Q MAIN");
                    if (NetworkUtils.workingConnection(MainActivity.this)) {
                        NetworkUtils.initializeQueryMain(MainActivity.this);
                    } else {
                        showEmptyView();
                    }
                    //showLoading();
                    break;
                case MOVIELIST_RATED_INDEX:
                    onPrefChange(R.string.settings_order_by_rating_value);
                    Log.i(LOG_TAG, "initialize Q MAIN");
                    if (NetworkUtils.workingConnection(MainActivity.this)) {
                        NetworkUtils.initializeQueryMain(MainActivity.this);
                    } else {
                        showEmptyView();
                    }
                    //showLoading();
                    break;
                case MOVIELIST_FAVOURITES_INDEX:
                    onPrefChange(R.string.settings_order_by_favourite_value);
                    break;
            }
            selectItem(position);
        }
    }


    private void selectItem(int position) {
        Log.i(LOG_TAG, "selectItem");

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(movieListTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }
}

package com.roberts.adrian.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.roberts.adrian.popularmovies.data.MovieContract;
import com.roberts.adrian.popularmovies.databinding.ActivityMovieDetailsBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        TrailerAdapter.TrailerAdapterOnClickHandler,
        ReviewAdapter.ReviewAdapterOnClickHandler {

    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();


    @BindView(R.id.recyclerview_trailers)
    RecyclerView mTrailersRecyclerView;
    @BindView(R.id.recyclerview_reviews)
    RecyclerView mReviewsRecyclerView;
    @BindView(R.id.save_button)
    ImageView saveButton;
    @BindView(R.id.tv_TrailerLabel)
    TextView trailersLabelTextView;
    @BindView(R.id.tv_ReviewLabel)
    TextView reviewsLabelTextView;
    MovieExtraData mExtraData;
    @BindView(R.id.sv_movie_details)
    ScrollView scrollView;
    @BindView(R.id.imageView_detailsHeader)
    ImageView detailsImageHeader;

    TrailerAdapter mTrailerAdapter;
    ReviewAdapter mReviewAdapter;
    ArrayList<MovieTrailer> mTrailers;
    ArrayList<MovieReview> mReviews;
    private Uri mUri;
    private boolean isFavourite = false;
    private ActivityMovieDetailsBinding movieDetailsBinding;


    public static final String[] MOVIE_DETAILS_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_MOVIE_ALTERNATIVE_TITLE,
            MovieContract.MovieEntry.COLUMN_MOVIE_POSTER,
            MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS,
            MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME,
            MovieContract.MovieEntry.COLUMN_MOVIE_RATING,
            MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_MOVIE_BY_FAVOURITE,
            MovieContract.MovieEntry.COLUMN_STORED_MOVIE_POSTER
    };
    public static final int INDEX_ORIGINAL_TITLE = 0;
    public static final int INDEX_ALTERNATIVE_TITLE = 1;
    public static final int INDEX_POSTER = 2;
    public static final int INDEX_SYNOPSIS = 3;
    public static final int INDEX_RUNTIME = 4;
    public static final int INDEX_RATING = 5;
    public static final int INDEX_RELEASE_DATE = 6;
    public static final int INDEX_RELEASE_IS_FAVOURITE = 7;
    public static final int INDEX_STORED_POSTER = 8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        movieDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        ButterKnife.bind(this);
        scrollView.smoothScrollTo(View.SCROLL_INDICATOR_START, View.SCROLL_INDICATOR_START);

        LinearLayoutManager trailersLayoutMngr = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mTrailersRecyclerView.setLayoutManager(trailersLayoutMngr);
        mTrailersRecyclerView.setHasFixedSize(true);

        LinearLayoutManager reviewsLayoutMngr = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mReviewsRecyclerView.setLayoutManager(reviewsLayoutMngr);
        mReviewsRecyclerView.setHasFixedSize(true);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mTrailersRecyclerView.getContext(),
                trailersLayoutMngr.getOrientation());

        mTrailersRecyclerView.addItemDecoration(dividerItemDecoration);
        mReviewsRecyclerView.addItemDecoration(dividerItemDecoration);


        final int EXTRA_DATA_LOADER = 1349;
        mUri = getIntent().getData();

        if (getIntent().hasExtra("trailersReviews")) {
            mExtraData = getIntent().getParcelableExtra("trailersReviews");

            mTrailers = mExtraData.getMovieTrailers();
            mReviews = mExtraData.getMovieReviews();

            mTrailerAdapter = new TrailerAdapter(this, this, mTrailers);
            movieDetailsBinding.recyclerviewTrailers.setAdapter(mTrailerAdapter);

            mReviewAdapter = new ReviewAdapter(this, this, mReviews);
            movieDetailsBinding.recyclerviewReviews.setAdapter(mReviewAdapter);
        }

        int trailersQuantity = 0;
        if (mTrailers != null) trailersQuantity = mTrailers.size();
        trailersLabelTextView.setText(
                trailersQuantity == 0 ? R.string.trailer_label_zero :
                        trailersQuantity == 1 ? R.string.trailer_label_one : R.string.trailer_label_many);

        int reviewsQuantity = 0;
        if (mReviews != null) reviewsQuantity = mReviews.size();
        reviewsLabelTextView.setText(reviewsQuantity == 0 ? R.string.review_label_zero :
                reviewsQuantity == 1 ? R.string.review_label_one : R.string.review_label_many);

        getSupportLoaderManager().initLoader(EXTRA_DATA_LOADER, null, this);


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mUri, MOVIE_DETAILS_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        String orgTitle = data.getString(INDEX_ORIGINAL_TITLE);
        String altTitle = data.getString(INDEX_ALTERNATIVE_TITLE);
        movieDetailsBinding.detailsPrimary.tvMovieDetailsTitle.setText(orgTitle);
        if (orgTitle.length() > 18
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            movieDetailsBinding.detailsPrimary.tvMovieDetailsTitle.setTextSize(getResources().getDimension(R.dimen.small_text));
        boolean hasAltTitle = !(altTitle.equals(orgTitle));
        if (hasAltTitle) {
            movieDetailsBinding.detailsPrimary.tvMovieDetailsTitleEN.setText(String.format("(%s)", altTitle));
        } else movieDetailsBinding.detailsPrimary.tvMovieDetailsTitleEN.setVisibility(View.GONE);


        /**
         * Originally meant for favourite movies only, at this point we load images from blob if there are any,
         * meaning the movie was previously a favourite. This way deleting a favourite while offline won't add a blank image */
        boolean loadAsFavourite = false;
        Log.i(LOG_TAG, "stored as fav = " + (data.getInt(INDEX_RELEASE_IS_FAVOURITE) == 1));
        Log.i(LOG_TAG, "hasBlob = " + (data.getBlob(INDEX_STORED_POSTER) != null));
        if (data.getBlob(INDEX_STORED_POSTER) != null) { // data.getInt(INDEX_RELEASE_IS_FAVOURITE) == 1
            loadAsFavourite = true;
        }

        if (loadAsFavourite) {
            byte[] posterBarray = data.getBlob(INDEX_STORED_POSTER);
            Bitmap posterImage = BitmapFactory.decodeByteArray(posterBarray, 0, posterBarray.length);

            movieDetailsBinding.detailsPrimary.imageViewDetailsHeader.setImageBitmap(posterImage);
            movieDetailsBinding.detailsPrimary.imageViewDetailsHeader.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            // Getting the poster URL and loading it into the imageview using Picasso
            String backposterUrl = data.getString(INDEX_POSTER);
            Picasso.with(this)
                    .load(backposterUrl)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_place_holder)
                    .error(R.drawable.ic_no_wifi)
                    .into(movieDetailsBinding.detailsPrimary.imageViewDetailsHeader);


        }

        String synopsis = data.getString(INDEX_SYNOPSIS);
        movieDetailsBinding.detailsPrimary.tvMovieDetailsOverview.setText(synopsis);

        int runtime = data.getInt(INDEX_RUNTIME);
        movieDetailsBinding.detailsPrimary.tvMovieDetailsRuntime.setText(getString(R.string.runtime_minutes, runtime));

        double rating = data.getDouble(INDEX_RATING);
        String numOfRatingStars = String.valueOf(rating / 2);
        movieDetailsBinding.detailsPrimary.rbMovieDetailsRating.setMax(5);
        movieDetailsBinding.detailsPrimary.rbMovieDetailsRating.setStepSize(0.01f);
        movieDetailsBinding.detailsPrimary.rbMovieDetailsRating.setRating(Float.parseFloat(numOfRatingStars));

        String release = data.getString(INDEX_RELEASE_DATE);
        movieDetailsBinding.detailsPrimary.tvMovieDetailsReleaseDate.setText(release.substring(0, 4));

        if (data.getInt(INDEX_RELEASE_IS_FAVOURITE) == 1) {
            isFavourite = true;
            saveButton.setImageResource(R.drawable.ic_lremove_favourite_v2);
            saveButton.setAlpha(0.4f);
        } else {
            saveButton.setImageResource(R.drawable.ic_add_favourite_v2);
            isFavourite = false;
            saveButton.setAlpha(1f);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Mark/unmark movie as a favourite
     */
    public void saveAsFavourite(View view) {
        final int IS_FAVOURITE = 1;
        final int IS_NOT_FAVOURITE = 0;
        ContentResolver contentResolver = getContentResolver();
        ContentValues newValues = new ContentValues();
        if (!isFavourite) {
            newValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_BY_FAVOURITE, IS_FAVOURITE);
            isFavourite = true;
        } else {
            newValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_BY_FAVOURITE, IS_NOT_FAVOURITE);
            isFavourite = false;
        }
        contentResolver.update(mUri, newValues, null, null);
        contentResolver.notifyChange(mUri, null);
    }


    @Override
    public void onClick(String url) {
        Uri trailer = Uri.parse(url);
        Intent externalContent = new Intent(Intent.ACTION_VIEW, trailer);

        if (externalContent.resolveActivity(getPackageManager()) != null)
            startActivity(externalContent);
    }
}
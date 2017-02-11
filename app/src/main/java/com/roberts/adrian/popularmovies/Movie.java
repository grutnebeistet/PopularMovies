package com.roberts.adrian.popularmovies;

/**
 * Created by Adrian on 07/02/2017.
 */

public class Movie {
    private String mMovieTitle;
    private String mMovieTitleOriginal;
    private String mMovieOverview;
    private String mReleaseDate;
    private String mThumbnailUrl;
    private String mBackPoster;
    private double mUserRating;
    private int mRuntime;

    public Movie(String title, String orgTitle, String overview, String releaseDate,
                 String thumbnail, String backPoster, double rating, int runtime) {
        mMovieTitle = title;
        mMovieTitleOriginal = orgTitle;
        mMovieOverview = overview;
        mReleaseDate = releaseDate;
        mThumbnailUrl = thumbnail;
        mBackPoster = backPoster;
        mUserRating = rating;
        mRuntime = runtime;
    }

    public String getMovieTitleOriginal() {
        return mMovieTitleOriginal;
    }

    public int getRuntime() {
        return mRuntime;
    }

    public String getBackPoster() {
        return mBackPoster;
    }

    public double getUserRating() {
        return mUserRating;
    }

    public String getMovieOverview() {
        return mMovieOverview;
    }

    public String getMovieThumbnail() {
        return mThumbnailUrl;
    }

    public String getMovieTitle() {
        return mMovieTitle;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }
}

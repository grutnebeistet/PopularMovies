package com.roberts.adrian.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Adrian on 07/02/2017.
 */

public class Movie implements Parcelable {
    private int mMovieId;
    private String mMovieTitle;
    private String mMovieTitleOriginal;
    private String mMovieOverview;
    private String mReleaseDate;
    private String mThumbnailUrl;
    private String mBackPoster;
    private double mUserRating;
    private int mRuntime;
    private String mTrailer;
    private String mReview;

    public Movie(int id, String title, String orgTitle, String overview, String releaseDate,
                 String thumbnail, String backPoster, double rating, int runtime, String trailer, String review) {
        mMovieId = id;
        mMovieTitle = title;
        mMovieTitleOriginal = orgTitle;
        mMovieOverview = overview;
        mReleaseDate = releaseDate;
        mThumbnailUrl = thumbnail;
        mBackPoster = backPoster;
        mUserRating = rating;
        mRuntime = runtime;
        mTrailer = trailer;
        mReview = review;
    }

    @Override
    public void writeToParcel(Parcel movie, int i) {
        movie.writeInt(mMovieId);
        movie.writeString(mMovieTitle);
        movie.writeString(mMovieTitleOriginal);
        movie.writeString(mMovieOverview);
        movie.writeString(mReleaseDate);
        movie.writeString(mThumbnailUrl);
        movie.writeString(mBackPoster);
        movie.writeDouble(mUserRating);
        movie.writeInt(mRuntime);
        movie.writeString(mTrailer);
        movie.writeString(mReview);

    }

    public Movie(Parcel in) {
        mMovieId = in.readInt();
        mMovieTitle = in.readString();
        mMovieTitleOriginal = in.readString();
        mMovieOverview = in.readString();
        mReleaseDate = in.readString();
        mThumbnailUrl = in.readString();
        mBackPoster = in.readString();
        mUserRating = in.readDouble();
        mRuntime = in.readInt();
        mTrailer = in.readString();
        mReview = in.readString();

    }

        public static final Parcelable.Creator<Movie> CREATOR
                = new Parcelable.Creator<Movie>() {
            public Movie createFromParcel(Parcel in) {
                return new Movie(in);
            }

            public Movie[] newArray(int size) {
                return new Movie[size];
            }
        };

    public void setRuntime(int runtime) {
        mRuntime = runtime;
    }

    public String getmReview() {
        return mReview;
    }

    public String getmTrailer() {
        return mTrailer;
    }

    public int getmMovieId() {
        return mMovieId;
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

    @Override
    public int describeContents() {
        return 0;
    }

}

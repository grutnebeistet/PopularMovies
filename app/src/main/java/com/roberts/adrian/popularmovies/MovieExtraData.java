package com.roberts.adrian.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Adrian on 13/03/2017.
 */

public class MovieExtraData implements Parcelable {

    private ArrayList<MovieTrailer> movieTrailers;
    private ArrayList<MovieReview> movieReviews;

    public MovieExtraData(ArrayList<MovieTrailer> trailers, ArrayList<MovieReview> reviews) {
        movieTrailers = trailers;
        movieReviews = reviews;


    }

    public MovieExtraData(Parcel in) {
        movieTrailers = in.readArrayList(MovieTrailer.class.getClassLoader());
        movieReviews = in.readArrayList(MovieReview.class.getClassLoader());

    }

    public static final Parcelable.Creator<MovieExtraData> CREATOR
            = new Parcelable.Creator<MovieExtraData>() {
        public MovieExtraData createFromParcel(Parcel in) {
            return new MovieExtraData(in);
        }

        public MovieExtraData[] newArray(int size) {
            return new MovieExtraData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel movieData, int i) {
        movieData.writeList(movieTrailers);
        movieData.writeList(movieReviews);
    }

    public ArrayList<MovieReview> getMovieReviews() {
        return movieReviews;
    }

    public ArrayList<MovieTrailer> getMovieTrailers() {
        return movieTrailers;
    }

}

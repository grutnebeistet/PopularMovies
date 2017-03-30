package com.roberts.adrian.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Adrian on 15/03/2017.
 */

public class MovieReview implements Parcelable {
    private String mAuthor;
    private String mContent;
    private String mUrl;

    public MovieReview(String author, String content, String url) {
        mAuthor = author;
        mContent = content;
        mUrl = url;
    }

    public MovieReview(Parcel in) {
        mAuthor = in.readString();
        mContent = in.readString();
        mUrl = in.readString();

    }

    public static final Parcelable.Creator<MovieReview> CREATOR
            = new Parcelable.Creator<MovieReview>() {
        public MovieReview createFromParcel(Parcel in) {
            return new MovieReview(in);
        }

        public MovieReview[] newArray(int size) {
            return new MovieReview[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mAuthor);
        parcel.writeString(mContent);
        parcel.writeString(mUrl);
    }

    public String getmContent() {
        return mContent;
    }

    public String getmUrl() {
        return mUrl;
    }

    public String getmAuthor() {
        return mAuthor;
    }
}

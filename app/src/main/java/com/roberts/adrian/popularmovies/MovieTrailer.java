package com.roberts.adrian.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Adrian on 15/03/2017.
 */

public class MovieTrailer implements Parcelable {
    private String mVideoTitle;
    private String mVideoKey;
    private int mVideoSize;

    public MovieTrailer(String title, String key, int size) {
        mVideoTitle = title;
        mVideoKey = key;
        mVideoSize = size;
    }

    public MovieTrailer(Parcel in) {
        mVideoTitle = in.readString();
        mVideoKey = in.readString();
        mVideoSize = in.readInt();
    }

    public static final Parcelable.Creator<MovieTrailer> CREATOR
            = new Parcelable.Creator<MovieTrailer>() {
        public MovieTrailer createFromParcel(Parcel in) {
            return new MovieTrailer(in);
        }

        public MovieTrailer[] newArray(int size) {
            return new MovieTrailer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mVideoTitle);
        parcel.writeString(mVideoKey);
        parcel.writeInt(mVideoSize);
    }

    public String getmVideoTitle() {
        return mVideoTitle;
    }

    public String getmVideoKey() {
        return mVideoKey;
    }

    public int getmVideoSize() {
        return mVideoSize;
    }
}

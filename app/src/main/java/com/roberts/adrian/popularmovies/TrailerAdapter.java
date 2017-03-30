package com.roberts.adrian.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Adrian on 15/03/2017.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private final Context mContext;

    private final TrailerAdapterOnClickHandler mClickHandler;

    private final static String YOUTUBE_BASE = "https://www.youtube.com/watch?v=";

    /**
     * The interface that receives onClick messages.
     */
    public interface TrailerAdapterOnClickHandler {
        void onClick(String url);
    }

    private ArrayList<MovieTrailer> mTrailers;

    public TrailerAdapter(Context context, TrailerAdapterOnClickHandler clickHandler, ArrayList<MovieTrailer> trailers) { //, List<MovieTrailer> trailers
        mContext = context;
        mClickHandler = clickHandler;
        mTrailers = trailers;

    }


    @Override
    public void onBindViewHolder(TrailerAdapterViewHolder holder, int position) {

        holder.trailerNameTextView.setText(mTrailers.get(position).getmVideoTitle());
        holder.trailerSizeTextView.setText(mContext.getString(R.string.trailer_size, mTrailers.get(position).getmVideoSize()));
    }

    @Override
    public TrailerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.trailer_layout, parent, false);

        view.setFocusable(true);

        return new TrailerAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mTrailers.size();
    }

    class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView trailerNameTextView;

        TextView trailerSizeTextView;

        TrailerAdapterViewHolder(View view) {
            super(view);
            trailerNameTextView = (TextView) view.findViewById(R.id.trailerNameTextView);
            trailerSizeTextView = (TextView) view.findViewById(R.id.trailerSizeTextView);
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int adapterPos = getAdapterPosition();
            String videoKey = mTrailers.get(adapterPos).getmVideoKey();
            String trailerUrl = YOUTUBE_BASE + videoKey;
            mClickHandler.onClick(trailerUrl);

        }
    }
}

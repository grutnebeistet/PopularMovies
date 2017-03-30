package com.roberts.adrian.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Adrian on 20/03/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private final Context mContext;

    private final ReviewAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface ReviewAdapterOnClickHandler {
        void onClick(String url);
    }

    private ArrayList<MovieReview> mReviews;

    public ReviewAdapter(Context context, ReviewAdapterOnClickHandler clickHandler, ArrayList<MovieReview> reviews) {
        mContext = context;
        mClickHandler = clickHandler;
        mReviews = reviews;

    }


    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder holder, int position) {
        String by_author = mContext.getString(R.string.review_author_label, mReviews.get(position).getmAuthor());
        holder.reviewAuthorTextView.setText(by_author);
        holder.reviewContentTextView.setText(mReviews.get(position).getmContent());

    }

    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.review_layout, parent, false);

        view.setFocusable(true);

        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }


    class ReviewAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView reviewAuthorTextView;

        TextView reviewContentTextView;

        ReviewAdapterViewHolder(View view) {
            super(view);
            reviewAuthorTextView = (TextView) view.findViewById(R.id.textViewReviewAuthor);
            reviewContentTextView = (TextView) view.findViewById(R.id.textViewReviewContent);
            view.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
            int adapterPos = getAdapterPosition();
            String url = mReviews.get(adapterPos).getmUrl();
            mClickHandler.onClick(url);

        }
    }
}

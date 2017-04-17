package com.roberts.adrian.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import static com.roberts.adrian.popularmovies.R.drawable.ic_place_holder;

/**
 * Created by Adrian on 07/02/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> implements View.OnLongClickListener, View.OnTouchListener {
    private final static String LOG_TAG = MovieAdapter.class.getSimpleName();

    private Context mContext;
    private boolean isLongPressed = false;

    final private MovieAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(int movieId);
    }

    private Cursor mCursor;

    public MovieAdapter(Context context, MovieAdapterOnClickHandler clickHandler) {
        Log.i(LOG_TAG, "MovieAdapter");
        mClickHandler = clickHandler;
        mContext = context;


    }

    @Override
    public boolean onLongClick(View view) {
        view.animate().alpha(0.4f);
        isLongPressed = true;

        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        view.onTouchEvent(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            if (isLongPressed) {
                view.setAlpha(1f);
                isLongPressed = false;

            }
        }
        return false;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
        Log.i(LOG_TAG, "Grid TAG: " + view.getTag());
        view.setFocusable(true);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        if (mCursor.isClosed()) return;

        mCursor.moveToPosition(position);

        long id = mCursor.getInt(MainActivity.INDEX_MOVIE_ID);

        boolean hasStoredThumbnail = mCursor.getBlob(MainActivity.INDEX_STORED_THUMBNAIL) != null;

        // Loading image thumbnail from DB
        if (hasStoredThumbnail) {
            byte[] thumbnailBarray = mCursor.getBlob(MainActivity.INDEX_STORED_THUMBNAIL);
            Bitmap thumbnail = BitmapFactory.decodeByteArray(thumbnailBarray, 0, thumbnailBarray.length);
            holder.movieThumbnailImageView.setImageBitmap(thumbnail);
           // Log.i(LOG_TAG, "BITMAP HEIGHGT: " + thumbnail.getHeight() + "\n WIDTH: " + thumbnail.getWidth());
           // Log.i(LOG_TAG, "Using stored image");
        }
        // Using picasso to load and insert thumbnail with url stored in the database
        else {
          //  Log.i(LOG_TAG, "no has blob, picassoing");
            String thumbnailUrl = mCursor.getString(MainActivity.INDEX_MOVIE_THUMBNAIL);
            Picasso.with(mContext)
                    .load(thumbnailUrl).fit()
                    .placeholder(ic_place_holder)
                    .error(ic_place_holder)
                    .into(holder.movieThumbnailImageView);
        }
        /*final int padding = mContext.getResources().getDimensionPixelSize(R.dimen.padding_small);
        holder.itemView.setPadding(padding, padding, padding, padding);
        //.addItemDecoration(new SpacesItemDecoration(padding));*/
        int w = holder.movieThumbnailImageView.getWidth();
        int h = holder.movieThumbnailImageView.getHeight();


        holder.itemView.setTag(id);

        if (mCursor.getInt(MainActivity.INDEX_IS_FAVOURITE) == 1) {
            holder.itemView.setOnTouchListener(this);
            holder.itemView.setOnLongClickListener(this);
        }

    }


    class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView movieThumbnailImageView;

        MovieAdapterViewHolder(View view) {
            super(view);

            movieThumbnailImageView = (ImageView) view.findViewById(R.id.grid_item_image_view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mCursor.isClosed()) return;
            mCursor.moveToPosition(getAdapterPosition());
            mClickHandler.onClick(mCursor.getInt(MainActivity.INDEX_MOVIE_ID));
        }

    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        Log.i(LOG_TAG, "swapCursor");
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}

package com.roberts.adrian.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Adrian on 20/03/2017.
 */

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImagesAdapterViewHolder> {

    private final Context mContext;

    private final ImagesAdapterOnClickHandler mClickHandler;
    private static final String BASE_URL = "https://image.tmdb.org/t/p/original";

    /**
     * The interface that receives onClick messages.
     */
    public interface ImagesAdapterOnClickHandler {
        void onClickImage(String url);
    }

    private ArrayList<String> mImages;

    public ImagesAdapter(Context context,
                         ImagesAdapterOnClickHandler clickHandler,
                         ArrayList<String> images) {
        mContext = context;
        mClickHandler = clickHandler;
        mImages = images;

    }


    @Override
    public void onBindViewHolder(final ImagesAdapterViewHolder holder, int position) {

        Log.i("IMGAdapter", mImages.get(position));
        String imUrl = BASE_URL + mImages.get(position);
        Picasso
                .with(mContext).load(imUrl)
                .fit()
                .centerCrop()
                .error(R.drawable.ic_no_wifi)
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("Test picasso", "image loaded");
                        PhotoViewAttacher mAttacher = new PhotoViewAttacher(holder.imageView);
                       // mAttacher.setZoomable(true);
                        mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {
                            @Override
                            public void onPhotoTap(ImageView view, float x, float y) {
                                Log.i("IMG AD", "Tap img");
                                holder.imageView.setScaleX(x * 2f);
                                holder.imageView.setScaleY(y * 2f);
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        Log.d("Test picasso", "image error");
                    }
                });

    }

    @Override
    public ImagesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.image_list_item, parent, false);

        view.setFocusable(true);

        return new ImagesAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }


    class ImagesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.image_list_imageview)
        com.github.chrisbanes.photoview.PhotoView imageView;

        ImagesAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {

        }
    }
}

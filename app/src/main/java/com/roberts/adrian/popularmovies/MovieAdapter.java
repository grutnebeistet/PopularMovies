package com.roberts.adrian.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Adrian on 07/02/2017.
 *
 */

public class MovieAdapter extends ArrayAdapter<Movie> {
    private static final String BASE_POSTER_PATH_URL = "http://image.tmdb.org/t/p/";
    private static final String DEFAULT_POSTER_SIZE = "w185";
    private static final String DEFAULT_BACKPOSTER_SIZE = "w500";
    private Context mContext;
    public MovieAdapter(Context context, List<Movie> movies){
        super(context,0,movies);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View gridViewItem = convertView;
        if(gridViewItem == null){
            gridViewItem = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }

        final Movie currentMovie = getItem(position);

        final ImageView moviePosterImageView = (ImageView)gridViewItem.findViewById(R.id.grid_item_image_view);

        //StringBuilder posterUrl = new StringBuilder();
       // posterUrl.append(BASE_POSTER_PATH_URL).append(DEFAULT_POSTER_SIZE).append(currentMovie.getMovieThumbnail());
        String posterUrl = BASE_POSTER_PATH_URL.concat(DEFAULT_POSTER_SIZE).concat(currentMovie.getMovieThumbnail());
        Picasso.with(mContext)
                .load(posterUrl).fit()
                .into(moviePosterImageView);
        gridViewItem.setPadding(8,8,8,8);
        gridViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = currentMovie.getMovieTitle();
                String orgTitle = currentMovie.getMovieTitleOriginal();
                String overview = currentMovie.getMovieOverview();
                String releaseDate = currentMovie.getReleaseDate();
                double rating = currentMovie.getUserRating();
                int runtime = currentMovie.getRuntime();
                String backPosterUrl = BASE_POSTER_PATH_URL.concat(DEFAULT_BACKPOSTER_SIZE).concat(currentMovie.getBackPoster());

                Intent openMovieDetails = new Intent(mContext, MovieDetailsActivity.class);
                Bundle movieDetails = new Bundle();
                movieDetails.putString("title", title);
                movieDetails.putString("orgTitle", orgTitle);
                movieDetails.putString("releaseDate", releaseDate);
                movieDetails.putString("overview", overview);
                movieDetails.putString("backposter", backPosterUrl.toString());
                movieDetails.putDouble("rating", rating);
                movieDetails.putInt("runtime", runtime);

                openMovieDetails.putExtras(movieDetails);
                mContext.startActivity(openMovieDetails);
            }
        });
        return gridViewItem;
    }
}

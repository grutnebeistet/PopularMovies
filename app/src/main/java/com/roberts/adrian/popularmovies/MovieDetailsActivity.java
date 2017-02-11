package com.roberts.adrian.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MovieDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private TextView originalTitleTextView;
    private TextView alternativeTitleTextView;
    private TextView overviewTextView;
    private TextView releaseDateTextView;
    private ImageView backposterImageView;
    private TextView runtimeTextView;
    private RatingBar ratingBar;

    private Target target;
    private LinearLayout ll;
    private ImageView headerImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Bundle bundle = getIntent().getExtras();
        ll = (LinearLayout) findViewById(R.id.ll_detail_header);
        headerImageView = (ImageView) findViewById(R.id.view_head);

        //TODO more images http://api.themoviedb.org/3/movie/[movie_id]/images?api_key=0f4813b07b3a6aa68a3d632f384f7faa
        originalTitleTextView = (TextView) findViewById(R.id.tv_movieDetails_title);
        alternativeTitleTextView = (TextView) findViewById(R.id.tv_movieDetails_title_EN);
        overviewTextView = (TextView) findViewById(R.id.tv_movieDetails_overview);
        releaseDateTextView = (TextView) findViewById(R.id.tv_movieDetails_releaseDate);
        //backposterImageView = (ImageView) findViewById(R.id.iw_movieDetails_poster);
        runtimeTextView = (TextView) findViewById(R.id.tv_movieDetails_runtime);
        ratingBar = (RatingBar) findViewById(R.id.rb_movieDetails_rating);


        String orgTitle = bundle.getString("orgTitle");
        String title = bundle.getString("title");
        String overview = bundle.getString("overview");
        String releaseDate = bundle.getString("releaseDate");
        String backposterUrl = bundle.getString("backposter");
        double rating = bundle.getDouble("rating");
        int runtime = bundle.getInt("runtime");

        originalTitleTextView.setText(orgTitle);
        if (orgTitle.length() > 18) originalTitleTextView.setTextSize(26);
        boolean hasAltTitle = !(title.equals(orgTitle));
        if (hasAltTitle) alternativeTitleTextView.setText(String.format("(%s)", title));

        releaseDateTextView.setText(releaseDate.substring(0, 4));
        overviewTextView.setText(overview);
        runtimeTextView.setText(runtime + "min");

        //overviewTextView.append("\n" + String.valueOf(rating));

        String numOfRatingStars = String.valueOf(rating / 2);
        ratingBar.setMax(5);
        ratingBar.setStepSize(0.01f);
        ratingBar.setRating(Float.parseFloat(numOfRatingStars));

        Picasso.with(this).load(backposterUrl).fit().centerCrop().into(headerImageView);

    }
}




/*        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable d = new BitmapDrawable(getResources(),bitmap);
               // headerImageView.setBackground(d);
                Log.i(LOG_TAG, "Bitmap size: " + bitmap.getHeight() + "X" + bitmap.getWidth());
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        Picasso.with(this)
                .load(backposterUrl)
                .into(target);*/
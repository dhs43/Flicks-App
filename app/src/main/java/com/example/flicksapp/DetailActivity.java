package com.example.flicksapp;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.flicksapp.models.Movie;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class DetailActivity extends YouTubeBaseActivity {

    public static final String YOUTUBE_API_KEY = "AIzaSyBLtptPoS0YMJZle8UkkRIjO3xG-ncf_Vk";
    private static final String TRAILERS_URL = "https://api.themoviedb.org/3/movie/%d/videos?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed&language=en-US";


    TextView tvTitle;
    TextView tvOverview;
    RatingBar ratingBar;
    TextView tvRatingNum;
    YouTubePlayerView youTubePlayerView;

    Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvTitle = findViewById(R.id.tvTitle);
        tvOverview = findViewById(R.id.tvOverview);
        //so the overview can scroll for long descriptions
        tvOverview.setMovementMethod(new ScrollingMovementMethod());
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingNum = findViewById(R.id.tvRatingNum);
        youTubePlayerView = findViewById(R.id.player);

        movie = Parcels.unwrap(getIntent().getParcelableExtra("movie"));
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());
        ratingBar.setRating((float) movie.getRating());
        tvRatingNum.setText(Float.toString((float) movie.getRating()));


        AsyncHttpClient client = new AsyncHttpClient();
        client.get(String.format(TRAILERS_URL, movie.getId()), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    JSONArray results = response.getJSONArray("results");
                    if (results.length() == 0) {
                        return;
                    }
                    JSONObject movieTrailer = results.getJSONObject(0);
                    //check if the trailer is from youtube
                    String site = movieTrailer.getString("site");
                    if (site.equals("YouTube")) {
                        String youtubeKey = movieTrailer.getString("key");
                        initializeYoutube(youtubeKey, movie.getRating());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    private void initializeYoutube(final String youtubeKey, final double rating) {
        youTubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                Log.d("ytPlayer","player init success");
                if (rating >= 7){
                    youTubePlayer.loadVideo(youtubeKey);
                }else{
                    youTubePlayer.cueVideo(youtubeKey);
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.d("ytPlayer", "player init failed");
            }
        });
    }
}
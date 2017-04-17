package com.roberts.adrian.popularmovies.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by Adrian on 16/04/2017.
 */

public class FirebaseJobService extends JobService {
    private AsyncTask<Void, Void, Void> mFetchMoviesTask;


    @Override
    public boolean onStartJob(final JobParameters job) {
        Log.i("JOBSERVICE, ", "starting job...");
        mFetchMoviesTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Context context = getApplicationContext();
                NetworkUtils.updateMovieLists(context);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(job, false);
            }
        };
        mFetchMoviesTask.execute();
        return true;
    }


    @Override
    public boolean onStopJob(JobParameters job) {
        if (mFetchMoviesTask != null) {
            mFetchMoviesTask.cancel(true);
        }
        return true;
    }
}

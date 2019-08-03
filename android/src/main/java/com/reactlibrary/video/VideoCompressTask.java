package com.reactlibrary.video;

import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class VideoCompressTask extends AsyncTask<String, Float, Boolean> {

    private CompressListener mListener;
    private int mQuality;
    private long mStartTime;
    private long mEndTime;
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    public VideoCompressTask(CompressListener listener, String quality, long startTime, long endTime) {
        int finalQuality = MediaController.COMPRESS_QUALITY_LOW;

        if (quality.equals("high")) {
            finalQuality = MediaController.COMPRESS_QUALITY_HIGH;
        } else if (quality.equals("medium")) {
            finalQuality = MediaController.COMPRESS_QUALITY_MEDIUM;
        }

        mListener = listener;
        mQuality = finalQuality;
        mStartTime = startTime;
        mEndTime = endTime;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null) {
            mListener.onStart();
        }
    }

    @Override
    protected Boolean doInBackground(String... paths) {
        boolean result = MediaController.getInstance().convertVideo(isCancelled, paths[0], paths[1], mQuality, mStartTime, mEndTime, new MediaController.CompressProgressListener() {
            @Override
            public void onProgress(float percent) {
                publishProgress(percent);
            }
        });
        Log.i("VideoCompressTask", "reached end of bkg task");
        return result;
    }

    @Override
    protected void onProgressUpdate(Float... percent) {
        super.onProgressUpdate(percent);
        if (mListener != null) {
            mListener.onProgress(percent[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (mListener != null) {
            if (result) {
                mListener.onSuccess();
            } else {
                mListener.onFail();
            }
        }
    }

//    @Override
//    protected void onCancelled() {
//        super.onCancelled();
//        if(mListener != null) {
//            mListener.onCancel();
//        }
//    }

    public void cancel() {
        isCancelled.set(true);

        /* Interrupt the compression task */
        super.cancel(true);
    }
}

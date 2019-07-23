package com.reactlibrary.video;

import android.os.AsyncTask;

public class VideoCompress {
    private static final String TAG = VideoCompress.class.getSimpleName();
    private static VideoCompressTask compression;

    public static VideoCompressTask compressVideo(String srcPath, String destPath, String quality, long startTime, long endTime, CompressListener listener) {
        int finalQuality = MediaController.COMPRESS_QUALITY_LOW;

        if (quality.equals("high")) {
            finalQuality = MediaController.COMPRESS_QUALITY_HIGH;
        } else if (quality.equals("medium")) {
            finalQuality = MediaController.COMPRESS_QUALITY_MEDIUM;
        }

        compression = new VideoCompressTask(listener, finalQuality, startTime, endTime);
        compression.execute(srcPath, destPath);
        return compression;
    }

    public static void cancelCompress() {
        compression.cancel(true);
    }

    public static class VideoCompressTask extends AsyncTask<String, Float, Boolean> {
        private CompressListener mListener;
        private int mQuality;
        private long mStartTime;
        private long mEndTime;

        public VideoCompressTask(CompressListener listener, int quality, long startTime, long endTime) {
            mListener = listener;
            mQuality = quality;
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
            return MediaController.getInstance().convertVideo(paths[0], paths[1], mQuality, mStartTime, mEndTime, new MediaController.CompressProgressListener() {
                @Override
                public void onProgress(float percent) {
                    publishProgress(percent);
                }
            });
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
    }

    public interface CompressListener {
        void onStart();
        void onSuccess();
        void onFail();
        void onProgress(float percent);
    }
}

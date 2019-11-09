package com.reactlibrary.video;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import com.reactlibrary.video.MediaController.CompressProgressListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.ypresto.qtfaststart.QtFastStart;

public class VideoCompressionTask extends AsyncTask<Void, Float, Boolean> {

  private static final String TAG = VideoCompressionTask.class.getSimpleName();

  private final String srcPath;
  private final String intermediatePath;
  private final String destPath;
  private final VideoCompressionListener listener;
  private final String quality;
  private final long startTime;
  private final long endTime;
  private final AtomicBoolean isCancelled = new AtomicBoolean(false);

  public VideoCompressionTask(
      final String srcPath,
      final String destPath,
      final String quality,
      final long startTime,
      final long endTime,
      final VideoCompressionListener listener) {
    this.srcPath = srcPath;
    this.destPath = destPath;
    this.listener = listener;
    this.quality = quality;
    this.startTime = startTime;
    this.endTime = endTime;

    this.intermediatePath = destPath + ".tmp";
  }

  static int toMediaControllerQuality(final String quality) {
    if ("high".equals(quality)) {
      return MediaController.COMPRESS_QUALITY_HIGH;
    } else if ("medium".equals(quality)) {
      return MediaController.COMPRESS_QUALITY_MEDIUM;
    }
    return MediaController.COMPRESS_QUALITY_LOW;
  }

  private static boolean fastStart(File input, File output) {
    try {
      if (!output.exists()) {
        // if there is no output file we'll create one
        output.createNewFile();
      }
    } catch (IOException e) {
      Log.e(TAG, e.toString());
    }

    try {
      // Adds moov to your input
      return QtFastStart.fastStart(input, output);
    } catch (QtFastStart.MalformedFileException m) {
      Log.e(TAG, m.toString());
    } catch (QtFastStart.UnsupportedFileException q) {
      Log.e(TAG, q.toString());
    } catch (IOException i) {
      Log.e(TAG, i.toString());
    }
    return false;
  }

  private static boolean deleteFileIfExists(final String path) {
    try {
      final File file = new File(path);
      if (file.exists()) {
        Log.w(TAG, "deleting existing file");
        return file.delete();
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to delete destPath", e);
    }
    return false;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    if (listener != null) {
      listener.onStart();
    }
  }

  @Override
  protected Boolean doInBackground(Void... ignored) {
    cleanUp();

    final boolean compressionResult = MediaController.getInstance().convertVideo(
        srcPath,
        intermediatePath,
        toMediaControllerQuality(quality),
        startTime,
        endTime,
        getListener(),
        isCancelled);
    File intermediateFile = new File(intermediatePath);
    if (intermediateFile.exists()) {
      fastStart(intermediateFile, new File(destPath));
    }
    return compressionResult;
  }

  @NonNull
  private CompressProgressListener getListener() {
    return new CompressProgressListener() {
      @Override
      public void onProgress(float percent) {
        publishProgress(percent);
      }
    };
  }

  @Override
  protected void onProgressUpdate(Float... percent) {
    super.onProgressUpdate(percent);
    if (listener != null) {
      listener.onProgress(percent[0]);
    }
  }

  @Override
  protected void onPostExecute(Boolean result) {
    super.onPostExecute(result);
    if (listener != null) {
      if (result) {
        listener.onSuccess();
      } else {
        listener.onFail();
      }
    }
  }

  /**
   * Overloaded cancel method which interrupts ongoing task.
   */
  public void cancel() {
    Log.w(TAG, "Canceling video compression task.");
    isCancelled.set(true);

    /* Interrupt the compression task */
    super.cancel(true);
  }

  @Override
  protected void onCancelled() {
    super.onCancelled();
    cleanUp();
  }

  private void cleanUp() {
    deleteFileIfExists(destPath);
    deleteFileIfExists(intermediatePath);
  }

  public interface VideoCompressionListener {

    void onStart();

    void onSuccess();

    void onFail();

    void onProgress(float percent);
  }
}

package com.reactlibrary.video;

public interface CompressListener {
    void onStart();
    void onSuccess();
    void onFail();
    void onProgress(float percent);
    void onCancel();
}

package it.dhd.oxygencustomizer.aiplugin.interfaces;

import android.graphics.Bitmap;

public interface SegmenterResultListener {
    void onSegmentationResult(Bitmap subject);
    void onSegmentationError(Exception e);
}

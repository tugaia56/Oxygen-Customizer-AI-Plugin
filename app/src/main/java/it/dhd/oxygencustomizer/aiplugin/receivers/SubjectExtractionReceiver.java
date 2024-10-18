package it.dhd.oxygencustomizer.aiplugin.receivers;

import static android.os.FileUtils.copy;
import static it.dhd.oxygencustomizer.aiplugin.utils.Constants.ACTION_EXTRACT_FAILURE;
import static it.dhd.oxygencustomizer.aiplugin.utils.Constants.ACTION_EXTRACT_SUCCESS;
import static it.dhd.oxygencustomizer.aiplugin.utils.ImageUtils.fixImageOrientation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import it.dhd.oxygencustomizer.aiplugin.interfaces.SegmenterResultListener;
import it.dhd.oxygencustomizer.aiplugin.utils.BitmapSubjectSegmenter;
import it.dhd.oxygencustomizer.aiplugin.utils.SubjectSegmenter;

public class SubjectExtractionReceiver extends BroadcastReceiver {

    private String mSourcePath = null;
    private String mDestinationPath = null;
    private String mSenderPackage = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("SubjectExtractionReceiver", "Received intent");

        mSourcePath = intent.getStringExtra("sourcePath");
        mDestinationPath = intent.getStringExtra("destinationPath");
        mSenderPackage = intent.getPackage();

        Log.d("SubjectExtractionReceiver", "mSenderPackage: " + mSenderPackage);

        if (mSourcePath == null || mDestinationPath == null) {
            sendError(context, "Invalid source or destination path");
            return;
        }

        startRemove(context);

    }

    private void startRemove(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        File file = new File(mSourcePath);

        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = fixImageOrientation(fis);
            if (bitmap == null) {
                sendError(context, "Failed to decode bitmap");
                return;
            }
            Bitmap inputBitmap = bitmap.copy(bitmap.getConfig(), true);
            int aiMode = Integer.parseInt(prefs.getString("ai_mode", "0"));
            Log.d("SubjectExtractionReceiver", "AI Mode: " + aiMode);
            if (aiMode == 1) {
                Log.d("SubjectExtractionReceiver", "Using SubjectSegmenter " + Integer.parseInt(prefs.getString("ai_model", "0")));
                new SubjectSegmenter(context, Integer.parseInt(prefs.getString("ai_model", "0")), new SegmenterResultListener() {
                    @Override
                    public void onSegmentationResult(Bitmap result) {
                        try {
                            if (result.isRecycled()) {
                                Log.e("SubjectExtractionReceiver", "onSuccess: BitmapSubjectSegmenter: Recycled bitmap");
                                return;
                            }
                            File tempFile = File.createTempFile("lswt", ".png");

                            Log.d("SubjectExtractionReceiver", "extractSubject: " + tempFile.getAbsolutePath() + " -> " + mDestinationPath);

                            FileOutputStream outputStream = new FileOutputStream(tempFile);
                            result.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                            outputStream.close();
                            result.recycle();

                            try {
                                copy(new FileInputStream(tempFile), new FileOutputStream(mDestinationPath));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Log.d("SubjectExtractionReceiver", "onSuccess: BitmapSubjectSegmenter " + mDestinationPath);

                            Intent successIntent = new Intent();
                            successIntent.setAction(ACTION_EXTRACT_SUCCESS);
                            successIntent.setPackage(mSenderPackage);
                            context.sendBroadcast(successIntent);
                        } catch (Throwable t) {
                            Log.e("SubjectExtractionReceiver", "onSuccess: BitmapSubjectSegmenter", t);
                        }
                    }

                    @Override
                    public void onSegmentationError(Exception e) {
                        sendError(context, e.getMessage());
                    }
                }).removeBackground(inputBitmap);
            } else {
                new BitmapSubjectSegmenter(context).segmentSubjectFromJava(inputBitmap, new SegmenterResultListener() {
                    @Override
                    public void onSegmentationResult(@Nullable Bitmap result) {
                        try {
                            File tempFile = File.createTempFile("lswt", ".png");

                            Log.d("SubjectExtractionReceiver", "extractSubject: " + tempFile.getAbsolutePath() + " -> " + mDestinationPath);

                            FileOutputStream outputStream = new FileOutputStream(tempFile);
                            result.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                            outputStream.close();
                            result.recycle();

                            try {
                                copy(new FileInputStream(tempFile), new FileOutputStream(mDestinationPath));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Intent successIntent = new Intent();
                            successIntent.setAction(ACTION_EXTRACT_SUCCESS);
                            successIntent.setPackage(mSenderPackage);
                            context.sendBroadcast(successIntent);
                        } catch (Throwable t) {
                            Log.e("SubjectExtractionReceiver", "onSuccess: BitmapSubjectSegmenter", t);
                        }
                    }

                    @Override
                    public void onSegmentationError(@NonNull Exception e) {
                        Intent failureIntent = new Intent();
                        failureIntent.setAction(ACTION_EXTRACT_FAILURE);
                        failureIntent.setPackage(mSenderPackage);
                        Log.e("SubjectExtractionReceiver", "Failed to extract subject", e);
                        failureIntent.putExtra("error", e.getMessage());
                        context.sendBroadcast(failureIntent);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("SubjectExtractionReceiver", "Failed to decode bitmap", e);
        }
    }

    private void sendError(Context context, String errorMessage) {
        Intent failureIntent = new Intent();
        failureIntent.setAction(ACTION_EXTRACT_FAILURE);
        failureIntent.setPackage(mSenderPackage);
        Log.e("SubjectExtractionReceiver", errorMessage);
        failureIntent.putExtra("error", errorMessage);
        context.sendBroadcast(failureIntent);
    }

}

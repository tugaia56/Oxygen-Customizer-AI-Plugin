package it.dhd.oxygencustomizer.aiplugin.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import java.io.IOException;

import ai.onnxruntime.OrtException;
import it.dhd.oxygencustomizer.aiplugin.interfaces.SegmenterResultListener;
import it.dhd.oxygencustomizer.aiplugin.sessions.BaseSession;
import it.dhd.oxygencustomizer.aiplugin.sessions.SessionFactory;

public class SubjectSegmenter {

    private Context mContext;
    private int mSessionId;
    private SegmenterResultListener mListener;
    private BaseSession mSession;


    public SubjectSegmenter(Context context, int sessionId, SegmenterResultListener listener) throws IOException, OrtException {
        Log.d("SubjectSegmenter", "Initializing SubjectSegmenter");
        this.mContext = context;
        this.mSessionId = sessionId;
        this.mListener = listener;
        mSession = SessionFactory.createSession(mContext, mSessionId);
    }

    public Bitmap remove(Bitmap image) throws OrtException {

        Log.d("SubjectSegmenter", "start predict");
        Bitmap mask = mSession.predict(image);
        Log.d("SubjectSegmenter", "end predict");

        Bitmap cutout;
        Log.d("SubjectSegmenter", "start naiveCutout");
        cutout = naiveCutout(image, mask);
        Log.d("SubjectSegmenter", "end naiveCutout");
        mSession.close();
        Log.d("SubjectSegmenter", "Returning result");
        return cutout;
    }

    private Bitmap naiveCutout(Bitmap img, Bitmap mask) {
        // Simple cutout using the mask
        Bitmap cutout = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutout);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(img, 0, 0, null);
        canvas.drawBitmap(mask, 0, 0, paint);
        return cutout;
    }

    public void removeBackground(Bitmap inputImage) {

        if (inputImage == null) {
            Log.e("SubjectSegmenter", "Input image is null");
            mListener.onSegmentationError(new Exception("Input image is null"));
            return;
        }

        try {
            mListener.onSegmentationResult(remove(inputImage));
        } catch (Exception e) {
            mListener.onSegmentationError(e);
        }
    }


}

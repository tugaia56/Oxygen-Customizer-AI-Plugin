package it.dhd.oxygencustomizer.aiplugin.sessions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class IsnetAnimeSession extends BaseSession {

    public IsnetAnimeSession(Context context) throws OrtException, IOException {
        super(context, "isnet_anime.onnx");
    }

    @Override
    public Bitmap predict(Bitmap image) throws OrtException {
        Map<String, OnnxTensor> inputs = normalize(image, new float[]{0.485f, 0.456f, 0.406f}, new float[]{1f, 1f, 1f}, 1024, 1024);

        try (OrtSession.Result result = session.run(inputs)) {
            float[][][][] ortOuts = (float[][][][]) result.get(0).getValue();
            float[][] pred = ortOuts[0][0];

            float ma = Float.NEGATIVE_INFINITY;
            float mi = Float.POSITIVE_INFINITY;

            for (float[] row : pred) {
                for (float value : row) {
                    if (value > ma) ma = value;
                    if (value < mi) mi = value;
                }
            }

            for (int y = 0; y < pred.length; y++) {
                for (int x = 0; x < pred[y].length; x++) {
                    pred[y][x] = (pred[y][x] - mi) / (ma - mi);
                }
            }

            Bitmap mask = Bitmap.createBitmap(pred[0].length, pred.length, Bitmap.Config.ARGB_8888);
            for (int y = 0; y < pred.length; y++) {
                for (int x = 0; x < pred[y].length; x++) {
                    int alpha = (int) (pred[y][x] * 255);
                    mask.setPixel(x, y, Color.argb(alpha, 255, 255, 255));
                }
            }

            mask = Bitmap.createScaledBitmap(mask, image.getWidth(), image.getHeight(), true);


            Log.d("IsnetAnimeSession", "Mask transformed");
            return mask;
        }
    }
}

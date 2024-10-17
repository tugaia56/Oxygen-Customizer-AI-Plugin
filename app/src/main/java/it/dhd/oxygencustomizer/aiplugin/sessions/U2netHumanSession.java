package it.dhd.oxygencustomizer.aiplugin.sessions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import it.dhd.oxygencustomizer.aiplugin.R;

public class U2netHumanSession extends BaseSession {

    // Constructor to initialize the session with the U2Net human segmentation model
    public U2netHumanSession(Context context) throws OrtException, IOException {
        super(context, "u2net_human_seg.onnx");
    }

    // The predict method to run the segmentation and return the mask
    @Override
    public Bitmap predict(Bitmap image) throws OrtException {
        // Normalize the input image before prediction
        Map<String, OnnxTensor> inputs = normalize(image, new float[]{0.485f, 0.456f, 0.406f}, new float[]{0.229f, 0.224f, 0.225f}, 320, 320);

        // Run the ONNX model for prediction
        try (OrtSession.Result result = session.run(inputs)) {
            // Extract the output and reshape the results
            float[][][][] ortOuts = (float[][][][]) result.get(0).getValue();
            float[][] pred = ortOuts[0][0];

            // Find max and min values in the output
            float max = Float.MIN_VALUE;
            float min = Float.MAX_VALUE;
            for (float[] row : pred) {
                for (float val : row) {
                    if (val > max) max = val;
                    if (val < min) min = val;
                }
            }

            // Normalize the output between 0 and 1
            for (int i = 0; i < pred.length; i++) {
                for (int j = 0; j < pred[0].length; j++) {
                    pred[i][j] = (pred[i][j] - min) / (max - min);
                }
            }

            // Convert the result into a mask (Bitmap)
            Bitmap mask = convertArrayToBitmap(pred, pred.length, pred[0].length);

            Log.d("U2netHumanSession", "Mask created");
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            matrix.postScale(-1, 1);

            Bitmap transformedMask = Bitmap.createBitmap(mask, 0, 0, mask.getWidth(), mask.getHeight(), matrix, true);
            Bitmap finalMask = Bitmap.createScaledBitmap(transformedMask, image.getWidth(), image.getHeight(), true);
            Log.d("U2netHumanSession", "Mask transformed");
            return finalMask;
        }
    }

    // Helper method to convert the normalized array into a Bitmap
    private Bitmap convertArrayToBitmap(float[][] pred, int originalWidth, int originalHeight) {
        Log.d("U2netHumanSession", "Converting array to bitmap");
        int width = pred.length;
        int height = pred[0].length;

        Bitmap mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = (int) (pred[x][y] * 255);
                mask.setPixel(x, y, Color.argb(alpha, 0, 0, 0));
            }
        }

        Log.d("U2netHumanSession", "Resizing mask to original image size");
        return Bitmap.createScaledBitmap(mask, originalWidth, originalHeight, true);
    }
}

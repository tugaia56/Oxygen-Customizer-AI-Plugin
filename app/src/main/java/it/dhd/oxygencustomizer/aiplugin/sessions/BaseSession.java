package it.dhd.oxygencustomizer.aiplugin.sessions;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import ai.onnxruntime.*;
import it.dhd.oxygencustomizer.aiplugin.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseSession {

    public OrtSession session;
    public OrtEnvironment env;

    /**
     * This is a base class for managing a session with a machine learning model
     * @param context The application context
     * @param fileName The name of the model file
     * @throws OrtException If an error occurs during session creation
     * @throws IOException If an error occurs while reading the model file
     */
    public BaseSession(Context context, String fileName) throws OrtException, IOException {
        env = OrtEnvironment.getEnvironment();

        byte[] modelBytes = null;
        File modelFile = new File(context.getFilesDir() + "/" + fileName);
        if (modelFile.exists()) {
            modelBytes = new byte[(int) modelFile.length()];
            try (InputStream is = context.openFileInput(fileName)) {
                is.read(modelBytes);
            }
        } else {
            Log.e("BaseSession", "Model file not found: " + fileName);
            modelBytes = new byte[0];
        }
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        int numThreads = Runtime.getRuntime().availableProcessors();
        Log.d("BaseSession", "Number of threads: " + numThreads);
        options.setInterOpNumThreads(numThreads);
        options.setIntraOpNumThreads(numThreads);

        this.session = env.createSession(modelBytes, options);
    }

    /**
     * Normalize the input image using the provided mean and standard deviation values
     * @param image The input image as a Bitmap
     * @param mean The mean values for each channel
     * @param std The standard deviation values for each channel
     * @param width The width of the input image
     * @param height The height of the input image
     * @return A map containing the input name and tensor
     * @throws OrtException If an error occurs during normalization
     */
    public Map<String, OnnxTensor> normalize(Bitmap image, float[] mean, float[] std, int width, int height) throws OrtException {
        Bitmap resizedImg = Bitmap.createScaledBitmap(image, width, height, true);

        int[] pixels = new int[width * height];
        resizedImg.getPixels(pixels, 0, width, 0, 0, width, height);

        float[][][] tmpImg = new float[3][height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = pixels[y * width + x];

                float r = ((rgb >> 16) & 0xFF) / 255.0f;
                float g = ((rgb >> 8) & 0xFF) / 255.0f;
                float b = (rgb & 0xFF) / 255.0f;

                tmpImg[0][y][x] = (r - mean[0]) / std[0];
                tmpImg[1][y][x] = (g - mean[1]) / std[1];
                tmpImg[2][y][x] = (b - mean[2]) / std[2];
            }
        }

        // Convert to OnnxTensor
        OnnxTensor tensor = OnnxTensor.createTensor(env, new float[][][][]{tmpImg});

        // Return the input name and tensor as a map
        String inputName = session.getInputNames().iterator().next();
        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put(inputName, tensor);

        return inputs;
    }

    // Abstract method to define model-specific prediction behavior
    public abstract Bitmap predict(Bitmap image) throws OrtException;

    public void close() {
        try {
            session.close();
            env.close();
        } catch (OrtException e) {
            Log.e("BaseSession", "Error closing session: " + e.getMessage());
        }
    }
}
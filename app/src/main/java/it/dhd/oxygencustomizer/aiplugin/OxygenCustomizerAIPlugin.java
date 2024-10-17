package it.dhd.oxygencustomizer.aiplugin;

import android.app.Application;
import android.content.Context;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.google.android.material.color.DynamicColors;

import java.lang.ref.WeakReference;

public class OxygenCustomizerAIPlugin extends Application {

    private static OxygenCustomizerAIPlugin instance;
    private static WeakReference<Context> contextReference;

    public void onCreate() {
        super.onCreate();
        instance = this;
        contextReference = new WeakReference<>(getApplicationContext());
        DynamicColors.applyToActivitiesIfAvailable(this);
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);
    }

    public static Context getAppContext() {
        if (contextReference == null || contextReference.get() == null) {
            contextReference = new WeakReference<>(OxygenCustomizerAIPlugin.getInstance().getApplicationContext());
        }
        return contextReference.get();
    }

    private static OxygenCustomizerAIPlugin getInstance() {
        if (instance == null) {
            instance = new OxygenCustomizerAIPlugin();
        }
        return instance;
    }
}

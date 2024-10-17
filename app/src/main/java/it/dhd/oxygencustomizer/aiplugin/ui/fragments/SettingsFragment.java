package it.dhd.oxygencustomizer.aiplugin.ui.fragments;

import static it.dhd.oxygencustomizer.aiplugin.utils.Constants.APP_UPDATES;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import javax.security.auth.callback.Callback;

import it.dhd.oxygencustomizer.aiplugin.BuildConfig;
import it.dhd.oxygencustomizer.aiplugin.R;
import it.dhd.oxygencustomizer.aiplugin.ui.activities.SettingsActivity;
import it.dhd.oxygencustomizer.aiplugin.ui.preferences.OplusJumpPreference;
import it.dhd.oxygencustomizer.aiplugin.ui.preferences.OplusListPreference;
import it.dhd.oxygencustomizer.aiplugin.ui.preferences.OplusMenuPreference;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences mPreferences;
    private OplusJumpPreference mUpdateChecker;

    @Override
    public void setDivider(Drawable divider) {
        super.setDivider(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void setDividerHeight(int height) {
        super.setDividerHeight(0);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        float dip = 12f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        final RecyclerView rv = getListView();
        rv.setPadding(0, 0, 0, (int) px);
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.ai_prefs, rootKey);

        mPreferences = getPreferenceManager().getSharedPreferences();

        OplusListPreference mModelPreference = findPreference("ai_model");
        mModelPreference.setVisible(mPreferences.getString("ai_mode", "0").equals("1"));

        OplusMenuPreference mAiModePreference = findPreference("ai_mode");
        mAiModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            mModelPreference.setVisible(newValue.equals("1"));
            return true;
        });

        Preference mModuleDownloader = findPreference("module_downloader");
        mModuleDownloader.setOnPreferenceClickListener(preference -> {
            ((SettingsActivity) requireActivity()).replaceFragment(new ModuleDownloader());
            return true;
        });

        Preference test = findPreference("test");
        test.setOnPreferenceClickListener(preference -> {
            ((SettingsActivity) requireActivity()).replaceFragment(new TestFragment());
            return true;
        });

        mUpdateChecker = findPreference("check_updates");
        Handler handler = new Handler(Looper.getMainLooper());
        mUpdateChecker.setOnPreferenceClickListener(preference -> {
            new updateChecker(result -> {
                handler.post(() -> {
                    if (result.get("versionCode").equals(-1)) {
                        mUpdateChecker.setSummary("Connection Error");
                    } else {
                        if (result.get("versionCode").equals(BuildConfig.VERSION_CODE)) {
                            mUpdateChecker.setSummary("You are up to date");
                        } else {
                            mUpdateChecker.setSummary("Latest version: " + result.get("version"));
                            mUpdateChecker.setOnPreferenceClickListener(preference1 -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) result.get("apkUrl")));
                                startActivity(intent);
                                return true;
                            });
                        }
                    }
                });
            }).start();
            return true;
        });
    }

    public interface TaskDoneCallback extends Callback {
        void onFinished(HashMap<String, Object> result);
    }

    public static class updateChecker extends Thread {
        private final TaskDoneCallback mCallback;

        public updateChecker(TaskDoneCallback callback) {
            mCallback = callback;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(200);
                URL updateData = new URL(APP_UPDATES);
                InputStream s = updateData.openStream();
                InputStreamReader r = new InputStreamReader(s);
                JsonReader jsonReader = new JsonReader(r);

                HashMap<String, Object> versionInfo = new HashMap<>();
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    String name = jsonReader.nextName();
                    switch (name) {
                        case "versionCode":
                            versionInfo.put(name, jsonReader.nextInt());
                            break;
                        case "apkUrl":
                        case "version":
                        case "changelog":
                        default:
                            versionInfo.put(name, jsonReader.nextString());
                            break;
                    }
                }
                mCallback.onFinished(versionInfo);
            } catch (Exception e) {
                HashMap<String, Object> error = new HashMap<>();
                error.put("version", "Connection Error");
                error.put("versionCode", -1);
                mCallback.onFinished(error);
                Log.e("updateChecker", "Failed to check for updates", e);
            }
        }
    }

}

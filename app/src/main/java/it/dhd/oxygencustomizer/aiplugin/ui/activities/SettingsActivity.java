package it.dhd.oxygencustomizer.aiplugin.ui.activities;

import static it.dhd.oxygencustomizer.aiplugin.utils.Constants.UPDATE_CHANNEL_ID;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.security.auth.callback.Callback;

import it.dhd.oxygencustomizer.aiplugin.R;
import it.dhd.oxygencustomizer.aiplugin.databinding.ActivityMainBinding;
import it.dhd.oxygencustomizer.aiplugin.databinding.FragmentModelDownloadBinding;
import it.dhd.oxygencustomizer.aiplugin.ui.adapters.ModelAdapter;
import it.dhd.oxygencustomizer.aiplugin.ui.fragments.ModuleDownloader;
import it.dhd.oxygencustomizer.aiplugin.ui.fragments.SettingsFragment;
import it.dhd.oxygencustomizer.aiplugin.ui.fragments.TestFragment;
import it.dhd.oxygencustomizer.aiplugin.ui.models.AIModel;
import it.dhd.oxygencustomizer.aiplugin.ui.preferences.OplusListPreference;
import it.dhd.oxygencustomizer.aiplugin.ui.preferences.OplusMenuPreference;

public class SettingsActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private FragmentManager fragmentManager;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DynamicColors.applyToActivityIfAvailable(this);
        fragmentManager = getSupportFragmentManager();
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        replaceFragment(new SettingsFragment(), false);

        setSupportActionBar(mBinding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name);
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return false;
    }

    public void replaceFragment(Fragment newFragment) {
        replaceFragment(newFragment, true);
    }

    private void replaceFragment(Fragment newFragment, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);
        fragmentTransaction.replace(R.id.frame_layout, newFragment, newFragment.getTag());
        if (addToBackStack) fragmentTransaction.addToBackStack(newFragment.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.update_channel_name);
        String description = getString(R.string.update_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(UPDATE_CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }




}


package it.dhd.oxygencustomizer.aiplugin.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.color.DynamicColors;

import it.dhd.oxygencustomizer.aiplugin.R;
import it.dhd.oxygencustomizer.aiplugin.databinding.ActivityMainBinding;
import it.dhd.oxygencustomizer.aiplugin.ui.fragments.SettingsFragment;

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

}


package it.dhd.oxygencustomizer.aiplugin.ui.fragments;

import static android.app.Activity.RESULT_OK;

import static it.dhd.oxygencustomizer.aiplugin.utils.AppUtils.getRealPath;
import static it.dhd.oxygencustomizer.aiplugin.utils.AppUtils.launchFilePicker;
import static it.dhd.oxygencustomizer.aiplugin.utils.Constants.ACTION_EXTRACT_FAILURE;
import static it.dhd.oxygencustomizer.aiplugin.utils.Constants.ACTION_EXTRACT_SUCCESS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;

import it.dhd.oxygencustomizer.aiplugin.databinding.FragmentTestAiBinding;
import it.dhd.oxygencustomizer.aiplugin.interfaces.SegmenterResultListener;
import it.dhd.oxygencustomizer.aiplugin.utils.AppUtils;
import it.dhd.oxygencustomizer.aiplugin.utils.BitmapSubjectSegmenter;
import it.dhd.oxygencustomizer.aiplugin.utils.SubjectSegmenter;

public class TestFragment extends Fragment {

    private FragmentTestAiBinding binding;
    private Bitmap mBitmap;
    private SharedPreferences mPrefs;
    private boolean isDoing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTestAiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private final SegmenterResultListener listener = new SegmenterResultListener() {
        @Override
        public void onSegmentationResult(Bitmap subject) {
            binding.imageView.setImageBitmap(subject);
            isDoing = false;
        }

        @Override
        public void onSegmentationError(Exception e) {
            isDoing = false;
            Log.e("TestFragment", "Error: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        if (!AppUtils.hasStoragePermission()) {
            AppUtils.requestStoragePermission(requireActivity());
        }

        binding.fab.setOnClickListener(v -> {
            launchFilePicker(pickImageIntent, "image/*");
        });

        binding.aiFab.setOnClickListener(v -> {
            if (mBitmap == null) {
                Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isDoing) {
                Toast.makeText(requireContext(), "Already processing", Toast.LENGTH_SHORT).show();
                return;
            }
            doExtract();
        });

    }

    private void doExtract() {
        isDoing = true;
        int aiMode = Integer.parseInt(mPrefs.getString("ai_mode", "0"));
        if (aiMode == 1) {
            try {
                new SubjectSegmenter(requireContext(), Integer.parseInt(mPrefs.getString("ai_model", "0")), listener).removeBackground(mBitmap);
            } catch (Exception e) {
                listener.onSegmentationError(e);
            }
        } else {
            new BitmapSubjectSegmenter(requireContext()).segmentSubjectFromJava(mBitmap, listener);
        }
    }

    ActivityResultLauncher<Intent> pickImageIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    ImageDecoder.Source source = ImageDecoder.createSource(new File(path));
                    try {
                        Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                        mBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        binding.imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

}

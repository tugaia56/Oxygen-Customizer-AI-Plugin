package it.dhd.oxygencustomizer.aiplugin.ui.fragments;

import static it.dhd.oxygencustomizer.aiplugin.utils.Constants.AI_MODELS;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

import it.dhd.oxygencustomizer.aiplugin.databinding.FragmentModelDownloadBinding;
import it.dhd.oxygencustomizer.aiplugin.ui.adapters.ModelAdapter;
import it.dhd.oxygencustomizer.aiplugin.ui.models.AIModel;
import it.dhd.oxygencustomizer.aiplugin.utils.NetworkUtils;

public class ModuleDownloader extends Fragment {
    private FragmentModelDownloadBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentModelDownloadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Handler handler = new Handler(Looper.getMainLooper());
        new updateChecker(result -> handler.post(() -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerView.setAdapter(new ModelAdapter(getContext(), result));
        })).start();
    }

    public interface TaskDoneCallback extends Callback {
        void onFinished(List<AIModel> result);
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
                String response = NetworkUtils.downloadUrlMemoryAsString(AI_MODELS);
                Log.d("Model", "Reading json " + response);

                List<AIModel> mAiModels = new ArrayList<>();
                JSONObject json = new JSONObject(response);
                JSONObject modelContainer = json.getJSONObject("models");
                JSONArray models = modelContainer.getJSONArray("model");
                for (int i = 0; i < models.length(); i++) {
                    String name = models.getJSONObject(i).getString("name");
                    String description = models.getJSONObject(i).getString("description");
                    String type = models.getJSONObject(i).getString("type");
                    String filename = models.getJSONObject(i).getString("filename");
                    String url = models.getJSONObject(i).getString("url");
                    mAiModels.add(new AIModel(name, description, type, filename, url));
                }
                mCallback.onFinished(mAiModels);
            } catch (Exception e) {
                mCallback.onFinished(new ArrayList<>());
                e.printStackTrace();
            }
        }

    }

}

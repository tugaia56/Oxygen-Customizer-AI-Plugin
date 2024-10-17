package it.dhd.oxygencustomizer.aiplugin.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;

import java.io.File;
import java.util.List;

import it.dhd.oxygencustomizer.aiplugin.databinding.ViewItemModelBinding;
import it.dhd.oxygencustomizer.aiplugin.ui.models.AIModel;

public class ModelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<AIModel> items;
    private final Context mContext;

    public ModelAdapter(Context context, List<AIModel> items) {
        this.mContext = context;
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewItemModelBinding bindingItem = ViewItemModelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(bindingItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AIModel model = items.get(position);
        ((ItemViewHolder) holder).bind(mContext, model);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ViewItemModelBinding binding;

        public ItemViewHolder(@NonNull ViewItemModelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Context context, AIModel model) {
            binding.modelName.setText(model.getName());
            binding.modelDescription.setText(model.getDescription());
            File modelFile = new File(context.getFilesDir(), model.getFilename());
            if (modelFile.exists()) {
                binding.modelDownload.setEnabled(false);
                binding.modelProgress.setVisibility(View.GONE);
            } else {
                binding.modelDownload.setEnabled(true);
                binding.modelDownload.setOnClickListener(v -> PRDownloader.download(
                                model.getUrl(),
                                binding.modelDownload.getContext().getFilesDir().getPath(),
                                model.getFilename())
                        .build()
                        .setOnProgressListener(progress -> {
                            int progressPercent = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                            binding.modelDownload.setText(progressPercent + "%");
                            binding.modelProgress.setProgress(progressPercent);
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                v.setEnabled(false);
                            }

                            @Override
                            public void onError(Error error) {

                            }
                        }));
            }
        }
    }

}

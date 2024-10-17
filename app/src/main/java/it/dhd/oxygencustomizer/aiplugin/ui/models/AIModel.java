package it.dhd.oxygencustomizer.aiplugin.ui.models;

import androidx.annotation.NonNull;

public class AIModel {

    private final String name;
    private final String description;
    private final String type;
    private final String filename;
    private final String url;

    public AIModel(String name, String description, String type, String filename, String url) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.filename = filename;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getFilename() {
        return filename;
    }

    public String getUrl() {
        return url;
    }

    @NonNull
    @Override
    public String toString() {
        return "Model{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", filename='" + filename + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}

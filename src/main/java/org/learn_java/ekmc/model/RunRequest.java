package org.learn_java.ekmc.model;

import java.util.ArrayList;
import java.util.List;

public class RunRequest {
    List<File> files = new ArrayList<>();
    String language = "java";
    String version = "15.0.2";

    public RunRequest(String content, Language language) {
        this.files.add(new File(content));
        this.language = language.getLanguage();
        this.version = language.getVersion();
    }

    public RunRequest() {
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}

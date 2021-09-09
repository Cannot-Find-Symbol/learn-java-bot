package org.learn_java.ekmc.model;

import java.util.List;

public class Language {
    private String language;
    private String version;
    private List<String> aliases;
    private String runtime;

    public Language() {
    }

    @Override
    public String toString() {
        return "Language{" +
                "language='" + language + '\'' +
                ", version='" + version + '\'' +
                ", aliases=" + aliases +
                ", runtime='" + runtime + '\'' +
                '}';
    }

    public String getLanguage() {
        return language;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }
}

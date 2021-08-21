package org.learn_java.bot.event.listeners.run_listener;

import java.util.List;

public class Language {
    private String language;
    private String version;
    private List<String> aliases;

    public Language() {
    }

    @Override
    public String toString() {
        return "Language{" +
                "language='" + language + '\'' +
                ", version='" + version + '\'' +
                ", aliases=" + aliases +
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
}

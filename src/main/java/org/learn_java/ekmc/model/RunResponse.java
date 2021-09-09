package org.learn_java.ekmc.model;

import org.learn_java.ekmc.model.Run;

public class RunResponse {
    String language;
    String version;
    Run run;

    public RunResponse() {
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

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    @Override
    public String toString() {
        return "RunResponse{" +
                "language='" + language + '\'' +
                ", version='" + version + '\'' +
                ", run=" + run +
                '}';
    }
}

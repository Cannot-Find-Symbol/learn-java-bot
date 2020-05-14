package org.learn_java.data.dto;

public class InfoDTO {

    private final String tagName;
    private final String message;

    public InfoDTO(String tagName, String message) {
        this.tagName = tagName;
        this.message = message;
    }

    public String getTagName() {
        return tagName;
    }

    public String getMessage() {
        return message;
    }
}

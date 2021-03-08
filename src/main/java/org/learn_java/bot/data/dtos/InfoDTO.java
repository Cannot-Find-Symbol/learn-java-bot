package org.learn_java.bot.data.dtos;

public class InfoDTO {
    private String topic;
    private String message;

    public InfoDTO() {
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package org.learn_java.bot.data.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Info {

    @Id
    private String topic;
    private String message;

    private String description;

    public Info() {
    }

    public Info(String topic, String message, String description) {
        this.topic = topic;
        this.message = message;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

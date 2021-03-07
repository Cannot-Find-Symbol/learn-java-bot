package org.learn_java.bot.data.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Info {

    @Id
    private String topic;
    private String message;

    public Info() {
    }

    public Info(String topic, String message) {
        this.topic = topic;
        this.message = message;
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

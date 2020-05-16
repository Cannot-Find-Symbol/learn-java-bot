package org.learn_java.data.entities;

import org.springframework.context.annotation.Primary;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
public class Info {

    @Id
    private String tagName;
    private String message;

    public Info() {

    }


    public Info(String tagName, String message) {
        this.tagName = tagName;
        this.message = message;
    }


    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

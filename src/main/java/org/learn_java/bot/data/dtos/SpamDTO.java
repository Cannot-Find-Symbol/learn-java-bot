package org.learn_java.bot.data.dtos;

public class SpamDTO {
    private Long id;
    private String message;

    public SpamDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

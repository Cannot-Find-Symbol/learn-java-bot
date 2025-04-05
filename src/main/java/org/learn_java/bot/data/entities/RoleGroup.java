package org.learn_java.bot.data.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class RoleGroup {
    Boolean isUnique;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String message;
    private Long guildId;
    private Long messageId;
    private Long channelId;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "group", cascade = CascadeType.ALL)
    private List<MemberRole> roles = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<MemberRole> getRoles() {
        return roles;
    }

    public void setRoles(List<MemberRole> roles) {
        this.roles = roles;
    }

    public Boolean getUnique() {
        return isUnique;
    }

    public void setUnique(Boolean unique) {
        isUnique = unique;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }
}

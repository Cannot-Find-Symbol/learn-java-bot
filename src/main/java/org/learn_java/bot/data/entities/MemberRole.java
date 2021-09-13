package org.learn_java.bot.data.entities;

import javax.persistence.*;

@Entity
public class MemberRole {
    @Id
    private Long id;

    private String description;

    private int ordinal;

    @ManyToOne
    RoleGroup group;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleGroup getGroup() {
        return group;
    }

    public void setGroup(RoleGroup group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int order) {
        this.ordinal = order;
    }
}

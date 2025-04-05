package org.learn_java.bot.data.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class MemberRole {
    @ManyToOne
    RoleGroup group;
    @Id
    private Long id;
    private String description;
    private int ordinal;

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

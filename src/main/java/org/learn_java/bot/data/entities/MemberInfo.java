package org.learn_java.bot.data.entities;


import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MemberInfo {
    @Id
    private Long id;

    private Integer totalThankCount;
    private Integer monthThankCount;

    public MemberInfo() {
    }

    public MemberInfo(Long id, Integer totalThankCount, Integer monthThankCount) {
        this.id = id;
        this.totalThankCount = totalThankCount;
        this.monthThankCount = monthThankCount;
    }

    @Override
    public String toString() {
        return "MemberInfo{" +
                "id=" + id +
                ", totalThankCount=" + totalThankCount +
                ", monthThankCount=" + monthThankCount +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTotalThankCount() {
        return totalThankCount;
    }

    public void setTotalThankCount(Integer totalThankCount) {
        this.totalThankCount = totalThankCount;
    }

    public Integer getMonthThankCount() {
        return monthThankCount;
    }

    public void setMonthThankCount(Integer monthThankCount) {
        this.monthThankCount = monthThankCount;
    }
}

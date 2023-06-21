package org.learn_java.bot.data.dtos;

import net.dv8tion.jda.api.entities.Member;

public class MemberInfoDTO {
    private Member member;
    private int thankCount;

    public MemberInfoDTO(Member member, int thankCount) {
        this.member = member;
        this.thankCount = thankCount;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public int getThankCount() {
        return thankCount;
    }

    public void setThankCount(int thankCount) {
        this.thankCount = thankCount;
    }
}

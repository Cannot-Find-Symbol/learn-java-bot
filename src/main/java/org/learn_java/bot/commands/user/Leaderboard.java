package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.learn_java.bot.data.entities.MemberInfo;
import org.learn_java.bot.service.MemberInfoService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Leaderboard implements SlashCommand {
    private final CommandData commandData;
    private final String name;
    private final MemberInfoService service;

    public Leaderboard(MemberInfoService service) {
        this.name = "leaderboard";
        this.commandData = new CommandData(name, "Show thanks leaderboard");
        this.service = service;
    }

    @Override
    public void executeSlash(SlashCommandEvent event) {
        event.deferReply(false).queue();
        List<MemberInfo> memberInfos = service.findTop10ForMonth();
        sendLeaderboard(event, memberInfos);
    }

    private void sendLeaderboard(SlashCommandEvent event, List<MemberInfo> memberInfos) {
        List<Long> top10Ids = memberInfos.stream().map(MemberInfo::getId).collect(Collectors.toList());
        event.getGuild().retrieveMembersByIds(top10Ids).onSuccess((members) -> {
            int maxNameLength = members.stream().map(Member::getEffectiveName).mapToInt(String::length).max().orElse(-1);
            StringBuilder sb = new StringBuilder();
            sb.append("Leaderboard (Total/Month)\n\n");
            int listNumber = 1;
            for (Member member : members) {
                MemberInfo stats = getThankCount(memberInfos, member.getIdLong());
                sb.append(buildDescription(member.getEffectiveName(), stats, listNumber++, maxNameLength)).append("\n");
            }

            event.getHook().sendMessage("```\n" + sb + "```").queue();
        });
    }

    private String buildDescription(String name, MemberInfo stats, int position, int maxLength) {
        return String.format("%d. %-" + maxLength + "s\t%d/%d", position, name, stats.getTotalThankCount(), stats.getMonthThankCount()).replaceAll("\s", "\\ ");
    }

    public MemberInfo getThankCount(List<MemberInfo> memberInfos, long id) {
        return memberInfos.stream().filter(member -> member.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}

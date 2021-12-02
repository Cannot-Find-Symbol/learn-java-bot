package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.commands.SlashCommand;
import org.learn_java.bot.commands.user.run.MemberInfoDTO;
import org.learn_java.bot.configuration.Config;
import org.learn_java.bot.data.entities.MemberInfo;
import org.learn_java.bot.service.MemberInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Leaderboard implements SlashCommand {
    private final CommandData commandData;
    private final String name;
    private final MemberInfoService service;
    private final String leaderboardChannelId;
    private final JDA jda;
    private final String guildId;

    public Leaderboard(MemberInfoService service,
                       @Value("${leaderboard.channelid}") String leaderboardChannelId, JDA jda, Config config) {
        this.name = "leaderboard";
        this.commandData = new CommandData(name, "Show thanks leaderboard");
        this.service = service;
        this.leaderboardChannelId = leaderboardChannelId;
        this.jda = jda;
        guildId = config.getGuildId();
    }

    @Override
    public void executeSlash(SlashCommandEvent event) {
        event.deferReply(false).queue();
        List<MemberInfo> memberInfos = service.findTop10ForMonth();
        sendLeaderboardViaSlash(event, memberInfos);
    }

    public void sendLeaderboardViaSlash(SlashCommandEvent event, List<MemberInfo> memberInfos) {
        List<Long> top10Ids = memberInfos.stream()
                .map(MemberInfo::getId)
                .collect(Collectors.toList());

        event.getGuild().retrieveMembersByIds(top10Ids).onSuccess((members) -> {

            // TODO this is a crappy bandaid because I don't have time to fix sorting.... fix it...
            List<MemberInfoDTO> memberInfoDTOS =  members.stream()
                    .map(member -> new MemberInfoDTO(member, service.getMonthThankCountByMemberId(member.getIdLong())))
                    .filter(dto -> dto.getThankCount() > 0)
                    .sorted(Comparator.comparing(MemberInfoDTO::getThankCount).reversed())
                    .collect(Collectors.toList());

            int maxNameLength = members.stream().map(Member::getEffectiveName).mapToInt(String::length).max().orElse(-1);
            StringBuilder sb = new StringBuilder();
            sb.append("Leaderboard (Total/Month)\n\n");
            int listNumber = 1;
            for (MemberInfoDTO member : memberInfoDTOS) {
                MemberInfo stats = getThankCount(memberInfos, member.getMember().getIdLong());
                sb.append(buildDescription(member.getMember().getEffectiveName(), stats, listNumber++, maxNameLength)).append("\n");
            }

            event.getHook().sendMessage("```\n" + sb + "```").queue();
        });
    }

    // TODO cleanup this duplicated code
    public void sendLeaderboardToChannelAndReset(TextChannel channel, List<MemberInfo> memberInfos) {
        List<Long> top10Ids = memberInfos.stream()
                .map(MemberInfo::getId)
                .collect(Collectors.toList());

        channel.getGuild().retrieveMembersByIds(top10Ids).onSuccess((members -> {
            // TODO this is a crappy bandaid because I don't have time to fix sorting.... fix it...
          List<MemberInfoDTO> memberInfoDTOS =  members.stream()
                    .map(member -> new MemberInfoDTO(member, service.getMonthThankCountByMemberId(member.getIdLong())))
                    .filter(dto -> dto.getThankCount() > 0)
                    .sorted(Comparator.comparing(MemberInfoDTO::getThankCount).reversed())
                    .collect(Collectors.toList());

            int maxNameLength = members.stream()
                    .map(Member::getEffectiveName)
                    .mapToInt(String::length)
                    .max()
                    .orElse(-1);

            String leaderboard = buildLeaderBoard(memberInfos, memberInfoDTOS, maxNameLength);

            channel.sendMessage("```\n" + leaderboard + "```").queue();
            service.resetForMonth();
        }));
    }

    private String buildLeaderBoard(List<MemberInfo> memberInfos, List<MemberInfoDTO> memberInfoDTOS, int maxNameLength) {
        StringBuilder sb = new StringBuilder();
        LocalDate today = LocalDate.now().minusMonths(1);
        String month = StringUtils.capitalize(today.getMonth().name().toLowerCase());
        sb.append("Leaderboard ").append(month).append(" totals (Total/Month)\n\n");
        int listNumber = 1;
        for (MemberInfoDTO member : memberInfoDTOS) {
            MemberInfo stats = getThankCount(memberInfos, member.getMember().getIdLong());
            sb.append(buildDescription(member.getMember().getEffectiveName(), stats, listNumber++, maxNameLength)).append("\n");
        }
        return sb.toString();
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

    @Scheduled(cron = "0 0 18 1 * * ")
    public void resetLeaderboard() {
        List<MemberInfo> memberInfos = service.findTop10ForMonth();
        TextChannel leaderboardChannel = jda.getGuildById(guildId).getTextChannelById(leaderboardChannelId);
        //TODO fix this method also handling reset, didn't want to deal with waiting
        sendLeaderboardToChannelAndReset(leaderboardChannel, memberInfos);
    }
}

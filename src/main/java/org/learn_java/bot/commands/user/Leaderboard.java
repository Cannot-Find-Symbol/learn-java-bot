package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.apache.bcel.classfile.Module;
import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.configuration.Config;
import org.learn_java.bot.data.dtos.MemberInfoDTO;
import org.learn_java.bot.data.entities.MemberInfo;
import org.learn_java.bot.service.MemberInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class Leaderboard extends Command {
    private final SlashCommandData commandData;
    private final MemberInfoService service;
    private final String leaderboardChannelId;
    private final JDA jda;
    private final String guildId;

    public Leaderboard(MemberInfoService service,
                       @Value("${leaderboard.channelid}") String leaderboardChannelId, JDA jda, Config config) {
        super("leaderboard", CommandType.ANY);
        this.service = service;
        this.leaderboardChannelId = leaderboardChannelId;
        this.jda = jda;
        guildId = config.getGuildId();

        SubcommandData month = new SubcommandData("month", "view leaderboard for current month");
        SubcommandData allTime = new SubcommandData("alltime", "view leaderboard for current month");
        this.commandData = Commands.slash(getName(), "Show thanks leaderboard")
                .addSubcommands(month, allTime)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        event.deferReply(false).queue();
        TopTenCommand topTenCommand = TopTenCommand.value(event.getSubcommandName());

        List<MemberInfo> topTenMembers = topTenCommand == null
                ? Collections.emptyList()
                : topTenCommand.retrieveTopTen(service);

        List<Long> top10Ids = topTenMembers.stream()
                .map(MemberInfo::getId)
                .collect(Collectors.toList());

        Objects.requireNonNull(event.getGuild())
                .retrieveMembersByIds(top10Ids)
                .onSuccess(members -> {

                    // TODO this is a crappy bandaid because I don't have time to fix sorting.... fix it...
                    List<MemberInfoDTO> memberInfoDTOS = extractMemberInfos(event.getSubcommandName(), members);

                    int maxNameLength = extractMaxNameLength(members);

                    StringBuilder sb = new StringBuilder();
                    sb.append(getTitle(Objects.requireNonNull(event.getSubcommandName())));

                    int listNumber = 1;
                    for (MemberInfoDTO member : memberInfoDTOS) {
                        MemberInfo stats = retrieveMemberInfoById(topTenMembers, member.getMember().getIdLong());
                        int thankCount = getThankCount(stats.getId(), event.getSubcommandName());
                        sb.append(buildDescription(member.getMember().getEffectiveName(), thankCount, listNumber++, maxNameLength)).append("\n");
                    }

                    event.getHook().sendMessage("```\n" + sb + "```").queue();
                });
    }

    @NotNull
    private String getTitle(String commandName) {
        LeaderboardTitle leaderboardTitle = LeaderboardTitle.value(commandName);

        return Optional.ofNullable(leaderboardTitle)
                .map(LeaderboardTitle::getText)
                .orElse("");
    }

    private int getThankCount(Long id, String command) {
        MemberInfo memberInfo = service.findById(id);

        ThankCountCommand thankCountCommand = ThankCountCommand.value(command);

        return Optional.ofNullable(thankCountCommand.getThanksCount(memberInfo))
                .orElse(-1);
    }

    // TODO cleanup this duplicated code
    public void sendLeaderboardToChannelAndReset(TextChannel channel, List<MemberInfo> memberInfos) {
        List<Long> top10Ids = memberInfos.stream()
                .map(MemberInfo::getId)
                .collect(Collectors.toList());

        Objects.requireNonNull(channel)
                .getGuild()
                .retrieveMembersByIds(top10Ids)
                .onSuccess(members -> {
                    getConsumer(members, channel, memberInfos, "month");
                    service.resetForMonth();
                });
    }

    private void getConsumer(List<Member> members, TextChannel channel, List<MemberInfo> memberInfos, String command) {
        // TODO this is a crappy bandaid because I don't have time to fix sorting.... fix it...
        List<MemberInfoDTO> memberInfoDTOS = extractMemberInfos(command, members);

        int maxNameLength = extractMaxNameLength(members);

        String leaderboard = buildLeaderBoard(memberInfos, memberInfoDTOS, maxNameLength);

        channel.sendMessage("```\n" + leaderboard + "```").queue();
    }

    private static int extractMaxNameLength(List<Member> members) {
        final int defaultValue = -1;

        return members == null
                ? defaultValue
                : members.stream()
                        .map(Member::getEffectiveName)
                        .mapToInt(String::length)
                        .max()
                        .orElse(defaultValue);
    }

    @NotNull
    private List<MemberInfoDTO> extractMemberInfos(String command, List<Member> members) {
        return members == null
                ? Collections.emptyList()
                : members.stream()
                        .map(member -> new MemberInfoDTO(member, getThankCount(member.getIdLong(), command)))
                        .filter(dto -> dto.getThankCount() > 0)
                        .sorted(Comparator.comparing(MemberInfoDTO::getThankCount).reversed())
                        .toList();
    }

    private String buildLeaderBoard(List<MemberInfo> memberInfos, List<MemberInfoDTO> memberInfoDTOS, int maxNameLength) {
        StringBuilder sb = new StringBuilder();
        LocalDate today = LocalDate.now().minusMonths(1);
        String month = StringUtils.capitalize(today.getMonth().name().toLowerCase());
        sb.append("Leaderboard ").append(month).append(" totals (Total/Month)\n\n");
        int listNumber = 1;
        for (MemberInfoDTO member : memberInfoDTOS) {
            MemberInfo stats = retrieveMemberInfoById(memberInfos, member.getMember().getIdLong());
            sb.append(buildDescription(member.getMember().getEffectiveName(), stats, listNumber++, maxNameLength)).append("\n");
        }
        return sb.toString();
    }

    private String buildDescription(String name, int thankCount, int position, int maxLength) {
        return String.format(
                        "%d. %-" + maxLength + "s\t%d",
                        position,
                        name,
                        thankCount)
                .replaceAll("\s", "\\ ");
    }

    private String buildDescription(String name, MemberInfo stats, int position, int maxLength) {
        return String.format(
                        "%d. %-" + maxLength + "s\t%d/%d",
                        position,
                        name,
                        stats.getTotalThankCount(),
                        stats.getMonthThankCount())
                .replaceAll("\s", "\\ ");
    }

    public MemberInfo retrieveMemberInfoById(List<MemberInfo> memberInfos, long id) {
        return memberInfos == null
                ? null
                : memberInfos.stream()
                        .filter(member -> member.getId().equals(id))
                        .findFirst()
                        .orElse(null);
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return commandData;
    }

    @Scheduled(cron = "0 0 18 1 * * ")
    public void resetLeaderboard() {
        final Function<MemberInfoService, List<MemberInfo>> findTop10AllTime = MemberInfoService::findTop10AllTime;
        List<MemberInfo> memberInfos = service.findTop10ForMonth();
        TextChannel leaderboardChannel = Objects.requireNonNull(jda.getGuildById(guildId))
                .getTextChannelById(leaderboardChannelId);
        //TODO fix this method also handling reset, didn't want to deal with waiting
        sendLeaderboardToChannelAndReset(leaderboardChannel, memberInfos);
    }

    enum TopTenCommand {
        ALLTIME("alltime", MemberInfoService::findTop10AllTime),
        MONTH("month", MemberInfoService::findTop10ForMonth);

        String key;
        Function<MemberInfoService, List<MemberInfo>> method;

        TopTenCommand(String inKey, Function<MemberInfoService, List<MemberInfo>> inMethod) {
            key = inKey;
            method = inMethod;
        }

        List<MemberInfo> retrieveTopTen(MemberInfoService memberInfoService) {

            return method.apply(memberInfoService);
        }

        static TopTenCommand value(String inKey) {

            TopTenCommand leaderboardTitle;
            try {
                leaderboardTitle = valueOf(inKey.toUpperCase());
            } catch (Exception e) {
                leaderboardTitle = null;
            }

            return leaderboardTitle;
        }
    }
    enum LeaderboardTitle {
        ALLTIME("alltime", "All Time Leaderboard\n\n"),
        MONTH("month", "Current Month Leaderboard\n\n");

        String key;
        String text;

        LeaderboardTitle(String inKey, String inText) {
            key = inKey;
            text = inText;
        }

        String getText() {
            return text;
        }

        static LeaderboardTitle value(String inKey) {

            LeaderboardTitle leaderboardTitle;
            try {
                leaderboardTitle = valueOf(inKey.toUpperCase());
            } catch (Exception e) {
                leaderboardTitle = null;
            }

            return leaderboardTitle;
        }
    }

    enum ThankCountCommand {
        ALLTIME("alltime", MemberInfo::getTotalThankCount),
        MONTH("month", MemberInfo::getMonthThankCount);

        final String value;
        final Function<MemberInfo, Integer> memberInfoIntegerFunction;

        ThankCountCommand(String inValue, Function<MemberInfo, Integer> inMemberInfoIntegerFunction) {
            value = inValue;
            memberInfoIntegerFunction = inMemberInfoIntegerFunction;
        }

        static ThankCountCommand value(String inValue) {
            ThankCountCommand thankCountCommand;
            try {
                thankCountCommand = valueOf(inValue.toUpperCase());
            } catch (Exception e) {
                thankCountCommand = null;
            }

            return thankCountCommand;
        }

        Integer getThanksCount(MemberInfo memberInfo) {

            return Optional.ofNullable(memberInfo)
                    .map(memberInfoIntegerFunction)
                    .orElse(-1);
        }

    }

}

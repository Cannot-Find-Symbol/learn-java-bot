package org.learn_java.bot.event.listeners;


import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.data.entities.MemberInfo;
import org.learn_java.bot.service.MemberInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;


import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class ThanksListener extends ListenerAdapter {

    private final MemberInfoService service;
    private final Map<Long, LocalDateTime> recentlyUsedByMembers;
    private final Set<String> blacklistedChannels;

    private static final ErrorHandler errorHandler = new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE);


    public ThanksListener(MemberInfoService service,
                          @Value("${thanks.blacklisted.channels}") Set<String> blacklistedChannels) {
        this.service = service;
        this.recentlyUsedByMembers = Collections.synchronizedMap(new HashMap<>());
        this.blacklistedChannels = blacklistedChannels;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (shouldIgnoreThank(event)) {
            return;
        }
        getRecentlyTalkedMembers(event).thenAccept(members -> sendMemberList(event, members));
    }

    private boolean shouldIgnoreThank(@NotNull GuildMessageReceivedEvent event) {
        return event.getAuthor().isBot() || event.getMember() == null || blacklistedChannels.contains(event.getChannel().getId()) || !containsThanks(event) || recentlyUsed(event);
    }

    private void sendMemberList(@NotNull GuildMessageReceivedEvent event, List<Member> members) {
        if (!members.isEmpty() && event.getMember() != null) {
            List<SelectOption> options = createMemberSelectOptions(members);
            SelectOption dismiss = SelectOption.of("Nobody", "dismiss").withDescription("Select to dismiss this message");
            options.add(dismiss);
            SelectionMenu menu = SelectionMenu.create("thanks" + ":" + event.getMember().getId()).addOptions(options).setPlaceholder("Member").build();
            event.getChannel().sendMessage("It looks like you've thanked someone, who helped you?")
                    .setActionRow(menu)
                    .queue(messageSentHandler(event));
        }
    }

    @NotNull
    private Consumer<Message> messageSentHandler(@NotNull GuildMessageReceivedEvent event) {
        return sentMessage -> event.getChannel().retrieveMessageById(sentMessage.getId())
                .queueAfter(1, TimeUnit.MINUTES, (updatedMessage) -> updatedMessage.delete().setCheck(() -> !updatedMessage.isEdited()).queue(), errorHandler);
    }

    private boolean containsThanks(@NotNull GuildMessageReceivedEvent event) {
       String message = event.getMessage().getContentRaw().toLowerCase();
        return message.contains("thanks") || message.contains("thank you");
    }

    private boolean recentlyUsed(GuildMessageReceivedEvent event) {
        long id = Objects.requireNonNull(event.getMember()).getIdLong();
        if (recentlyUsedByMembers.containsKey(id)) {
            return Duration.between(recentlyUsedByMembers.get(id), LocalDateTime.now()).toMinutes() < 10;
        }
        return false;
    }

    @NotNull
    private List<SelectOption> createMemberSelectOptions(List<Member> members) {
        return members.stream().map(member -> SelectOption.of(member.getEffectiveName(), member.getId())).collect(Collectors.toList());
    }

    @NotNull
    private CompletableFuture<List<Member>> getRecentlyTalkedMembers(@NotNull GuildMessageReceivedEvent event) {
        return event.getChannel().getIterableHistory().takeAsync(10).thenApply(messages -> filterMemberList(messages, event));
    }

    public List<Member> filterMemberList(List<Message> messages, GuildMessageReceivedEvent event) {
        return messages.stream()
                .map(Message::getMember)
                .filter(Objects::nonNull)
                .filter(this::isNotBot)
                .filter(member -> isNotSelf(event, member))
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isNotBot(Member member) {
        return !member.getUser().isBot();
    }

    private boolean isNotSelf(@NotNull GuildMessageReceivedEvent event, Member member) {
        return event.getMember() != null && member.getIdLong() != event.getMember().getIdLong();
    }

    public void onSelectionMenu(@Nonnull SelectionMenuEvent event) {
        String menuId = event.getComponentId();
        if (!menuId.startsWith("thanks")) return;
        String memberId = event.getComponentId().split(":")[1];
        if (!event.getMember().getId().equals(memberId)) {
            event.reply("This interaction was not for you :) Sorry").setEphemeral(true).queue();
            return;
        }

        if (event.getValues().contains("dismiss")) {
            event.getMessage().delete().queue();
            return;
        }

        String id = event.getValues().get(0);
        if (event.isFromGuild()) {
            event.deferEdit().queue();
            recentlyUsedByMembers.put(Objects.requireNonNull(event.getMember()).getIdLong(), LocalDateTime.now());
            event.getGuild().retrieveMemberById(id).queue((member) -> {
                MemberInfo info = service.updateThankCountForMember(member.getIdLong());
                event.getHook().editOriginal(member.getEffectiveName() + " has been awarded a point! Now has a total of " + info.getTotalThankCount() + " point(s)").setActionRows(Collections.emptyList()).queue(null, errorHandler);
            });
        }
    }


    @Scheduled(cron = "* */15 * * * *")
    public void purgeCache() {
        LocalDateTime now = LocalDateTime.now();
        recentlyUsedByMembers.entrySet().removeIf(e -> Duration.between(e.getValue(), now).toHours() >= 10);
    }
}


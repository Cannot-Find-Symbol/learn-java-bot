package org.learn_java.bot.event.listeners.run_listener;

import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;

@Component
public class RunListener extends ListenerAdapter {
    private static final String RUNNING = EmojiManager.getForAlias("running").getUnicode();
    private static final String EXECUTE = "https://emkc.org/api/v2/piston/execute";
    private static final String RUNTIMES = "https://emkc.org/api/v2/piston/runtimes";
    private static final String LANGUAGE_IS_INVALID = "Sorry, can't run this code. No language provided or language is invalid";
    public static final int FIELD_WIDTH = 1024;
    private final WebClient client;
    private final Map<String, Language> languageMap;

    public RunListener() {
        client = WebClient.create();
        languageMap = new HashMap<>();
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        getLanguages();
    }

    private void getLanguages() {
        client.get().uri(RUNTIMES)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Language>>(){})
                .subscribe(this::buildLanguageMap);
    }


    private void buildLanguageMap(List<Language> languages) {
        languages.forEach(language -> {
            languageMap.put(language.getLanguage(), language);
            language.getAliases().forEach(alias -> languageMap.put(alias, language));
        });
    }

    private boolean hasCodeBlock(Message message) {
        return StringUtils.countMatches(message.getContentRaw(), "```") == 2;
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Message message = event.getMessage();

        if (event.getAuthor().isBot() || !hasCodeBlock(message)) {
            return;
        }

        message.addReaction(RUNNING).queue();
    }

    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot() || !RUNNING.equals(event.getReactionEmote().getEmoji())) {
            return;
        }

        String messageId = event.getReaction().getMessageId();
        event.getReaction().getChannel().retrieveMessageById(messageId)
                .queue(message -> sendResponseToChannel(event, message));
        event.getReaction().clearReactions().queue();
    }

    private void sendResponseToChannel(GuildMessageReactionAddEvent event, Message message) {
        String lang = StringUtils.substringBetween(message.getContentRaw(), "```", "\n");
        String code = StringUtils.substringBetween(message.getContentRaw(), "```" + lang, "```");

        if (languageMap.containsKey(lang)) {
            Language language = languageMap.get(lang);
            sendRequest(event, code, language);
        } else {
            event.getChannel().sendMessage(LANGUAGE_IS_INVALID).queue();
        }
    }

    private void sendRequest(GuildMessageReactionAddEvent event, String code, Language language) {
        try {
            client.post()
                    .uri(new URI(EXECUTE))
                    .body(Mono.just(new RunRequest(code, language)), RunRequest.class)
                    .retrieve()
                    .bodyToMono(RunResponse.class)
                    .subscribe(sendResponse(event, language));
        } catch (URISyntaxException ignored) {
        }
    }

    private Consumer<RunResponse> sendResponse(GuildMessageReactionAddEvent event, Language language) {
        return (r) -> event.getChannel().sendMessageEmbeds(buildResponse(language, r)).queue();
    }

    private MessageEmbed buildResponse(Language language, RunResponse r) {
        String output = r.getRun().getOutput();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Run Code Result");
        builder.addField("Language", language.getLanguage(), true);
        builder.addField("Version", language.getVersion(), true);
        builder.addField("Output Truncated", Boolean.toString(output.length() > FIELD_WIDTH), true);
        builder.addField("Output", StringUtils.truncate(output, FIELD_WIDTH), false);

        return builder.build();
    }


    @Scheduled(cron = "0 0 6 * * *")
    public void refreshLanguageMap() {
        getLanguages();
    }
}

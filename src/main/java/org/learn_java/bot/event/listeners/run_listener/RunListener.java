package org.learn_java.bot.event.listeners.run_listener;

import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RunListener extends ListenerAdapter {
    private static final String RUNNING = EmojiManager.getForAlias("running").getUnicode();
    private WebClient client = WebClient.create();
    Map<String, Language> languageMap = new HashMap<>();


    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        buildLanguageMap();
    }

    private void buildLanguageMap() {
        client.get().uri("https://emkc.org/api/v2/piston/runtimes")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Language>>() {})
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
        if (event.getUser().isBot()) {
            return;
        }

        String messageId = event.getReaction().getMessageId();
        String emote = event.getReactionEmote().getEmoji();

        if(RUNNING.equals(emote)) {
             event.getReaction().getChannel().retrieveMessageById(messageId).queue(message -> {
                 String lang = StringUtils.substringBetween(message.getContentRaw(), "```", "\n");
                 String code = StringUtils.substringBetween(message.getContentRaw(), "```" + lang, "```");

                 if(!languageMap.containsKey(lang)) {
                    buildLanguageMap();
                 }

                 if(languageMap.containsKey(lang)) {
                     Language language = languageMap.get(lang);
                     try {
                         client.post()
                                 .uri(new URI("https://emkc.org/api/v2/piston/execute"))
                                 .body(Mono.just(new RunRequest(code, language)), RunRequest.class)
                                 .retrieve()
                                 .bodyToMono(RunResponse.class)
                                 .subscribe((r) -> event.getChannel().sendMessageEmbeds(buildResponse(language, r)).queue());
                     } catch (URISyntaxException ignored) {
                     }
                 } else {
                     event.getChannel().sendMessage("Sorry, can't run this code. No language provided or language is invalid").queue();
                 }
             });
        }
        event.getReaction().clearReactions().queue();
    }

    private MessageEmbed buildResponse(Language language, RunResponse r) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Run Code Result");
        builder.addField("Language", language.getLanguage(), false);
        builder.addField("Version", language.getVersion(), false);
        builder.addField("Output", r.getRun().getOutput(), true);
        return builder.build();
    }
}

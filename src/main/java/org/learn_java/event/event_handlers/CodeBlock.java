package org.learn_java.event.event_handlers;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.learn_java.event.ActionableEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class CodeBlock implements ActionableEvent<GuildMessageReceivedEvent> {

    @Override
    public void handle(GuildMessageReceivedEvent event) {
        Message message = event.getMessage();

        if (StringUtils.countMatches(message.getContentRaw(), "```") != 2) {
            return;
        }

        String originalMessage = event.getMessage().getContentRaw();
        String beginning = StringUtils.substringBefore(originalMessage, "```");
        String code = StringUtils.substringBetween(originalMessage, "```", "```");
        String end = StringUtils.substringAfterLast(originalMessage, "```");
        List<String> blockContents = new ArrayList<>(Arrays.asList(code.split("\n")));
        int start = 1;
        for (int i = 1; i < blockContents.size(); i++) {
            blockContents.set(i, String.format("%2d %s", start++, blockContents.get(i)));
        }
        String language = blockContents.remove(0);
        StringJoiner joiner = new StringJoiner("\n");
        blockContents.forEach(joiner::add);
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append(beginning);
        messageBuilder.appendCodeBlock(joiner.toString(), language);
        messageBuilder.append(end);
        if (messageBuilder.getStringBuilder().toString().length() >= 2000) {
            event.getChannel().sendMessage("Sorry too long, can't add line numbers").queue();
            return;
        }
        Message reformatted = messageBuilder.build();
        event.getMessage().delete().queue();
        event.getChannel().sendMessage(reformatted).queue();
    }
}

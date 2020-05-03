package org.learn_java.event.listeners.code_block;

import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CodeBlockListener extends ListenerAdapter {

    private static final String THUMBS_UP = "\uD83D\uDC4D";
    private static final String THUMBS_DOWN = "\uD83D\uDC4E";
    private static final String DELETE = "❌";
    private static final String NUMBERS = EmojiManager.getForAlias("1234").getUnicode();

    private final List<CodeBlock> codeBlocks = new ArrayList<>();

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Message message = event.getMessage();

        if (event.getAuthor().isBot() || !hasCodeBlock(message)) {
            return;
        }

        message.addReaction(NUMBERS).queue();
    }

    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        String messageId = event.getReaction().getMessageId();
        String emote = event.getReactionEmote().getEmoji();
        Message messageToFormat = event.getReaction().getChannel().retrieveMessageById(messageId).complete();


        if (NUMBERS.equals(emote)) {
            if (event.getUser().equals(messageToFormat.getAuthor())) {
                handleFormattingCodeBlock(messageToFormat);
            }
        }

        for (CodeBlock block : codeBlocks) {
            if (block.getMessageId().equals(event.getReaction().getMessageId())) {
                String emoji = event.getReaction().getReactionEmote().getEmoji();
                RestAction<Message> channel = event.getChannel().retrieveMessageById(block.getMessageId());
                if (emoji.equals(THUMBS_UP)) {
                    channel.queue(message -> {
                        if (block.getOriginal().equals(message)) {
                            message.editMessage(block.getReformatted()).queue(block::setReformatted);
                        }
                    });

                }

                if (emoji.equals(THUMBS_DOWN)) {
                    channel.queue(message -> {
                        if (block.getReformatted().equals(message)) {
                            message.editMessage(block.getOriginal()).queue(block::setOriginal);
                        }
                    });
                }

                if (emoji.equals(DELETE)) {
                    channel.queue(message -> {
                        if (block.getOwner().equals(event.getUser())) {
                            message.delete().queue();
                            codeBlocks.remove(block);
                        }
                    });
                }
                event.getReaction().removeReaction(event.getUser()).queue();
            }
        }
    }


    private void handleFormattingCodeBlock(Message message) {
        String author = message.getAuthor().getAsMention() + "\n";
        String originalMessage = "Author: " + author + message.getContentRaw();
        String beginning = StringUtils.substringBefore(originalMessage, "```");
        String code = StringUtils.substringBetween(originalMessage, "```", "```");
        String end = StringUtils.substringAfterLast(originalMessage, "```");
        List<String> blockContents = Arrays.stream(code.split("\n")).collect(Collectors.toList());
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
            message.getChannel().sendMessage("Sorry too long, can't add line numbers").queue();
            return;
        }

        Message reformatted = messageBuilder.build();
        message.delete().queue();

        MessageAction newMessage = message.getChannel().sendMessage(reformatted);
        newMessage.queue(sentMessage -> {
            CodeBlock codeBlock = new CodeBlock(message, sentMessage, message.getAuthor());
            codeBlocks.add(codeBlock);
            sentMessage.addReaction(THUMBS_UP).queue(then -> sentMessage.addReaction(THUMBS_DOWN).queue(last -> sentMessage.addReaction(DELETE).queue()));
            sentMessage.clearReactions().queueAfter(5, TimeUnit.MINUTES, (delete) -> codeBlocks.remove(codeBlock), (error) -> codeBlocks.remove(codeBlock));
        });
    }

    private boolean hasCodeBlock(Message message) {
        return StringUtils.countMatches(message.getContentRaw(), "```") == 2;
    }
}




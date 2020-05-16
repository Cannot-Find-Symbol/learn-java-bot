package org.learn_java.event.listeners.code_block;

import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CodeBlockListener extends ListenerAdapter {

    private static final String THUMBS_UP;
    private static final String THUMBS_DOWN = EmojiManager.getForAlias("thumbsdown").getUnicode();
    private static final String DELETE = EmojiManager.getForAlias("x").getUnicode();
    private static final String NUMBERS = EmojiManager.getForAlias("1234").getUnicode();

    static {
        THUMBS_UP = EmojiManager.getForAlias("thumbsup").getUnicode();
    }

    private final Map<String, CodeBlock> codeBlocks = new HashMap<>();

    public CodeBlockListener() {
    }

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

        if (!codeBlocks.containsKey(event.getReaction().getMessageId())) {
            return;
        }

        CodeBlock block = codeBlocks.get(event.getReaction().getMessageId());
        RestAction<Message> foundMessage = event.getChannel().retrieveMessageById(block.getMessageId());


        if (emote.equals(THUMBS_UP)) {
            foundMessage.queue(message -> {
                if (block.getOriginal().equals(message)) {
                    message.editMessage(block.getReformatted()).queue(block::setReformatted);
                }
            });

        } else if (emote.equals(THUMBS_DOWN)) {
            foundMessage.queue(message -> {
                if (block.getReformatted().equals(message)) {
                    message.editMessage(block.getOriginal()).queue(block::setOriginal);
                }
            });
        } else if (emote.equals(DELETE)) {
            foundMessage.queue(message -> {
                if (block.getOwner().equals(event.getUser())) {
                    message.delete().queue();
                    codeBlocks.remove(message.getId());
                }
            });
        }

        event.getReaction().removeReaction(event.getUser()).queue();
    }

    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String messageId = event.getMessageId();
        if (hasCodeBlock(event.getMessage()) && !codeBlocks.containsKey(messageId)) {
            event.getMessage().addReaction(NUMBERS).queue();
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
            codeBlocks.put(sentMessage.getId(), codeBlock);
            sentMessage.addReaction(THUMBS_UP).queue(then -> sentMessage.addReaction(THUMBS_DOWN).queue(last -> sentMessage.addReaction(DELETE).queue()));
            sentMessage.clearReactions().queueAfter(5, TimeUnit.MINUTES, (delete) -> codeBlocks.remove(codeBlock.getMessageId()), (error) -> codeBlocks.remove(codeBlock.getMessageId()));
        });
    }

    private boolean hasCodeBlock(Message message) {
        return StringUtils.countMatches(message.getContentRaw(), "```") == 2;
    }
}




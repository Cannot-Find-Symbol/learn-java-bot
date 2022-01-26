package org.learn_java.bot.commands.user;


import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.commands.SlashCommand;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@ConditionalOnProperty(value = "code.enabled", havingValue = "true", matchIfMissing = true)
public class CodeCommand extends Command {

    private final Message message;
    private final CommandData commandData;

    public CodeCommand() {
        super("code", CommandType.ANY);
        message = buildMessage();
        this.commandData = new CommandData(getName(), "sends code block message");
    }

    private Message buildMessage() {
        MessageBuilder builder = new MessageBuilder();
        builder.append("\nPlease format your code using the following format\n");
        builder.append("\\`\\`\\`java\n");
        builder.append("//your code");
        builder.append("\n\\`\\`\\`");
        builder.append("\n Which will result in a code block such as");
        builder.appendCodeBlock("int x = 3;\nSystem.out.println(x);", "java");
        return builder.build();
    }

    @Override
    public void executeSlash(SlashCommandEvent event) {
        event.reply(message).queue();
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }

}

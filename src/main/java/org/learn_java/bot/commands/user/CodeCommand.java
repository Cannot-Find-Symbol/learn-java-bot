package org.learn_java.bot.commands.user;


import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.springframework.stereotype.Component;


@Component
public class CodeCommand extends Command {

    private final MessageCreateData message;
    private final SlashCommandData commandData;

    public CodeCommand() {
        super("code", CommandType.ANY);
        message = buildMessage();
        this.commandData = Commands.slash(getName(), "sends code block message");
    }

    private MessageCreateData buildMessage() {
        MessageCreateBuilder builder = new MessageCreateBuilder();
        builder.addContent("\nPlease format your code using the following format\n");
        builder.addContent("\\`\\`\\`java\n");
        builder.addContent("//your code");
        builder.addContent("\n\\`\\`\\`");
        builder.addContent("\n Which will result in a code block such as");
        builder.addContent(MarkdownUtil.codeblock("java", "int x = 3;\nSystem.out.println(x);"));
        return builder.build();
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        event.reply(message).queue();
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return commandData;
    }

}

package org.learn_java.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

public class Code extends Command {

    private Message message;
    private EventWaiter waiter;

    public Code(){
        this.name = "code";
        this.help = "Messages channel detailing how to format code in a code block";
        this.cooldown = 5;
        message = buildMessage();
    }

    private Message buildMessage(){
        MessageBuilder builder = new MessageBuilder();
        builder.append("\nPlease format your code using the following format\n");
        builder.append("\\`\\`\\`java\n");
        builder.append("//your code");
        builder.append("\n\\`\\`\\`");
        builder.append("\n Which will result in a code block such as");
        builder.appendCodeBlock("int x = 3\nSystem.out.println(x);", "java");
        builder.append("\nor if the snippit is too long to fit, http://hasteb.in/");
        return builder.build();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply(message);
    }
}

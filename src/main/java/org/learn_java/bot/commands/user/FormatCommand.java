package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import org.learn_java.bot.commands.ContextCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@ConditionalOnProperty(value = "format.enabled", havingValue = "true", matchIfMissing = true)
public class FormatCommand implements ContextCommand {

    private static final BigInteger MAX_MESSAGE_ID = new BigInteger(String.valueOf(Long.MAX_VALUE));
	private final CommandData contextCommandData;

    public FormatCommand(@Value("${format.cooldown:15}") int cooldown) {
		contextCommandData = Commands.context(Command.Type.MESSAGE, "Format");
        //this.cooldown = cooldown;
    }

	@Override
	public void executeContextCommand(MessageContextInteractionEvent event) {
		event.deferReply().queue();
		if(!event.getName().equals("Format")) return;
		String wrappedMessage = String.format("```%s\n%s\n```", "java", event.getTarget().getContentRaw());
		event.getHook().sendMessage(wrappedMessage).queue(RestAction.getDefaultSuccess(), error -> event.getHook().sendMessage("Something bad happened, couldn't format"));
	}

	@Override
	public CommandData getContextCommandData() {
		return contextCommandData;
	}

	@Override
	public String getName() {
		return "Format";
	}
}

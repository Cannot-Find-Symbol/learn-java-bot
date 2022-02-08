package org.learn_java.bot.commands.role;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.data.dtos.InfoDTO;
import org.learn_java.bot.data.entities.Info;
import org.learn_java.bot.service.InfoService;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Objects;

@Component
public class Infomaster extends Command {
	private final InfoService service;
	private final SlashCommandData commandData;

	public Infomaster(InfoService service) {
		super("infomaster", CommandType.ROLE);
		this.service = service;

		SubcommandData add = new SubcommandData("add", "add topic")
				.addOption(OptionType.STRING, "topic", "chosen topic", true)
				.addOption(OptionType.STRING, "content", "topic content", true);
		SubcommandData update = new SubcommandData("update", "update topic")
				.addOption(OptionType.STRING, "topic", "chosen topic", true)
				.addOption(OptionType.STRING, "content", "topic content", true);
		SubcommandData delete = new SubcommandData("delete", "delete topic")
				.addOption(OptionType.STRING, "topic", "chosen topic", true);

		this.commandData = Commands.slash(getName(), "infomaster commands")
				.addSubcommands(add, update, delete)
				.setDefaultEnabled(false);
	}


	@Override
	public void executeSlash(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		switch(Objects.requireNonNull(event.getSubcommandName())){
			case "add" -> handleAdd(event);
			case "update" -> handleUpdate(event);
			case "delete" -> handleDelete(event);
		}
	}

	public void handleAdd(SlashCommandInteractionEvent event) {
		String topic = Objects.requireNonNull(event.getOption("topic")).getAsString();
		String content = Objects.requireNonNull(event.getOption("content")).getAsString();
		service.save(new Info(topic, content));
		event.getHook().sendMessage("Succuessfully saved").queue();
	}

	@Transactional
	public void handleUpdate(SlashCommandInteractionEvent event) {
		String topic = Objects.requireNonNull(event.getOption("topic")).getAsString();
		String content = Objects.requireNonNull(event.getOption("content")).getAsString();
		service.findById(topic).ifPresentOrElse((info) -> update(content, info, event),
				() -> event.getHook().sendMessage("Sorry, can't find topic by that name").queue()
		);
	}

	private void update(String content, InfoDTO info, SlashCommandInteractionEvent event) {
		info.setMessage(content);
		event.getHook().sendMessage("Succuessfully deleted").queue();
	}

	public void handleDelete(SlashCommandInteractionEvent event) {
		String topic = Objects.requireNonNull(event.getOption("topic")).getAsString();


		if (!service.existsById(topic)) {
			event.getHook().sendMessage("Failed to delete topic, possibly doesn't exist?").queue();
		} else {
			service.deleteById(topic);
			event.getHook().sendMessage("Succuessfully deleted").queue();
		}
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return commandData;
	}
}

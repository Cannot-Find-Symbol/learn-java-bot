package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.data.dtos.InfoDTO;
import org.learn_java.bot.service.InfoService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class Info extends Command {
	private final InfoService service;
	private final SlashCommandData commandData;

	public Info(InfoService service) {
		super("info", CommandType.ANY);
		this.service = service;
		SubcommandData listSubcommand = new SubcommandData("list", "Lists all topics");
		SubcommandData topicSubcommand = new SubcommandData("topic", "Information about a topic")
				.addOption(OptionType.STRING, "topic", "chosen topic", true);

		this.commandData = Commands.slash(getName(), "Shows information about topic")
				.addSubcommands(listSubcommand, topicSubcommand);
	}

	@Override
	public void executeSlash(SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		switch(Objects.requireNonNull(event.getSubcommandName())) {
			case "list" -> listTopics(event);
			case "topic" -> sendTopic(event);
			default -> event.getHook().sendMessage("That command was not found... weird").queue();
		}

	}

	public void sendTopic(SlashCommandInteractionEvent event) {
		service.findById(Objects.requireNonNull(event.getOption("topic")).getAsString()).ifPresentOrElse(
				topic -> event.getHook().sendMessage(topic.getMessage()).queue(),
				() -> event.getHook().sendMessage("Sorry, couldn't find a topic by that name").queue());
	}

	public void listTopics(SlashCommandInteractionEvent event) {
		List<String> topics = service.findAll().stream()
				.map(InfoDTO::getTopic)
				.collect(Collectors.toList());

		event.getHook().sendMessageEmbeds(buildTopicsEmbed(topics)).queue();
	}

	private MessageEmbed buildTopicsEmbed(List<String> topics) {
		EmbedBuilder builder = new EmbedBuilder();
		StringBuilder sb = builder.getDescriptionBuilder();

		builder.setTitle("List of topics");

		int listNumber = 1;
		for (String topic : topics) {
			sb.append(buildTopicLine(listNumber++, topic));
		}

		builder.setDescription(sb.toString());

		return builder.build();
	}

	private String buildTopicLine(int listNumber, String topic) {
		return String.format("%4s %s%n", listNumber + ". ", topic);
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return this.commandData;
	}

	@Override
	public int getDelay() {
		return 10;
	}

	@Override
	public int getQuickLimit() {
		return 2;
	}
}

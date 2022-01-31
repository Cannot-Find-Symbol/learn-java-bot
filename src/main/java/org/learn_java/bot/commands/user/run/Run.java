package org.learn_java.bot.commands.user.run;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.commands.ContextCommand;
import org.learn_java.bot.commands.SlashCommand;
import org.learn_java.ekmc.PistonService;
import org.learn_java.ekmc.model.Language;
import org.learn_java.ekmc.model.RunRequest;
import org.learn_java.ekmc.model.RunResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Run implements ContextCommand {
	private final String name;
	private final CommandData commandData;
	private final SlashCommandData slashCommandData;
	private final Pattern pattern = Pattern.compile("https://discord.com/channels/(?>\\d{18}/){2}(\\d{18})");
	private final PistonService pistonService;

	private Map<String, Language> languageMap;
	private LocalDateTime lastLanguageRefresh;

	private static final int FIELD_WIDTH = 1024;
	private static final String LANGUAGE_IS_INVALID = "Sorry, can't run this code. No language provided or language is invalid";
	private static final Logger logger = LoggerFactory.getLogger(Run.class);

	private static final String COMMON_IMPORTS =
			"""
					import java.util.*;
					import java.io.*;
					import java.time.*;
					import java.math.*;
					            
					""";

	public Run(PistonService pistonService) {
		this.name = "run";
		SubcommandData runId = new SubcommandData("from-id", "run code from message id");
		runId.addOption(OptionType.STRING, "id", "message id to run", true);
		SubcommandData runMessageLink = new SubcommandData("from-message-link", "run code from message link");
		runMessageLink.addOption(OptionType.STRING, "link", "link to message that contains code", true);
		this.commandData = Commands.context(Command.Type.MESSAGE, "run");
		this.slashCommandData = Commands.slash(name, "runs code")
				.addSubcommands(runId, runMessageLink);
		this.pistonService = pistonService;
	}

	public void updateLanguages() {
		if (languageMap == null || Duration.between(lastLanguageRefresh, LocalDateTime.now()).toHours() > 24) {
			try {
				Response<List<Language>> response = pistonService.getRuntimes().execute();
				Objects.requireNonNull(response.body(), "Piston service getRuntimes returned null body");
				languageMap = buildLanguageMap(response.body());
				this.lastLanguageRefresh = LocalDateTime.now();
			} catch (IOException io) {
				logger.error(String.valueOf(io));
			}
		}
	}

	private Map<String, Language> buildLanguageMap(@NotNull List<Language> languages) {
		Map<String, Language> map = new HashMap<>();
		languages.forEach(language -> {
			map.put(language.getLanguage(), language);
			language.getAliases().forEach(alias -> map.put(alias, language));
		});
		return map;
	}

	private void sendResponseToChannel(Message message, InteractionHook hook) {
		String lang = StringUtils.substringBetween(message.getContentRaw(), "```", "\n");
		String code = StringUtils.substringBetween(message.getContentRaw(), "```" + lang, "```");

		if (languageMap.containsKey(lang)) {
			Language language = languageMap.get(lang);
			sendRequest(message, code, language, hook);
		} else {
			hook.sendMessage(LANGUAGE_IS_INVALID).queue();
		}
	}

	private void sendRequest(Message message, String code, Language language, InteractionHook hook) {
		RunRequest runRequest = new RunRequest(addCommonImports(code, language), language);
		pistonService.execute(runRequest).enqueue(new Callback<>() {
			@Override
			public void onResponse(@NotNull Call<RunResponse> call, @NotNull Response<RunResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					hook.sendMessageEmbeds(buildResponse(language, message, response.body())).queue();
				}
			}

			@Override
			public void onFailure(@NotNull Call<RunResponse> call, @NotNull Throwable throwable) {
				hook.sendMessage("Sorry, it seemst he run API is currently down or something broke.").queue();
			}
		});
	}

	private String addCommonImports(String code, Language language) {
		if (language.getLanguage().equalsIgnoreCase("java")) {
			return COMMON_IMPORTS + addClass(code);
		}
		return code;
	}

	private String addClass(String code) {
		if (code.contains("class")) return code;
		return "class Example {" + addMainMethod(code) + "};";
	}

	private String addMainMethod(String code) {
		if (code.contains("public static void main")) return code;
		return """
				public static void main(String[] args) {
				""" + code + "};";
	}

	private MessageEmbed buildResponse(Language language, Message message, RunResponse r) {
		String output = r.getRun().getOutput();
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Run Code Result");
		builder.addField("Language", language.getLanguage(), true);
		builder.addField("Version", language.getVersion(), true);
		builder.addField("code", message.getJumpUrl(), false);
		if (output.length() > FIELD_WIDTH) {
			builder.addField("Output Truncated", Boolean.toString(true), true);
		}
		builder.addField("Output", StringUtils.truncate(output, FIELD_WIDTH), false);

		return builder.build();
	}

	@Override
	public void executeContextCommand(MessageContextInteractionEvent event) {
		event.getInteraction().deferReply().queue();
		if(event.getName().equals("run")) {
			updateLanguages();
			event.getInteraction().getTargetType();
			sendResponseToChannel(event.getTarget(), event.getHook());
		}
		event.getInteraction().reply("Had an issue with this command").queue();
	}

	@Override
	public CommandData getContextCommandData() {
		return commandData;
	}

	@Override
	public String getName() {
		return name;
	}
}

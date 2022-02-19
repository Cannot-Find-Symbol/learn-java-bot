package org.learn_java.bot.event.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.configuration.Config;
import org.learn_java.bot.manager.QuestionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
public class QuestionListener extends ListenerAdapter implements Startup {
	public static final int MAX_THREAD_CHANNEL_NAME = 100;
	public static final int MESSAGE_AMOUNT_TO_RETRIEVE = 10;
	public static final String THREAD_CREATED_MESSAGE = """
			 Here is your help thread.

			**INSTRUCTIONS**
			Please use /edit to edit the thread name to have a very short general description of your problem (Max 100 characters) if it doesn't have one already
			Then please state the problem you're having clearly, what you've tried, and provide your code""";
	@Value("${questionChannelId}")
	private long questionChannelId;
	private final QuestionManager manager;
	private final JDA jda;
	private final Config config;
	private final static String CHANNEL_INSTRUCTIONS =
			"**INSTRUCTIONS**\nSend a short description of your problem. This message will be used to create a thread (max title length 100 chars). " +
			"After your thread is created please give a detailed message containing your problem, what you've tried, and your code.";

	public QuestionListener(JDA jda, Config config, QuestionManager manager) {
		this.manager = manager;
		this.jda = jda;
		this.config = config;
	}

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if(questionChannelId != event.getChannel().getIdLong()) return;
		if (shouldIgnoreEvent(event)) {
			deleteMessageIfNotFromBot(event);
			return;
		}

		String messageContent = event.getMessage().getContentStripped();
		String truncatedContent = StringUtils.truncate(messageContent, MAX_THREAD_CHANNEL_NAME);
		event.getTextChannel().createThreadChannel(truncatedContent).queue(thread -> {
			thread.sendMessage(buildThreadCreatedMessage(event)).queue();
			manager.register(event.getAuthor().getIdLong(), thread.getIdLong());
			clearOldMessages(event.getTextChannel()).whenComplete((a, b) -> sendInstructionMessage(event.getTextChannel()));
		});
	}

	private Message buildThreadCreatedMessage(@NotNull MessageReceivedEvent event) {
		MessageBuilder builder = new MessageBuilder();
		builder.append(event.getAuthor());
		builder.append(THREAD_CREATED_MESSAGE);
		return builder.build();
	}

	private void deleteMessageIfNotFromBot(@NotNull MessageReceivedEvent event) {
		if(!event.getAuthor().equals(jda.getSelfUser())){
			event.getMessage().delete().queue();
		}
	}

	private boolean shouldIgnoreEvent(@NotNull MessageReceivedEvent event) {
		return event.getChannel().getIdLong() != questionChannelId
				|| event.getAuthor().isBot() || event.getChannelType() != ChannelType.TEXT
				|| event.isFromThread() || manager.contains(event.getAuthor().getIdLong());
	}

	@Override
	public void startup() {
		Guild guild = Objects.requireNonNull(jda.getGuildById(config.getGuildId()));
		TextChannel questionChannel = Objects.requireNonNull(guild.getTextChannelById(questionChannelId));
		questionChannel.getManager().setSlowmode(5).queue();
		clearOldMessages(questionChannel).whenComplete((success, error) -> sendInstructionMessage(questionChannel));
	}

	private CompletableFuture<?> clearOldMessages(TextChannel questionChannel) {
		return questionChannel.getHistory().retrievePast(MESSAGE_AMOUNT_TO_RETRIEVE)
				.submit()
				.thenApply((history) -> deleteMessages(questionChannel, history));
	}

	@NotNull
	private CompletableFuture<Void> deleteMessages(TextChannel questionChannel, List<Message> history) {
		List<Message> messagesToDelete = history.stream().filter(message -> message.getType() != MessageType.THREAD_CREATED).toList();
		if(messagesToDelete.isEmpty()) {
			return CompletableFuture.completedFuture(null);
		}
		return messagesToDelete.size() == 1 ? messagesToDelete.get(0).delete().submit() : questionChannel.deleteMessages(messagesToDelete).submit();
	}

	private void sendInstructionMessage(TextChannel questionChannel) {
		questionChannel.sendMessage(CHANNEL_INSTRUCTIONS).submit();
	}
}

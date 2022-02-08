package org.learn_java.bot.event.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ModMail extends ListenerAdapter {

	private final String modMailChannelId;

	public ModMail(@Value("${modmail.channel}") String modMailChannelId){
		this.modMailChannelId = modMailChannelId;
	}

	private void handlePrivateMessage(MessageReceivedEvent event) {
		if(event.getJDA().getSelfUser() == event.getAuthor()) return;
		TextChannel modMailChannel = event.getJDA().getTextChannelById(modMailChannelId);
		if(modMailChannel != null) {
			modMailChannel.sendMessage(event.getAuthor().getAsTag() + ": " + event.getMessage().getContentRaw()).queue();
		}
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		switch(event.getChannelType()) {
			case TEXT -> handleChannelMessage(event);
			case PRIVATE -> handlePrivateMessage(event);
		}
	}

	private void handleChannelMessage(MessageReceivedEvent event) {
		Message message = event.getMessage();
		TextChannel channel = event.getTextChannel();
		if(isInvalidModMailReply(message, channel)) return;
		String referencedContent = Objects.requireNonNull(message.getReferencedMessage()).getContentRaw();
		String userTag = referencedContent.substring(0, referencedContent.indexOf(":"));
		User user = event.getJDA().getUserByTag(userTag);
		sendReplyToUser(event, user);
	}

	private void sendReplyToUser(MessageReceivedEvent event, User user) {
		if(user != null) {
			user.openPrivateChannel()
					.queue(privChannel -> privChannel.sendMessage(event.getMessage().getContentRaw()).queue());
		}
	}

	private boolean isInvalidModMailReply(Message message, TextChannel channel) {
		return !channel.getId().equals(modMailChannelId)
				|| MessageType.INLINE_REPLY != message.getType()
				|| message.getReferencedMessage() == null
				|| message.getReferencedMessage().getContentRaw().isEmpty()
				|| !message.getReferencedMessage().getContentRaw().contains(":");
	}
}

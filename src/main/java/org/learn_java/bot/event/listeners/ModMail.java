package org.learn_java.bot.event.listeners;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ModMail extends ListenerAdapter {

	private final String modMailChannelId;

	public ModMail(@Value("${modmail.channel}") String modMailChannelId){
		this.modMailChannelId = modMailChannelId;
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		if(event.getJDA().getSelfUser() == event.getAuthor()) return;
		TextChannel modMailChannel = event.getJDA().getTextChannelById(modMailChannelId);
		if(modMailChannel != null) {
			modMailChannel.sendMessage(event.getAuthor().getAsTag() + ": " + event.getMessage().getContentRaw()).queue();
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		TextChannel channel = event.getChannel();
		if(isInvalidModMailReply(message, channel)) return;
		String referencedContent = message.getReferencedMessage().getContentRaw();
		String userTag = referencedContent.substring(0, referencedContent.indexOf(":"));
		User user = event.getJDA().getUserByTag(userTag);
		sendReplyToUser(event, user);
	}

	private void sendReplyToUser(GuildMessageReceivedEvent event, User user) {
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

package org.learn_java.bot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Component;

@Component
public class Free extends Command {

  private static final String FREE_EMOJI = EmojiManager.getForAlias("free").getUnicode();
  private static final String TAKEN_EMOJI = EmojiManager.getForAlias("x").getUnicode();

  public Free() {
    this.name = "free";
    this.cooldown = 5;
  }

  @Override
  protected void execute(CommandEvent event) {
    TextChannel channel = event.getTextChannel();
    String channelName = channel.getName();
    if (channelName.contains(FREE_EMOJI) || !channelName.contains("help")) {
      event.reply("This channel is already free or is unable to be freed");
    } else {
      String originalName = stripEmojis(channel.getName());
      channel.getManager().setName(originalName + FREE_EMOJI).queue();
    }
    event.getMessage().delete().queue();
  }

  public String stripEmojis(String channelName) {
    return channelName.replaceAll(FREE_EMOJI, "").replaceAll(TAKEN_EMOJI, "");
  }
}

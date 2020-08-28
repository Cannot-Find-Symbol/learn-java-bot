package org.learn_java.bot.commands.free;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;
import org.springframework.stereotype.Component;

@Component
public class Free extends Command {

  private static final String FREE_EMOJI = EmojiManager.getForAlias("free").getUnicode();
  private static final String TAKEN_EMOJI = EmojiManager.getForAlias("x").getUnicode();

  public Free() {
    this.name = "free";
    this.cooldown = 5;
  }

  protected boolean isValidForFree(String name) {
    return name.contains(TAKEN_EMOJI) || name.contains("help");
  }

  public String stripEmojis(String channelName) {
    return channelName.replaceAll(TAKEN_EMOJI, "");
  }

  protected void setNameFree(ChannelManager channel) {
    channel.setName(stripEmojis(channel.getChannel().getName()) + FREE_EMOJI);
  }

  @Override
  protected void execute(CommandEvent event) {
    TextChannel channel = event.getTextChannel();
    if (isValidForFree(channel.getName())) {
      setNameFree(channel.getManager());
    } else {
      event.reply("This channel is already free or is unable to be freed");
    }
  }
}

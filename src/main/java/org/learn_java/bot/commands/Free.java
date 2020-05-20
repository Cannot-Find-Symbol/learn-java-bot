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
    if (channel.getName().contains(FREE_EMOJI)) {
      event.reply("This channel is already free");
    } else {
      String originalName = stripEmojis(channel.getName());
      channel.getManager().setName(originalName + FREE_EMOJI).queue();
    }
  }

  public String stripEmojis(String channelName) {
    return channelName.replaceAll(FREE_EMOJI, "").replaceAll(TAKEN_EMOJI, "");
  }
}

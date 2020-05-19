package org.learn_java.bot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.EmojiManager;
import org.springframework.stereotype.Component;

@Component
public class Free extends Command {

  private static final String FREE_EMOJI = EmojiManager.getForAlias("free").getUnicode();

  public Free() {
    this.name = "free";
  }

  @Override
  protected void execute(CommandEvent event) {}
}

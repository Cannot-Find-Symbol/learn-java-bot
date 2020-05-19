package org.learn_java.bot.event.listeners.free;

import com.vdurmont.emoji.EmojiManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.utils.concurrent.DelayedCompletableFuture;
import org.springframework.stereotype.Component;

@Component
public class FreeListener extends ListenerAdapter {

  private static final String FREE_EMOJI = EmojiManager.getForAlias("free").getUnicode();
  private static final String TAKEN_EMOJI = EmojiManager.getForAlias("x").getUnicode();

  Map<String, ChannelManager> helpChannels = new HashMap<>();
  Map<String, String> originalNames = new HashMap<>();
  Map<String, DelayedCompletableFuture<Void>> freeEvents = new HashMap<>();

  @Override
  public void onReady(@Nonnull ReadyEvent event) {
    event
        .getJDA()
        .getGuilds()
        .forEach(
            guild ->
                guild.getChannels().stream()
                    .filter(channel -> channel.getName().contains("help"))
                    .forEach(channel -> helpChannels.put(channel.getId(), channel.getManager())));

    helpChannels.forEach(
        (k, v) ->
            v.setName(stripEmojis(v.getChannel().getName()))
                .queue(
                    (complete) -> {
                      String strippedName = stripEmojis(v.getChannel().getName());
                      originalNames.put(k, strippedName);
                      v.setName(strippedName + FREE_EMOJI).queue();
                    }));
  }

  @Override
  public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
    String channelId = event.getChannel().getId();

    if (freeEvents.containsKey(channelId)) {

      if (freeEvents.get(channelId).isDone() || freeEvents.get(channelId).isCancelled()) {
        freeEvents.remove(channelId);
      }
    }

    if (freeEvents.containsKey(channelId)) {

      freeEvents.get(channelId).isCancelled();
      freeEvents.replace(
          channelId,
          helpChannels
              .get(channelId)
              .setName(originalNames.get(channelId) + FREE_EMOJI)
              .submitAfter(10, TimeUnit.SECONDS));
    }

    if (helpChannels.containsKey(channelId)) {
      ChannelManager manager = helpChannels.get(channelId);
      manager.setName(originalNames.get(channelId) + TAKEN_EMOJI).queue();
      freeEvents.put(
          channelId,
          manager
              .setName(originalNames.get(channelId) + FREE_EMOJI)
              .submitAfter(10, TimeUnit.SECONDS));
    }
    System.out.println(freeEvents);
  }

  public String stripEmojis(String channelName) {
    return channelName.replaceAll(FREE_EMOJI, "").replaceAll(TAKEN_EMOJI, "");
  }
}

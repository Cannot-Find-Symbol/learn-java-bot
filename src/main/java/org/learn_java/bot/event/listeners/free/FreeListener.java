package org.learn_java.bot.event.listeners.free;

import com.vdurmont.emoji.EmojiManager;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.ChannelManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "autofree.enabled", havingValue = "true", matchIfMissing = true)
public class FreeListener extends ListenerAdapter {

  private static final String FREE_EMOJI = EmojiManager.getForAlias("free").getUnicode();
  private static final String TAKEN_EMOJI = EmojiManager.getForAlias("x").getUnicode();

  private final Map<String, ChannelManager> helpChannels = new HashMap<>();
  private final Map<String, String> originalNames = new HashMap<>();
  private final int hours;
  private JDA jda;

  public FreeListener(@Value("${free.hours:3}") int hours) {
    this.hours = hours;
  }

  @Override
  public void onReady(@Nonnull ReadyEvent event) {
    jda = event.getJDA();
    jda.getGuilds()
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
    if (event.getAuthor().isBot()) {
      return;
    }
    if (event.getChannel().getName().contains(FREE_EMOJI)) {
      helpChannels.get(channelId).setName(originalNames.get(channelId) + TAKEN_EMOJI).queue();
    }
  }

  @Scheduled(cron = "0 0/15 * * * ?")
  public void freeChannels() {
    helpChannels.forEach(
        (k, v) -> {
          TextChannel channel = jda.getTextChannelById(k);
          if (channel != null && channel.hasLatestMessage()) {
            if (channel.getName().contains(FREE_EMOJI)) {
              return;
            }
            String latestMessageId = channel.getLatestMessageId();
            channel
                .retrieveMessageById(latestMessageId)
                .queue(
                    e -> {
                      OffsetDateTime lastMessage = e.getTimeCreated();
                      OffsetDateTime limit =
                          OffsetDateTime.now(lastMessage.getOffset())
                              .minus(Duration.ofHours(hours));

                      if (lastMessage.isBefore(limit)) {
                        v.setName(originalNames.get(k) + FREE_EMOJI).queue();
                      }
                    });
          }
        });
  }

  public String stripEmojis(String channelName) {
    return channelName.replaceAll(FREE_EMOJI, "").replaceAll(TAKEN_EMOJI, "");
  }
}

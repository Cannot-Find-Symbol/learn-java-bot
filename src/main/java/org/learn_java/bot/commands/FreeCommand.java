package org.learn_java.bot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "free.enabled", havingValue = "true", matchIfMissing = true)
public class FreeCommand extends Command implements SlashCommand {

    private static final String FREE_EMOJI = EmojiManager.getForAlias("free").getUnicode();
    private static final String TAKEN_EMOJI = EmojiManager.getForAlias("x").getUnicode();
    private final CommandData commandData;


    public FreeCommand(@Value("${free.cooldown:5}") int cooldown) {
        this.name = "free";
        this.cooldown = cooldown;
        this.commandData = new CommandData(name, "sends code block message");

    }

    @Override
    protected void execute(CommandEvent event) {
        run(event.getTextChannel());
    }

    private void run(TextChannel channel){
        String channelName = channel.getName();
        if (channelName.contains(FREE_EMOJI) || !channelName.contains("help")) {
            channel.sendMessage("This channel is already free or is unable to be freed").queue();
        } else {
            String originalName = stripEmojis(channel.getName());
            channel.getManager().setName(originalName + FREE_EMOJI).queue(null, (error) -> {
                channel.sendMessage("Sorry unable to free this channel, most likely we hit the rate limit becaue discord sucks").queue();
            });
        }
    }


    public String stripEmojis(String channelName) {
        return channelName.replaceAll(FREE_EMOJI, "").replaceAll(TAKEN_EMOJI, "");
    }

    @Override
    public void executeSlash(SlashCommandEvent event) {

    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}

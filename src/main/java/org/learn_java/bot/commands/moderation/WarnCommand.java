package org.learn_java.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.learn_java.bot.data.dtos.WarnDTO;
import org.learn_java.bot.data.entities.Warn;
import org.learn_java.bot.service.WarnService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(value = "warn.enabled", havingValue = "true", matchIfMissing = true)
public class WarnCommand extends Command {

    final WarnService service;

    public WarnCommand(WarnService service) {
        this.service = service;
        this.name = "warn";
        this.requiredRole = "moderator";
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s+", 2);

        Optional<User> user = event.getMessage().getMentionedUsers()
                .stream()
                .findFirst();

        if(user.isEmpty()) {
            event.reply("User cannot be blank");
            return;
        }

        if (event.getArgs().startsWith("show")) {
            handleShow(user.get().getAsTag(), event.getTextChannel());
            return;
        }

        String reason = args[1];

        service.save(createWarn(user.get(), reason));
    }

    public void handleShow(String userTag, TextChannel channel) {
        List<WarnDTO> warns = service.findByUsername(userTag);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Warns for @" + userTag);
        warns.forEach(warn -> eb.addField("Warn on " + warn.getDate(), warn.getReason(), false));
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    private Warn createWarn(User user, String reason) {
        return new Warn(user.getAsTag(), reason, LocalDate.now());
    }
}

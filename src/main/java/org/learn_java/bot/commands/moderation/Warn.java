package org.learn_java.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.learn_java.bot.data.repositories.WarnRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(value = "warn.enabled", havingValue = "true", matchIfMissing = true)
public class Warn extends Command {

    final WarnRepository repository;

    public Warn(WarnRepository repository) {
        this.repository = repository;
        this.name = "warn";
        this.requiredRole = "moderator";
    }

    @Override
    protected void execute(CommandEvent event) {

        if (event.getArgs().isBlank()) {
            return;
        }

        List<User> users = event.getMessage().getMentionedUsers();
        String command = event.getArgs().split("\\s+")[0];

        if (command.equals("show") && !users.isEmpty()) {
            String username = users.get(0).getAsTag();
            List<org.learn_java.bot.data.entities.Warn> warns = repository.findByUsername(username);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Warns for @" + username);
            warns.forEach(warn -> eb.addField("Warn on " + warn.getDate(), warn.getReason(), false));
            event.getChannel().sendMessage(eb.build()).queue();
            return;
        }

        List<String> args = List.of(event.getArgs().split("\\s+", users.size() + 1));

        List<org.learn_java.bot.data.entities.Warn> warns = new ArrayList<>();
        args.stream().filter(m -> !m.startsWith("<@") && !m.endsWith(">")).findFirst()
                .ifPresent(reason -> users.forEach(user -> warns
                        .add(new org.learn_java.bot.data.entities.Warn(user.getAsTag(), reason, LocalDate.now()))));
        repository.saveAll(warns);
    }
}

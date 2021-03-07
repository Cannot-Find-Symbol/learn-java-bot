package org.learn_java.bot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import org.learn_java.bot.data.repositories.InfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "info.enabled", havingValue = "true", matchIfMissing = true)
public class Info extends Command {
    private static final String addRole = "infomaster";
    private final InfoRepository repository;
    private final List<String> validCommands = Arrays.asList("add", "update", "delete", "topics");

    public Info(InfoRepository repository, @Value("${info.cooldown:5}") int cooldown) {
        this.repository = repository;
        this.name = "info";
        this.help = "";
        this.cooldown = cooldown;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s+", 3);
        if (!validCommands.contains(args[0])) {
            repository.findById(args[0]).ifPresentOrElse(
                    topic -> event.getChannel().sendMessage(topic.getMessage()).queue(),
                    () -> event.getChannel().sendMessage("Sorry, couldn't find a topic by that name").queue());

            return;
        }

        boolean hasPermission = event.getMember().getRoles().stream().map(Role::getName)
                .anyMatch(e -> e.equals(addRole));

        if (args[0].equals("topics")) {
            List<String> topics = repository.findAll().stream().map(org.learn_java.bot.data.entities.Info::getTopic)
                    .collect(Collectors.toList());
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder sb = builder.getDescriptionBuilder();
            builder.setTitle("List of topics");
            int[] listNumber = {1};
            topics.forEach(topic -> sb.append(String.format("%4s", listNumber[0]++ + ". ")).append(topic).append("\n"));
            builder.setDescription(sb.toString());
            event.getChannel().sendMessage(builder.build()).queue();
            return;
        }

        if (!hasPermission) {
            event.getChannel().sendMessage("Sorry, you don't have permission for that").queue();
            return;
        }

        switch (args[0]) {
            case "add":
                if (args.length != 3) {
                    event.getChannel().sendMessage("Sorry, invalid number of arguments for add/update").queue();
                    return;
                }
                repository.save(new org.learn_java.bot.data.entities.Info(args[1], args[2]));

                break;
            case "delete":
                if (args.length != 2) {
                    event.getChannel().sendMessage("Sorry, invalid number of arguments for delete").queue();
                    return;
                }
                if (!repository.existsById(args[1])) {
                    event.getChannel().sendMessage("Failed to delete tag, possibly doesn't exist?").queue();
                } else {
                    repository.deleteById(args[1]);
                }

                break;
            case "update":
                if (args.length != 3) {
                    event.getChannel().sendMessage("Sorry, invalid number of arguments for add/update").queue();
                    return;
                }
                if (repository.existsById(args[0])) {
                    event.getChannel().sendMessage("Failed to update tag, possibly doesn't exist?").queue();
                } else {
                    repository.save(new org.learn_java.bot.data.entities.Info(args[1], args[2]));
                }

                break;
        }
    }
}

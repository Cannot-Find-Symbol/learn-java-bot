package org.learn_java.bot.commands.user;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.learn_java.bot.data.dtos.InfoDTO;
import org.learn_java.bot.data.entities.Info;
import org.learn_java.bot.service.InfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "info.enabled", havingValue = "true", matchIfMissing = true)
public class InfoCommand extends Command {
    private static final String addRole = "infomaster";
    private final InfoService service;
    private final List<String> validCommands = Arrays.asList("add", "update", "delete", "topics");
    private final List<String> commandsRequiringPermission = Arrays.asList("add", "update", "delete");

    public InfoCommand(InfoService service, @Value("${info.cooldown:5}") int cooldown) {
        this.service = service;
        this.name = "info";
        this.help = "";
        this.cooldown = cooldown;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s+", 3);

        if(event.getArgs().isBlank()){
            handleShowTopics(event);
            return;
        }

        String command = args[0];

        if (!validCommands.contains(command)) {
            service.findById(command).ifPresentOrElse(
                    topic -> event.reply(topic.getMessage()),
                    () -> event.reply("Sorry, couldn't find a topic by that name"));

            return;
        }


        if (!hasPermission(command, event)) {
            event.reply("Sorry, you don't have permission for that");
            return;
        }

        switch (command) {
            case "add" -> handleAdd(args, event);
            case "delete" -> handleDelete(args, event);
            case "update" -> handleUpdate(args, event);
        }
    }

    public boolean hasPermission(String command, CommandEvent event) {
        if (!commandsRequiringPermission.contains(command)) {
            return true;
        }

        return event.getMember().getRoles()
                .stream()
                .map(Role::getName)
                .anyMatch(e -> e.equals(addRole));
    }

    public void handleShowTopics(CommandEvent event) {
        List<String> topics = service.findAll().stream()
                .map(InfoDTO::getTopic)
                .collect(Collectors.toList());

        event.getChannel().sendMessageEmbeds(buildTopicsEmbed(topics)).queue();
    }

    public void handleAdd(String[] args, CommandEvent event) {
        if (args.length != 3) {
            event.reply("Sorry, invalid number of arguments for add/update");
            return;
        }

        service.save(new Info(args[1], args[2]));
    }

    public void handleUpdate(String[] args, CommandEvent event) {
        if (args.length != 3) {
            event.reply("Sorry, invalid number of arguments for add/update");
            return;
        }

        if (!service.existsById(args[1])) {
            event.reply("Failed to update tag, possibly doesn't exist?");
        } else {
            service.save(new Info(args[1], args[2]));
        }
    }

    public void handleDelete(String[] args, CommandEvent event) {
        if (args.length != 2) {
            event.reply("Sorry, invalid number of arguments for delete");
            return;
        }

        if (!service.existsById(args[1])) {
            event.reply("Failed to delete tag, possibly doesn't exist?");
        } else {
            service.deleteById(args[1]);
        }
    }

    private MessageEmbed buildTopicsEmbed(List<String> topics) {
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder sb = builder.getDescriptionBuilder();

        builder.setTitle("List of topics");

        int listNumber = 1;
        for (String topic : topics) {
            sb.append(buildTopicLine(listNumber++, topic));
        }

        builder.setDescription(sb.toString());

        return builder.build();
    }

    private String buildTopicLine(int listNumber, String topic) {
        return String.format("%4s %s%n", listNumber + ". ", topic);
    }
}

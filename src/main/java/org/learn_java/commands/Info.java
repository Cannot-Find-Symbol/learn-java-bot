package org.learn_java.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import org.jooq.exception.DataAccessException;
import org.learn_java.data.dto.InfoDTO;
import org.learn_java.data.repositories.InfoRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Info extends Command {
    private final InfoRepository repository = new InfoRepository();
    private final List<String> validCommands = Arrays.asList("add", "update", "delete", "topics");
    private static final String addRole = "infomaster";

    public Info() {
        this.name = "info";
        this.help = "";
        this.cooldown = 3;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s+", 3);
        if (!validCommands.contains(args[0])) {
            Optional.ofNullable(repository.findByName(args[0]))
                    .ifPresentOrElse(tag -> event.getChannel()
                                                 .sendMessage(tag.getMessage())
                                                 .queue(), () ->
                                             event.getChannel()
                                                  .sendMessage("Sorry, couldn't find a tag by that name").queue());

            return;
        }


        boolean hasPermission = event.getMember()
                                     .getRoles()
                                     .stream()
                                     .map(Role::getName)
                                     .anyMatch(e -> e.equals(addRole));


        if(args[0].equals("topics")){
            List<String> topics = repository.getAll().stream().map(InfoDTO::getTagName).collect(Collectors.toList());
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder sb = builder.getDescriptionBuilder();
            builder.setTitle("List of topics");
            int[] listNumber = {1};
            topics.forEach(topic -> sb.append(listNumber[0]++).append(". ").append(topic).append("\n"));
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
                try {
                    repository.add(new InfoDTO(args[1], args[2]));
                } catch (DataAccessException e) {
                    event.getChannel().sendMessage("Failed to add tag, possibly a duplicate?").queue();
                }
                break;
            case "delete":
                if (args.length != 2) {
                    event.getChannel().sendMessage("Sorry, invalid number of arguments for delete").queue();
                    return;
                }
                try {
                    if(repository.remove(args[1]) == 0){
                        event.getChannel().sendMessage("Failed to delete tag, possibly doesn't exist?").queue();
                    }
                } catch (DataAccessException e) {
                    event.getChannel().sendMessage("Failed to delete tag, possibly doesn't exist?").queue();
                }
                break;
            case "update":
                if (args.length != 3) {
                    event.getChannel().sendMessage("Sorry, invalid number of arguments for add/update").queue();
                    return;
                }
                try {
                    if(repository.update(new InfoDTO(args[1], args[2]))== 0){
                        event.getChannel().sendMessage("Failed to update tag, possibly doesn't exist?").queue();
                    }
                } catch (DataAccessException e) {
                    event.getChannel().sendMessage("Failed to update tag, possibly doesn't exist?").queue();
                }
                break;
        }
    }
}

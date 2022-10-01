package org.learn_java.bot.commands.role;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.data.entities.Info;
import org.learn_java.bot.service.InfoService;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class Infomaster extends Command {
    private final InfoService service;
    private final SlashCommandData commandData;

    public Infomaster(InfoService service) {
        super("infomaster", CommandType.ROLE);
        this.service = service;

        SubcommandData fromContent = new SubcommandData("from-content", "add topic from content option")
                .addOption(OptionType.STRING, "topic", "chosen topic", true)
                .addOption(OptionType.STRING, "content", "topic content", true)
                .addOption(OptionType.STRING, "description", "topic description", true);
        SubcommandData fromMessage = new SubcommandData("from-message", "add topic from message")
                .addOption(OptionType.STRING, "topic", "chosen topic", true)
                .addOption(OptionType.STRING, "message-id", "message to use", true)
                .addOption(OptionType.STRING, "description", "topic description", true);
        SubcommandData delete = new SubcommandData("delete", "delete topic")
                .addOption(OptionType.STRING, "topic", "chosen topic", true);
        SubcommandGroupData add = new SubcommandGroupData("add", "add topic from content option")
                .addSubcommands(fromContent, fromMessage);
        SubcommandGroupData update = new SubcommandGroupData("update", "update topic")
                .addSubcommands(fromContent, fromMessage);
        this.commandData = Commands.slash(getName(), "infomaster commands")
                .addSubcommandGroups(add, update)
                .addSubcommands(delete)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }


    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        String rootCommand = event.getSubcommandGroup() != null ? event.getSubcommandGroup() : event.getSubcommandName();
        switch (Objects.requireNonNull(rootCommand)) {
            case "add" -> handleAdd(event);
            case "update" -> handleUpdate(event);
            case "delete" -> handleDelete(event);
        }
    }

    public void handleAdd(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "from-content" -> handleAddFromContent(event);
            case "from-message" -> handleAddFromMessage(event);
        }
    }


    private void handleAddFromContent(SlashCommandInteractionEvent event) {
        String topic = Objects.requireNonNull(event.getOption("topic")).getAsString().toLowerCase();
        String content = Objects.requireNonNull(event.getOption("content")).getAsString();
        String description = Objects.requireNonNull(event.getOption("description")).getAsString();
        saveMessage(topic, content, description, event);
        updateCommands(event, topic, description);
    }

    private static void updateCommands(SlashCommandInteractionEvent event, String topic, String description) {
        SlashCommandData data = Commands.slash(topic, description);
        event.getJDA().updateCommands().addCommands(data).queue();
    }

    private void handleAddFromMessage(SlashCommandInteractionEvent event) {
        String topic = Objects.requireNonNull(event.getOption("topic")).getAsString().toLowerCase();
        String messageId = Objects.requireNonNull(event.getOption("message-id")).getAsString();
        String description = Objects.requireNonNull(event.getOption("description")).getAsString();
        event.getChannel().retrieveMessageById(messageId).queue((message) -> saveMessage(topic, message.getContentRaw(), description, event), (e) -> sendReply(event, "Failed to save, possibly message id was bad?"));
        updateCommands(event, topic, description);
    }

    private void sendReply(SlashCommandInteractionEvent event, String content) {
        event.getHook().sendMessage(content).queue();
    }

    private void saveMessage(String topic, String message, String description, SlashCommandInteractionEvent event) {
        service.save(new Info(topic, message, description));
        sendReply(event, "Successfully saved");
    }

    public void handleUpdate(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "from-content" -> handleUpdateFromContent(event);
            case "from-message" -> handleUpdateFromMessage(event);
        }
    }

    private void handleUpdateFromContent(SlashCommandInteractionEvent event) {
        String topic = Objects.requireNonNull(event.getOption("topic")).getAsString().toLowerCase();
        String content = Objects.requireNonNull(event.getOption("content")).getAsString();
        String description = Objects.requireNonNull(event.getOption("description")).getAsString();
        service.findById(topic).ifPresentOrElse(
                (info) -> update(content, info, event),
                () -> sendReply(event, "Sorry, can't find topic by that name"));
        updateCommands(event, topic, description);
    }

    private void handleUpdateFromMessage(SlashCommandInteractionEvent event) {
        String topic = Objects.requireNonNull(event.getOption("topic")).getAsString().toLowerCase();
        String messageId = Objects.requireNonNull(event.getOption("message-id")).getAsString();
        event.getChannel().retrieveMessageById(messageId).queue(
                (message) -> updateFromFoundMessage(event, topic, message),
                (error) -> sendReply(event, "Failed to save, possibly message id was bad?"));
        String description = Objects.requireNonNull(event.getOption("description")).getAsString();
        updateCommands(event, topic, description);
    }

    private void updateFromFoundMessage(SlashCommandInteractionEvent event, String topic, Message message) {
        service.findById(topic).ifPresentOrElse(
                (info) -> update(message.getContentRaw(), info, event),
                () -> sendReply(event, "Sorry, can't find topic by that name"));
    }

    private void update(String content, Info info, SlashCommandInteractionEvent event) {
        info.setMessage(content);
        service.save(info);
        sendReply(event, "Successfully updated");
    }

    public void handleDelete(SlashCommandInteractionEvent event) {
        String topic = Objects.requireNonNull(event.getOption("topic")).getAsString();


        if (!service.existsById(topic)) {
            sendReply(event, "Failed to delete topic, possibly doesn't exist?");
        } else {
            service.deleteById(topic);
            sendReply(event, "Successfully deleted");
            event.getJDA().retrieveCommands().queue((commands) -> commands.stream().filter(command -> command.getName().equals(topic)).forEach(c -> c.delete().queue()));
        }
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return commandData;
    }
}

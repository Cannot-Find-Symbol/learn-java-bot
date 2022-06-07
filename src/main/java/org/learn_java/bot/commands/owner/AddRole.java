package org.learn_java.bot.commands.owner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.data.entities.MemberRole;
import org.learn_java.bot.data.entities.RoleGroup;
import org.learn_java.bot.event.listeners.RoleListener;
import org.learn_java.bot.service.RoleGroupService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class AddRole extends Command {
	private final SlashCommandData commandData;
	private final RoleGroupService service;
	private final RoleListener listener;

	public AddRole(RoleGroupService service, RoleListener listener) {
		super("manage-roles", CommandType.OWNER);
		this.listener = listener;

		SubcommandData view = new SubcommandData("view", "view current roles");

		SubcommandData add = new SubcommandData("add-role-to-group", "add role to group")
				.addOption(OptionType.ROLE, "role", "role", true)
				.addOption(OptionType.STRING, "description", "description", true)
				.addOption(OptionType.INTEGER, "order", "ordinal value", true)
				.addOption(OptionType.INTEGER, "group-id", "group id", true);

		SubcommandData createGroup = new SubcommandData("create-group", "Create new role group")
				.addOption(OptionType.CHANNEL, "channel", "channel id", true)
				.addOption(OptionType.BOOLEAN, "unique", "unique", false)
				.addOption(OptionType.STRING, "name", "group name", false)
				.addOption(OptionType.STRING, "message", "Message to be sent", false);

		commandData = Commands.slash("manage-roles", "manage role groups")
				.addSubcommands(view, add, createGroup);
		commandData.setDefaultEnabled(false);
		this.service = service;
	}

	@Override
	public void executeSlash(SlashCommandInteractionEvent event) {
		if (Objects.equals(event.getSubcommandName(), "view")) {
			event.deferReply(true).queue();
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Current groups/roles");
			service.findAll().forEach(group -> {
				List<Role> roles = Objects.requireNonNull(event.getGuild()).getRoles();
				group.getRoles()
						.forEach(role -> builder.addField(group.getName() + " " + group.getId(), findRoleNameById(roles, role.getId()), false));
			});
			event.getHook().sendMessageEmbeds(builder.build()).queue();
		} else if (Objects.equals(event.getSubcommandName(), "add-role-to-group")) {
			event.deferReply(true).queue();
			long groupId = Objects.requireNonNull(event.getOption("group-id")).getAsLong();
			RoleGroup group = service.findById(groupId);
			if (group == null) {
				event.getHook().sendMessage("Group id does not exist").queue();
				return;
			}
			Role role = event.getOptionsByType(OptionType.ROLE).get(0).getAsRole();
			String description = Objects.requireNonNull(event.getOption("description")).getAsString();
			int ordinal = (int) Objects.requireNonNull(event.getOption("order")).getAsLong();
			MemberRole memberRole = new MemberRole();
			memberRole.setId(role.getIdLong());
			memberRole.setDescription(description);
			memberRole.setOrdinal(ordinal);
			memberRole.setGroup(group);
			group.getRoles().add(memberRole);
			service.save(group);
			// move method into shared context
			listener.startup();
			event.getHook().sendMessage("Role added").queue();

		} else if (Objects.equals(event.getSubcommandName(), "create-group")) {
			event.deferReply(true).queue();
			RoleGroup group = new RoleGroup();
			MessageChannel channel = event.getOptionsByType(OptionType.CHANNEL).get(0).getAsMessageChannel();
			group.setGuildId(Objects.requireNonNull(event.getGuild()).getIdLong());
			OptionMapping uniqueMapping = event.getOption("unique");
			group.setUnique(uniqueMapping != null && uniqueMapping.getAsBoolean());
			group.setChannelId(Objects.requireNonNull(channel).getIdLong());
			OptionMapping name = event.getOption("name");
			group.setName(name == null ? "" : name.getAsString());
			OptionMapping message = event.getOption("message");
			group.setMessage(message == null ? "" : message.getAsString());
			RoleGroup saved = service.save(group);
			event.getHook().sendMessage("Guild created, id is " + saved.getId()).queue();
		}
	}

	private String findRoleNameById(List<Role> roles, long roleId) {
		for (Role role : roles) {
			if (role.getIdLong() == roleId) {
				return role.getName();
			}
		}
		return "ERROR";
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return commandData;
	}
}

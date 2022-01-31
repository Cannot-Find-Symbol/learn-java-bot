package org.learn_java.bot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.commands.SlashCommand;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class BanCommand implements SlashCommand {
	private final SlashCommandData commandData;
	private final CommandType commandType;
	private final String name;

	public BanCommand() {
		this.name = "ban";
		this.commandData = Commands.slash(name, "bans user")
				.addOption(OptionType.USER, "member", "member to ban", true)
				.addOption(OptionType.STRING, "reason", "reason for ban", true);
		this.commandType = CommandType.MODERATOR;
		commandData.setDefaultEnabled(false);
	}

	@Override
	public void executeSlash(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
		if (!botHasPermissionToBan(Objects.requireNonNull(member))) {
			event.getHook().sendMessage("I don't have permission to ban that member").queue();
			return;
		}

		member.ban(7, Objects.requireNonNull(event.getOption("reason")).getAsString())
				.queue(s -> event.getHook().sendMessage("Ban succuessful").queue(),
						e -> event.getHook().sendMessage("An error occured while trying to ban member").queue());

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return commandData;
	}

	@Override
	public CommandType getType() {
		return this.commandType;
	}

	private boolean botHasPermissionToBan(Member member) {
		Role botHighestRole = getHighestRole(member.getGuild().getSelfMember());
		Role memberHighestRole = getHighestRole(member);

		return memberHighestRole == null || botHighestRole == null || botHighestRole.compareTo(memberHighestRole) > 0 || !member.getPermissions().contains(Permission.ADMINISTRATOR);
	}

	private Role getHighestRole(Member member) {
		return member == null ? null : member.getRoles().stream().sorted().findFirst().orElse(null);
	}
}

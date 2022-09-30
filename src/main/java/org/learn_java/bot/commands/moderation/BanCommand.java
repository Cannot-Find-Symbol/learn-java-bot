package org.learn_java.bot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class BanCommand extends Command {
    private final SlashCommandData commandData;

    public BanCommand() {
        super("ban", CommandType.MODERATOR);
        this.commandData = Commands.slash(getName(), "bans user")
                .addOption(OptionType.USER, "member", "member to ban", true)
                .addOption(OptionType.STRING, "reason", "reason for ban", true)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
        if (!botHasPermissionToBan(Objects.requireNonNull(member))) {
            event.getHook().sendMessage("I don't have permission to ban that member").queue();
            return;
        }

        member.ban(7, TimeUnit.DAYS).reason(Objects.requireNonNull(event.getOption("reason")).getAsString())
                .queue(s -> event.getHook().sendMessage("Ban succuessful").queue(),
                        e -> event.getHook().sendMessage("An error occured while trying to ban member").queue());

    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return commandData;
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

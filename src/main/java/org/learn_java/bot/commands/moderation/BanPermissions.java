package org.learn_java.bot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public final class BanPermissions {
    private BanPermissions() {
    }

    public static boolean canBan(Member target) {
        Member bot = target.getGuild().getSelfMember();
        return bot.hasPermission(Permission.BAN_MEMBERS) && bot.canInteract(target);
    }
}

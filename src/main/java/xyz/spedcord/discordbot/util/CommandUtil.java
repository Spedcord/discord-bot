package xyz.spedcord.discordbot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public class CommandUtil {

    private CommandUtil() {
    }

    public static boolean isBotAdmin(Member member) {
        return member.hasPermission(Permission.ADMINISTRATOR) || member.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("Spedcord Bot Admin"));
    }

}

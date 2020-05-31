package xyz.spedcord.discordbot.util;

import net.dv8tion.jda.api.entities.Member;

public class CommandUtil {

    private CommandUtil() {
    }

    public static boolean isBotAdmin(Member member) {
        return member.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("Spedcord Bot Admin"));
    }

}

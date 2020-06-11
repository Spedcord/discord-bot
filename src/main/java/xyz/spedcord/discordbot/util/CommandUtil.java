package xyz.spedcord.discordbot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.settings.GuildSettingsProvider;

public class CommandUtil {

    private static GuildSettingsProvider settingsProvider;

    private CommandUtil() {
    }

    public static void init(GuildSettingsProvider provider) {
        settingsProvider = provider;
    }

    public static boolean isBotAdmin(Member member) {
        return member.hasPermission(Permission.ADMINISTRATOR) || member.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("Spedcord Bot Admin"));
    }

    public static boolean isInCommandChannel(TextChannel channel) {
        return channel.getIdLong() == settingsProvider.getGuildSettings(channel.getGuild().getIdLong()).getCommandChannelId();
    }

}

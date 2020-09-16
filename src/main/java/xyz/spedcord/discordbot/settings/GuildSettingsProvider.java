package xyz.spedcord.discordbot.settings;

import xyz.spedcord.common.config.Config;
import xyz.spedcord.discordbot.SpedcordDiscordBot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GuildSettingsProvider {

    private final Config config;

    public GuildSettingsProvider(Config config) {
        this.config = config;
    }

    public GuildSettings getGuildSettings(long guildId) {
        String rawSettings = config.get(guildId + "");
        if (rawSettings == null) {
            GuildSettings guildSettings = new GuildSettings(guildId, -1, -1);
            save(guildSettings);
            return guildSettings;
        }

        return SpedcordDiscordBot.GSON.fromJson(new String(Base64.getDecoder()
                .decode(rawSettings.getBytes(StandardCharsets.UTF_8))), GuildSettings.class);
    }

    public void save(GuildSettings guildSettings) {
        config.set(guildSettings.getGuildId() + "", Base64.getEncoder()
                .encodeToString(SpedcordDiscordBot.GSON.toJson(guildSettings)
                        .getBytes(StandardCharsets.UTF_8)));
        try {
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

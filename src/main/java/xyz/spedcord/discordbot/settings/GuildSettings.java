package xyz.spedcord.discordbot.settings;

public class GuildSettings {

    private final long guildId;
    private long logChannelId;

    public GuildSettings(long guildId, long logChannelId) {
        this.guildId = guildId;
        this.logChannelId = logChannelId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getLogChannelId() {
        return logChannelId;
    }

    public void setLogChannelId(long logChannelId) {
        this.logChannelId = logChannelId;
    }

}

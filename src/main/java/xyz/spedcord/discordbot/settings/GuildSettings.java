package xyz.spedcord.discordbot.settings;

public class GuildSettings {

    private final long guildId;
    private long logChannelId;
    private long commandChannelId;

    public GuildSettings(long guildId, long logChannelId, long commandChannelId) {
        this.guildId = guildId;
        this.logChannelId = logChannelId;
        this.commandChannelId = commandChannelId;
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

    public long getCommandChannelId() {
        return commandChannelId;
    }

    public void setCommandChannelId(long commandChannelId) {
        this.commandChannelId = commandChannelId;
    }

}

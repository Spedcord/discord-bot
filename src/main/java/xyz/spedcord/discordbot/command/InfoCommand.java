package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.settings.GuildSettings;
import xyz.spedcord.discordbot.settings.GuildSettingsProvider;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.time.Instant;
import java.util.Set;

public class InfoCommand extends AbstractCommand {

    private final GuildSettingsProvider settingsProvider;

    public InfoCommand(GuildSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    @SubCommand(isDefault = true)
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Spedcord Bot Info")
                .setDescription("Made by [Cerus](https://cerus-dev.de)\nGitHub repository: " +
                        "[Click here](https://github.com/Spedcord)\nGuilds: " + event.getJDA().getGuilds().size())
                .setFooter("Requested by " + member.getUser().getAsTag(), member.getUser().getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());

        if (CommandUtil.isBotAdmin(member)) {
            GuildSettings guildSettings = settingsProvider.getGuildSettings(channel.getGuild().getIdLong());

            String logChannel = (guildSettings.getLogChannelId() == -1 ? "None"
                    : channel.getGuild().getTextChannelById(guildSettings.getLogChannelId()).getAsMention());
            embedBuilder.addField("Guild specific info", "Log channel: " + logChannel, false);
        }

        channel.sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&info", Color.PINK,
                "Shows general information about Spedcord.")).build();
    }

}

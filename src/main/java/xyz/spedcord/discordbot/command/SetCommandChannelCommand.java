package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.settings.GuildSettings;
import xyz.spedcord.discordbot.settings.GuildSettingsProvider;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SetCommandChannelCommand extends AbstractCommand {

    private final GuildSettingsProvider settingsProvider;

    public SetCommandChannelCommand(GuildSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    @SubCommand(isDefault = true)
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!CommandUtil.isBotAdmin(member)) {
            channel.sendMessage(Messages.error("You need the `Spedcord Bot Admin` role or `Administrator` permission for this command."))
                    .queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        GuildSettings guildSettings = settingsProvider.getGuildSettings(channel.getGuild().getIdLong());

        TextChannel logChannel = event.getFirstChannelMention().orElse(channel);
        guildSettings.setCommandChannelId(logChannel.getIdLong());
        settingsProvider.save(guildSettings);

        channel.sendMessage(Messages.success("Channel " + logChannel.getAsMention()
                + " is now the command channel!")).queue();
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&setcommandchannel [#channel]", Color.PINK,
                "Sets the bot command channel.\n\n**Requires `Spedcord Bot Admin` role " +
                        "or `Administrator` permission**")).build();
    }

}

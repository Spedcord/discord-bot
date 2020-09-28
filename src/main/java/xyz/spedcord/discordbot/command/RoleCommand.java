package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.util.Set;

public class RoleCommand extends AbstractCommand {

    @SubCommand(isDefault = true)
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!CommandUtil.isInCommandChannel(channel)) {
            return;
        }

        channel.sendMessage(Messages.custom("Note", Color.ORANGE, "Please use the Spedcord " +
                "client for role and member management.")).queue();
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&role", Color.PINK,
                "Role management\n\n**`Spedcord Bot Admin` role or `Administrator` permission**")).build();
    }

}

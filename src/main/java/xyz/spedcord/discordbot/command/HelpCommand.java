package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractHelpCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import xyz.spedcord.discordbot.message.Messages;

import java.awt.*;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public class HelpCommand extends AbstractHelpCommand {

    @Override
    public void provideGeneralHelp(CommandEvent event, String prefix, Map<String, ICommand> commands) {
        Member member = event.getMember();
        User user = member.getUser();

        CommandSettings settings = event.getCommandSettings();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Spedcord Bot Help")
                .setDescription("To learn more about a command type `&help <command label>`\n\n")
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .setColor(Color.GRAY)
                .setTimestamp(Instant.now());

        embedBuilder.appendDescription("```\n" + String.join(", ", commands.keySet()) + "\n```");

        event.respond(embedBuilder.build());
    }

    @Override
    public void provideSpecificHelp(CommandEvent event, String prefix, ICommand command, Set<String> labels) {
        event.respond(command.info(event.getMember(), prefix, labels));
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder(Messages.custom("&help [command]", Color.PINK,
                "Shows general help or help for a command")).build();
    }

}

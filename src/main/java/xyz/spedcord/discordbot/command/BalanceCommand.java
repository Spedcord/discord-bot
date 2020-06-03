package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.message.Messages;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.Set;

public class BalanceCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public BalanceCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand()
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        Optional<User> mention = event.getFirstUserMention();
        if (mention.isEmpty()) {
            mention = Optional.of(member.getUser());
        }

        User user = mention.get();
        xyz.spedcord.discordbot.api.User userInfo = apiClient.getUserInfo(user.getIdLong(), false);

        if (userInfo == null) {
            channel.sendMessage(Messages.error(user.getAsTag() + " is not registered!")).queue();
            return;
        }

        channel.sendMessage(Messages.custom("Balance", Color.ORANGE, String.format("Balance of %s: `%s`",
                user.getAsTag(), new DecimalFormat("#,###").format(userInfo.getBalance()) + "$"))).queue();
    }

    @SubCommand(isDefault = true)
    public void onWrongUsage(CommandEvent event, Member member, TextChannel channel, String[] args) {
        channel.sendMessage(Messages.wrongUsage("&profile <@user>")).queue();
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&balance [@member]", Color.PINK,
                "Shows the balance of a member.")).build();
    }

}

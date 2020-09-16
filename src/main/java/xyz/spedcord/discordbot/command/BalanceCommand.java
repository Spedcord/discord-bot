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
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.Set;

public class BalanceCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public BalanceCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(args = "<@!?\\d+>")
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if(!CommandUtil.isInCommandChannel(channel)) {
            return;
        }

        Optional<User> mention = event.getFirstUserMention();
        if (mention.isEmpty()) {
            mention = Optional.of(member.getUser());
        }

        Message message = channel.sendMessage(Messages.pleaseWait()).complete();

        User user = mention.get();
        apiClient.getExecutorService().submit(() -> {
            xyz.spedcord.discordbot.api.User userInfo = apiClient.getUserInfo(user.getIdLong(), false);

            if (userInfo == null) {
                message.editMessage(Messages.error(user.getAsTag() + " is not registered!")).queue();
                return;
            }

            message.editMessage(Messages.custom("Balance", Color.ORANGE, String.format("Balance of %s: `%s`",
                    user.getAsTag(), new DecimalFormat("#,###").format(userInfo.getBalance()) + "$"))).queue();
        });
    }

    @SubCommand(isDefault = true)
    public void onWrongUsage(CommandEvent event, Member member, TextChannel channel, String[] args) {
        channel.sendMessage(Messages.wrongUsage("&balance [@member]")).queue();
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&balance [@member]", Color.PINK,
                "Shows the balance of a member.")).build();
    }

}

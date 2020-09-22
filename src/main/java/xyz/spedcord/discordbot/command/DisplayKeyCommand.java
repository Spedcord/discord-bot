package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.message.Messages;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DisplayKeyCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public DisplayKeyCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.cooldown = TimeUnit.SECONDS.toMillis(10);
    }

    @SubCommand(guildOnly = false)
    public void onExecution(CommandEvent event, User user, MessageChannel channel, String[] args) {
        if (!(channel instanceof PrivateChannel)) {
            channel.sendMessage(Messages.error("This command is only available in private chat!")).queue();
            return;
        }

        channel.sendMessage(Messages.pleaseWait()).queue(message -> {
            this.apiClient.getUserInfoAsync(user.getIdLong(), true).whenComplete((userInfo, throwable) -> {
                if (userInfo == null) {
                    channel.sendMessage(Messages.error("You are not registered! You can register yourself " +
                            "here: https://api.spedcord.xyz/user/register")).queue();
                    return;
                }

                message.editMessage(Messages.custom("Your key", Color.GRAY, String.format("||%s||\n**DO NOT " +
                        "share this key with anyone!**", userInfo.getKey()))).queue();
            });
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&key", Color.PINK,
                "Shows your user key.\n\n**Requires private chat**")).build();
    }

}

package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.message.Messages;

import java.awt.*;

public class DisplayKeyCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public DisplayKeyCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(guildOnly = false)
    public void onExecution(CommandEvent event, User user, MessageChannel channel, String[] args) {
        if (!(channel instanceof PrivateChannel)) {
            channel.sendMessage(Messages.error("This command is only available in private chat!")).queue();
            return;
        }

        channel.sendMessage(Messages.custom("Please wait", Color.ORANGE, "")).queue(message -> {
            xyz.spedcord.discordbot.api.User userInfo = apiClient.getUserInfo(user.getIdLong(), true);

            if(userInfo == null) {
                channel.sendMessage(Messages.error("You are not registered! You can register yourself " +
                        "here: https://api.spedcord.xyz/user/register")).queue();
                return;
            }

            message.editMessage(Messages.custom("Your key", Color.GRAY, String.format("||%s||\n**DO NOT " +
                    "share this key with anyone!**", userInfo.getKey()))).queue();
        });
    }

}
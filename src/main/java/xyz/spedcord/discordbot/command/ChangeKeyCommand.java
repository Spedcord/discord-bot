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

public class ChangeKeyCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public ChangeKeyCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(guildOnly = false)
    public void onExecution(CommandEvent event, User user, MessageChannel channel, String[] args) {
        if (!(channel instanceof PrivateChannel)) {
            channel.sendMessage(Messages.error("This command is only available in private chat!")).queue();
            return;
        }

        channel.sendMessage(Messages.custom("Please wait", Color.ORANGE, "")).queue(message -> {
            ApiClient.ApiResponse apiResponse = apiClient.changeUserKey(user.getIdLong());
            if (apiResponse.status != 200) {
                message.editMessage(Messages.error("Failed to change key. Please try again later.")).queue();
                return;
            }

            String key = apiResponse.body.substring(50, apiResponse.body.length() - 3);
            message.editMessage(Messages.success(String.format("Your new key is: ||%s||\n**DO NOT " +
                    "share this key with anyone!**", key))).queue();
        });
    }

}
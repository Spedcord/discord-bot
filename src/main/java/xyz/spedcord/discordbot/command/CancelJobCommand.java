package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.util.Set;

public class CancelJobCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public CancelJobCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(guildOnly = false)
    public void onExecution(CommandEvent event, User user, MessageChannel channel, String[] args) {
        channel.sendMessage(Messages.pleaseWait()).queue(message -> {
            apiClient.getExecutorService().submit(() -> {
                ApiClient.ApiResponse apiResponse = apiClient.cancelJob(user.getIdLong());
                if (apiResponse.status != 200) {
                    message.editMessage(Messages.error("Failed to cancel your job: `"
                            + JsonParser.parseString(apiResponse.body).getAsJsonObject().get("data").getAsJsonObject()
                            .get("message").getAsString() + "`")).queue();
                    return;
                }

                message.editMessage(Messages.success("Your job was cancelled!")).queue();
            });
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&canceljob", Color.PINK,
                "Cancels your current delivery.")).build();
    }

}

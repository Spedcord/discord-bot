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

import java.awt.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CancelJobCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public CancelJobCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.cooldown = TimeUnit.SECONDS.toMillis(10);
    }

    @SubCommand(guildOnly = false)
    public void onExecution(CommandEvent event, User user, MessageChannel channel, String[] args) {
        channel.sendMessage(Messages.pleaseWait()).queue(message -> {
            this.apiClient.cancelJobAsync(user.getIdLong()).whenComplete((apiResponse, throwable) -> {
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

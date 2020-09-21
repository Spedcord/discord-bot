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

public class LeaveCompanyCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public LeaveCompanyCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(guildOnly = false)
    public void onExecution(CommandEvent event, User user, MessageChannel channel, String[] args) {
        Message message = channel.sendMessage(Messages.pleaseWait()).complete();

        apiClient.getExecutorService().submit(() -> {
            ApiClient.ApiResponse apiResponse = apiClient.leaveCompany(user.getIdLong());

            if (apiResponse.status == 200) {
                message.editMessage(Messages.success("You left your company!")).queue();
                return;
            }

            message.editMessage(Messages.error(String.format("Failed to remove you from your company: `%s`",
                    JsonParser.parseString(apiResponse.body).getAsJsonObject().get("data").getAsJsonObject()
                            .get("message").getAsString()))).queue();
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&leaveCompany", Color.PINK,
                "Removes you from your company.")).build();
    }
}
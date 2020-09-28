package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LeaveCompanyCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public LeaveCompanyCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.cooldown = TimeUnit.SECONDS.toMillis(10);
    }

    @SubCommand(guildOnly = false)
    public void onExecution(CommandEvent event, User user, MessageChannel channel, String[] args) {
        if (channel instanceof TextChannel && !CommandUtil.isInCommandChannel((TextChannel) channel)) {
            return;
        }

        Message message = channel.sendMessage(Messages.pleaseWait()).complete();

        this.apiClient.leaveCompanyAsync(user.getIdLong()).whenComplete((apiResponse, throwable) -> {
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

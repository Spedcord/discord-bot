package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.util.Optional;
import java.util.Set;

public class KickMemberCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public KickMemberCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(args = "<@!?\\d+>")
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!CommandUtil.isInCommandChannel(channel)) {
            return;
        }

        Optional<User> userMention = event.getFirstUserMention();
        if (userMention.isEmpty()) {
            this.onWrongUsage(event, member, channel, args);
            return;
        }

        User user = userMention.get();
        Message message = channel.sendMessage(Messages.pleaseWait()).complete();

        this.apiClient.kickMemberAsync(channel.getGuild().getIdLong(), member.getIdLong(), user.getIdLong())
                .whenComplete((apiResponse, throwable) -> {
                    if (apiResponse.status == 200) {
                        message.editMessage(Messages.success("The member was kicked!")).queue();
                        return;
                    }

                    message.editMessage(Messages.error(String.format("Failed to kick member: `%s`",
                            JsonParser.parseString(apiResponse.body).getAsJsonObject().get("data")
                                    .getAsJsonObject().get("message").getAsString()))).queue();
                });
    }

    @SubCommand(isDefault = true)
    public void onWrongUsage(CommandEvent event, Member member, TextChannel channel, String[] args) {
        channel.sendMessage(Messages.wrongUsage("&kickmember <@user>")).queue();
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&kickmember <@member>", Color.PINK,
                "Removes a member from the company.\n\n**Requires `Spedcord Bot Admin` role**")).build();
    }

}

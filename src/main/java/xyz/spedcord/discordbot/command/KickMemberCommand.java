package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.util.Optional;

public class KickMemberCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public KickMemberCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(args = "<@!?\\d+>")
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!CommandUtil.isBotAdmin(member)) {
            event.respond(Messages.error("You need the `Spedcord Bot Admin` role or `Administrator` permission for this command."));
            return;
        }

        Optional<User> userMention = event.getFirstUserMention();
        if (userMention.isEmpty()) {
            onWrongUsage(event, member, channel, args);
            return;
        }

        User user = userMention.get();
        ApiClient.ApiResponse apiResponse = apiClient.kickMember(channel.getGuild().getIdLong(), user.getIdLong());
        if (apiResponse.status == 200) {
            channel.sendMessage(Messages.success("The member was kicked!")).queue();
            return;
        }

        channel.sendMessage(Messages.error(String.format("Failed to kick member: `%s`",
                JsonParser.parseString(apiResponse.body).getAsJsonObject().get("data")
                        .getAsJsonObject().get("message").getAsString()))).queue();
    }

    @SubCommand(isDefault = true)
    public void onWrongUsage(CommandEvent event, Member member, TextChannel channel, String[] args) {
        channel.sendMessage(Messages.wrongUsage("&kickmember <@user>")).queue();
    }

}

package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.api.Company;
import xyz.spedcord.discordbot.message.Messages;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

public class ProfileCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public ProfileCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(args = "<@!?\\d+>")
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        Optional<? extends IMentionable> mention = event.getFirstMention(Message.MentionType.USER);
        if (mention.isEmpty()) {
            channel.sendMessage(Messages.error("You need to mention a member!")).queue();
            return;
        }

        IMentionable iMentionable = mention.get();
        if (!(iMentionable instanceof User)) {
            channel.sendMessage(Messages.error("Invalid mention: you need to mention a member!")).queue();
            return;
        }

        User user = (User) iMentionable;
        xyz.spedcord.discordbot.api.User userInfo = apiClient.getUserInfo(user.getIdLong(), false);

        if (userInfo == null) {
            channel.sendMessage(Messages.error(user.getAsTag() + " is not registered!")).queue();
            return;
        }

        Company company = userInfo.getCompanyId() == -1 ? null : apiClient.getCompanyInfo(userInfo.getCompanyId());

        channel.sendMessage(new EmbedBuilder()
                .setTitle("Profile of " + user.getAsTag())
                .setDescription(String.format("ID: %d\nCompany: %s", userInfo.getId(),
                        company == null ? "None" : company.getName()))
                .setColor(Color.WHITE)
                .setTimestamp(Instant.now())
                .build()).queue();
    }

    @SubCommand(isDefault = true)
    public void onWrongUsage(CommandEvent event, Member member, TextChannel channel, String[] args) {
        channel.sendMessage(Messages.wrongUsage("&profile <@user>")).queue();
    }

}

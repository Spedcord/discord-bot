package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.api.Company;
import xyz.spedcord.discordbot.message.Messages;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Set;

public class CompanyCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public CompanyCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(isDefault = true)
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        Message message = channel.sendMessage(Messages.pleaseWait()).complete();

        apiClient.getExecutorService().submit(() -> {
            Company companyInfo = apiClient.getCompanyInfo(channel.getGuild().getIdLong());
            if (companyInfo == null) {
                message.editMessage(Messages.error("This server is not a vtc!")).queue();
                return;
            }

            User owner = event.getJDA().getUserById(companyInfo.getOwnerDiscordId());
            message.editMessage(new EmbedBuilder()
                    .setTitle("Company Info")
                    .setDescription("ID: " + companyInfo.getId())
                    .appendDescription("\nName: " + companyInfo.getName())
                    .appendDescription("\nMember: " + companyInfo.getMemberDiscordIds().size())
                    .appendDescription("\nOwner: " + (owner == null ? "Unknown" : owner.getAsTag()))
                    .appendDescription("\nBalance: " + new DecimalFormat("#,###").format(companyInfo.getBalance()) + "$")
                    .setFooter("Requested by " + member.getUser().getAsTag(), member.getUser().getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now())
                    .build()).queue();
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&company", Color.PINK,
                "Shows company related info.")).build();
    }

}

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
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public class CompanyCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public CompanyCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(isDefault = true)
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!CommandUtil.isInCommandChannel(channel)) {
            return;
        }

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
                    .addField("ID", String.valueOf(companyInfo.getId()), true)
                    .addField("Name", companyInfo.getName(), true)
                    .addField("Member", String.valueOf(companyInfo.getMemberDiscordIds().size()), true)
                    .addField("Owner", (owner == null ? "Unknown" : owner.getAsTag()), true)
                    .addField("Balance", "$" + new DecimalFormat("#,###").format(companyInfo.getBalance()), true)
                    .addField("Global ranking", String.valueOf(companyInfo.getRank()), true)
                    .addField("Roles", companyInfo.getRoles().stream()
                            .map(companyRole -> companyRole.getName() + " [" + companyRole.getMemberDiscordIds().size()
                                    + " members]").collect(Collectors.joining(", ")), false)
                    .setFooter("Requested by " + member.getUser().getAsTag(), member.getUser().getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now())
                    .setColor(Color.RED)
                    .build()).queue();
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&company", Color.PINK,
                "Shows company related info.")).build();
    }

}

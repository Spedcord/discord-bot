package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.api.Company;
import xyz.spedcord.discordbot.message.Messages;

import java.awt.*;
import java.util.Set;

public class CreateJoinLinkCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public CreateJoinLinkCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(isDefault = true)
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        Company companyInfo = apiClient.getCompanyInfo(channel.getGuild().getIdLong());
        if (companyInfo == null) {
            channel.sendMessage(Messages.error("This server is not registered as a vtc!")).queue();
            return;
        }

        int maxUses = 1;
        if(args.length >= 1) {
            try {
                maxUses = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                channel.sendMessage(Messages.error("Invalid maxUses parameter!")).queue();
                return;
            }

            if(maxUses <= 1) {
                channel.sendMessage(Messages.error("The maxUses parameter must be higher than 0!")).queue();
                return;
            }
        }

        ApiClient.ApiResponse apiResponse = apiClient.createJoinLink(companyInfo.getId(), maxUses);
        if (apiResponse.status != 200) {
            channel.sendMessage(Messages.error("Failed to create join link, please try again later.")).queue();
            return;
        }

        channel.sendMessage(Messages.success("Your join link was created: " + apiResponse.body.trim())).queue();
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&createjoinlink [maxUses]", Color.PINK,
                "Creates a join link for this vtc. Max uses specifies the max amount of usages for this link.")).build();
    }

}

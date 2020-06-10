package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.api.Company;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;
import xyz.spedcord.discordbot.api.ApiClient;

import java.awt.*;
import java.util.Set;

public class CreateJoinLinkCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public CreateJoinLinkCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(isDefault = true)
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!CommandUtil.isBotAdmin(member)) {
            event.respond(Messages.error("You need the `Spedcord Bot Admin` role or `Administrator` permission for this command."));
            return;
        }

        Message message = channel.sendMessage(Messages.pleaseWait()).complete();

        apiClient.getExecutorService().submit(() -> {
            Company companyInfo = apiClient.getCompanyInfo(channel.getGuild().getIdLong());
            if (companyInfo == null) {
                message.editMessage(Messages.error("This server is not registered as a vtc!")).queue();
                return;
            }

            int maxUses = 1;
            if (args.length >= 1) {
                try {
                    maxUses = Integer.parseInt(args[0]);
                } catch (NumberFormatException ignored) {
                    message.editMessage(Messages.error("Invalid maxUses parameter!")).queue();
                    return;
                }

                if (maxUses < 1) {
                    message.editMessage(Messages.error("The maxUses parameter must be greater than 0!")).queue();
                    return;
                }
            }

            ApiClient.ApiResponse apiResponse = apiClient.createJoinLink(companyInfo.getId(), maxUses);
            if (apiResponse.status != 200) {
                message.editMessage(Messages.error("Failed to create join link, please try again later.")).queue();
                return;
            }

            message.editMessage(Messages.success("Your join link was created: " + apiResponse.body.trim()
                    + "\nThis link can be used " + maxUses + " times.")).queue();
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&createjoinlink [maxUses]", Color.PINK,
                "Creates a join link for this vtc. Max uses specifies the max amount of usages for this link." +
                        "\n\n**Requires `Spedcord Bot Admin` role**")).build();
    }

}

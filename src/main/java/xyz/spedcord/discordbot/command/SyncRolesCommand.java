package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;
import xyz.spedcord.discordbot.util.SyncUtil;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SyncRolesCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public SyncRolesCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.cooldown = TimeUnit.SECONDS.toMillis(30);
    }

    @SubCommand(isDefault = true)
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!CommandUtil.isInCommandChannel(channel)) {
            return;
        }

        if (!CommandUtil.isBotAdmin(member)) {
            event.respond(Messages.error("You need the `Spedcord Bot Admin` role or `Administrator` permission for this command."));
            return;
        }

        channel.sendMessage(Messages.pleaseWait()).queue(message -> {
            this.apiClient.getCompanyInfoAsync(channel.getGuild().getIdLong()).whenComplete((company, throwable) -> {
                if (company == null) {
                    message.editMessage(Messages.error("This server is not a vtc")).queue();
                    return;
                }

                boolean success;
                try {
                    success = SyncUtil.synchronize(company, channel.getGuild());
                } catch (Exception e) {
                    message.editMessage(Messages.error("Failed to sync roles, please notify Spedcord support\n```"
                            + e.getClass().getSimpleName() + ": " + e.getMessage() + "```")).queue();
                    return;
                }

                if (!success) {
                    message.editMessage(Messages.error("Failed to sync roles. Please move the `Spedcord` role to the first position!")).queue();
                    return;
                }
                message.editMessage(Messages.success("The role synchronisation was started! This can take up to 5 minutes.")).queue();
            });
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&syncroles", Color.PINK,
                "Shows the balance of a member.\n\n**Requires `Spedcord Bot Admin` role or `Administrator` permission**")).build();
    }

}

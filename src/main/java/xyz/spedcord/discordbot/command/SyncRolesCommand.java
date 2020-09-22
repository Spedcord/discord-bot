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

public class SyncRolesCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public SyncRolesCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
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

                SyncUtil.synchronize(company, channel.getGuild());

                message.editMessage(Messages.success("The role synchronisation was started! This can take up to 5 minutes.")).queue();
            });
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&balance [@member]", Color.PINK,
                "Shows the balance of a member.")).build();
    }

}

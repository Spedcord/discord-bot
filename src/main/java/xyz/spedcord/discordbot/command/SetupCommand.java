package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.SpedcordDiscordBot;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.api.Company;
import xyz.spedcord.discordbot.api.User;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.util.Set;

public class SetupCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public SetupCommand(ApiClient apiClient) {
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

        Message message = channel.sendMessage(Messages.custom("Please wait", Color.ORANGE, "Please wait while I check some things in the background.")).complete();

        apiClient.getExecutorService().submit(() -> {
            User userInfo = apiClient.getUserInfo(member.getIdLong(), false);
            if (userInfo == null) {
                message.editMessage(Messages.error("You are not registered. [Please register an account.]("
                        + (SpedcordDiscordBot.DEV ? "http://localhost:81" : "https://api.spedcord.xyz") + "/user/register)")).queue();
                return;
            }

            Company companyInfo = apiClient.getCompanyInfo(channel.getGuild().getIdLong());
            if (companyInfo != null) {
                message.editMessage(Messages.success("This server is already a fully functioning vtc!")).queue();
                return;
            }

            if (args.length == 0) {
                message.editMessage(Messages.error("Please specify a name for your vtc!\n`&setup <vtc name>`")).queue();
                return;
            }
            String name = String.join(" ", args);

            if(name.length() >= 24) {
                message.editMessage(Messages.error("The name has to be shorter than 25 characters.")).queue();
                return;
            }
            if(name.length() <= 4) {
                message.editMessage(Messages.error("The name has to be longer than 4 characters.")).queue();
                return;
            }

            ApiClient.ApiResponse apiResponse = apiClient.registerCompany(name, channel.getGuild().getIdLong(), channel.getGuild().getOwnerIdLong());

            message.editMessage(apiResponse.status == 200 ? Messages.success("This server is now registered as a vtc!")
                    : Messages.error("Failed to register server: " + apiResponse.body)).queue();
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&setup", Color.PINK,
                "Turns the server into a vtc.\n\n**Requires `Spedcord Bot Admin` role**")).build();
    }

}

package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.util.Set;

public class ShopCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public ShopCommand(ApiClient apiClient) {
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

        if (args.length != 0) {
            String item = String.join(" ", args);
            System.out.println(item);
            this.handleItemBuy(event, member, channel, item);
            return;
        }

        channel.sendMessage(Messages.custom("Shop", new Color(50, 168, 137),
                "Available items:\n```Custom perma invite: 450,000$```")).queue();
    }

    private void handleItemBuy(CommandEvent event, Member member, TextChannel channel, String item) {
        Message message = channel.sendMessage(Messages.pleaseWait()).complete();
        this.apiClient.getExecutorService().submit(() -> {
            ApiClient.ApiResponse apiResponse = null;
            switch (item.toLowerCase()) {
                case "custom perma invite":
                    //apiResponse = apiClient.buyItem(channel.getGuild().getIdLong(), item);
                    break;
                default:
                    //apiResponse = apiClient.buyItem(channel.getGuild().getIdLong(), item);
                    break;
            }

            if (apiResponse.status == 200) {
                message.editMessage(Messages.success("The item was purchased successfully!")).queue();
                return;
            }

            message.editMessage(Messages.error(String.format("Failed to purchase item: `%s`",
                    JsonParser.parseString(apiResponse.body).getAsJsonObject().get("data")
                            .getAsJsonObject().get("message").getAsString()))).queue();
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&shop [item]", Color.PINK,
                "Shows the shop or buys an item.")).build();
    }

}

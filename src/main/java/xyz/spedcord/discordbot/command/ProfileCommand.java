package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.api.Company;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ProfileCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public ProfileCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.cooldown = TimeUnit.SECONDS.toMillis(10);
    }

    @SubCommand(args = "<@!?\\d+>")
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!CommandUtil.isInCommandChannel(channel)) {
            return;
        }

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
        Message message = channel.sendMessage(Messages.pleaseWait()).complete();

        this.apiClient.getUserInfoAsync(user.getIdLong(), false).whenComplete((userInfo, throwable) -> {
            if (userInfo == null) {
                message.editMessage(Messages.error(user.getAsTag() + " is not registered!")).queue();
                return;
            }

            Company company = userInfo.getCompanyId() == -1 ? null : this.apiClient.getCompanyInfo(userInfo.getCompanyId());

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Profile of " + user.getAsTag())
                    .setDescription(String.format("ID: %d\nCompany: %s", userInfo.getId(),
                            company == null ? "None" : company.getName()))
                    .setThumbnail(user.getEffectiveAvatarUrl())
                    .setColor(Color.WHITE)
                    .setTimestamp(Instant.now());
            if (Arrays.asList(userInfo.getFlags()).contains(xyz.spedcord.discordbot.api.User.Flag.CHEATER)) {
                embedBuilder.addField(":warning: Warning :warning:", "This user is flagged as a cheater!", false);
            }

            message.editMessage(embedBuilder.build()).queue();
        });
    }

    @SubCommand(isDefault = true)
    public void onWrongUsage(CommandEvent event, Member member, TextChannel channel, String[] args) {
        channel.sendMessage(Messages.wrongUsage("&profile <@user>")).queue();
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&profile <@member>", Color.PINK,
                "Shows the Spedcord profile of a member.")).build();
    }

}

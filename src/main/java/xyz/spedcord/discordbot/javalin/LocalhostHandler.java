package xyz.spedcord.discordbot.javalin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lukaesebrot.jal.endpoints.Endpoint;
import io.javalin.http.Context;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import xyz.spedcord.discordbot.SpedcordDiscordBot;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.api.Company;
import xyz.spedcord.discordbot.api.Job;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.settings.GuildSettings;
import xyz.spedcord.discordbot.settings.GuildSettingsProvider;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalhostHandler extends Endpoint {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final JDA jda;
    private final ApiClient apiClient;
    private final GuildSettingsProvider settingsProvider;

    public LocalhostHandler(JDA jda, ApiClient apiClient, GuildSettingsProvider settingsProvider) {
        this.jda = jda;
        this.apiClient = apiClient;
        this.settingsProvider = settingsProvider;
    }

    @Override
    public void handle(Context ctx) {
        if (!ctx.ip().equals("127.0.0.1") && !ctx.ip().equals("192.95.58.241")) {
            ctx.status(401);
            return;
        }

        JsonObject jsonObject = JsonParser.parseString(ctx.body()).getAsJsonObject();
        long userId = jsonObject.get("user").getAsLong();
        User user = this.jda.getUserById(userId);

        ctx.status(200);

        this.executorService.submit(() -> {
            switch (jsonObject.get("event").getAsString()) {
                case "NEW_USER":
                    this.handleNewUser(user, jsonObject);
                    break;
                case "JOB":
                    this.handleJob(jsonObject, userId);
                    break;
                case "USER_JOIN_COMPANY":
                case "USER_LEAVE_COMPANY":
                    this.handleUserLeaveOrJoinCompany(user, jsonObject.get("event").getAsString().contains("JOIN"), jsonObject);
                    break;
                case "USER_ROLE_UPDATE":
                    this.handleUserRoleUpdate(user, jsonObject);
                    break;
                case "WARN":
                    this.handleWarn(user, jsonObject);
                    break;
                case "MOD_LOG":
                    this.handleModLog(user, jsonObject);
                    break;
            }
        });
    }

    private void handleModLog(User user, JsonObject jsonObject) {
        long modChannelId = SpedcordDiscordBot.DEV ? 758534045612900402L : 758525117025484860L;
        TextChannel channel = this.jda.getTextChannelById(modChannelId);

        String msg = jsonObject.get("data").getAsJsonObject().get("msg").getAsString();
        Color color = new Color(jsonObject.get("data").getAsJsonObject().get("color").getAsInt());

        channel.sendMessage(Messages.custom("Mod Log", color, "**" + user.getAsMention() + "**\n\n" + msg)).queue();
    }

    private void handleWarn(User user, JsonObject jsonObject) {
        String msg = jsonObject.get("data").getAsJsonObject().get("msg").getAsString();
        if (msg.equals("CHEATER")) {
            msg = "Your company member " + user.getAsMention() + " has violated Spedcords rules by cheating.";

            user.openPrivateChannel().queue(channel -> channel.sendMessage(Messages.custom("Warning", Color.RED,
                    "You were flagged as a cheater by our moderators. Please contact the staff team if you have any questions.")).queue());
        }

        Company companyInfo = this.apiClient.getCompanyInfo(jsonObject.get("data").getAsJsonObject().get("company").getAsInt());

        GuildSettings guildSettings = this.settingsProvider.getGuildSettings(companyInfo.getDiscordServerId());
        TextChannel textChannel = this.jda.getTextChannelById(guildSettings.getLogChannelId());

        if (textChannel == null) {
            return;
        }

        textChannel.sendMessage(Messages.custom("Warning",
                new Color(255, 170, 0), msg)).queue();
    }

    private void handleUserRoleUpdate(User user, JsonObject object) {
        if (user == null) {
            return;
        }

        Company companyInfo = this.apiClient.getCompanyInfo(object.get("data").getAsJsonObject().get("company").getAsInt());

        GuildSettings guildSettings = this.settingsProvider.getGuildSettings(companyInfo.getDiscordServerId());
        TextChannel textChannel = this.jda.getTextChannelById(guildSettings.getLogChannelId());

        if (textChannel == null) {
            return;
        }

        textChannel.sendMessage(Messages.custom("Member role changed",
                new Color(102, 204, 255), String.format("The user %s is now a member of the role %s.",
                        user.getAsTag(), object.get("data").getAsJsonObject().get("role").getAsString()))).queue();
    }

    private void handleUserLeaveOrJoinCompany(User user, boolean join, JsonObject object) {
        if (user == null) {
            return;
        }

        Company companyInfo = this.apiClient.getCompanyInfo(object.get("data").getAsJsonObject().get("company").getAsInt());

        GuildSettings guildSettings = this.settingsProvider.getGuildSettings(companyInfo.getDiscordServerId());
        TextChannel textChannel = this.jda.getTextChannelById(guildSettings.getLogChannelId());

        if (textChannel == null) {
            return;
        }

        textChannel.sendMessage(Messages.custom(String.format("User %s the company", join ? "joined" : "left"),
                new Color(204, 153, 255), String.format("The user %s just %s the company.", user.getAsTag(),
                        join ? "joined" : "left"))).queue();
    }

    private void handleNewUser(User user, JsonObject jsonObject) {
        if (user == null) {
            return;
        }

        user.openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage(new EmbedBuilder()
                        .setTitle("Welcome to Spedcord!")
                        .setDescription(String.format("Welcome! This is your private key: " +
                                        "||%s|| Please **do not** share this key with anyone!",
                                jsonObject.get("data").getAsJsonObject().get("key").getAsString()))
                        .setColor(Color.WHITE)
                        .setTimestamp(Instant.now())
                        .setFooter("Powered by Spedcord", this.jda.getSelfUser().getEffectiveAvatarUrl())
                        .build()).queue());
    }

    private void handleJob(JsonObject jsonObject, long userId) {
        xyz.spedcord.discordbot.api.User userInfo = this.apiClient.getUserInfo(userId, false);
        Company companyInfo = this.apiClient.getCompanyInfo(userInfo.getCompanyId());
        GuildSettings guildSettings = this.settingsProvider.getGuildSettings(companyInfo.getDiscordServerId());

        if (guildSettings.getLogChannelId() == -1) {
            return;
        }

        TextChannel channel = this.jda.getTextChannelById(guildSettings.getLogChannelId());
        if (channel == null) {
            return;
        }

        User discordUser = this.jda.getUserById(userId);
        if (discordUser == null) {
            return;
        }

        JsonObject jobObj = jsonObject.get("data").getAsJsonObject();
        Job jobData = SpedcordDiscordBot.GSON.fromJson(jobObj, Job.class);
        String state = jobObj.get("state").getAsString();

        switch (state) {
            case "START":
                this.handleJobStart(channel, discordUser, jobData);
                break;
            case "END":
                this.handleJobEnd(channel, discordUser, jobData);
                break;
            case "CANCEL":
                this.handleJobCancel(channel, discordUser, jobData);
                break;
        }
    }

    private void handleJobStart(TextChannel channel, User discordUser, Job jobData) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("Delivery has been started")
                .addField("Driver", discordUser.getAsMention(), false)
                .addField("Route", jobData.getFromCity() + " -> " + jobData.getToCity(), false)
                .addField("Cargo", jobData.getCargo() + " (" + jobData.getCargoWeight() + "t)", false)
                .addField("Truck", jobData.getTruck(), false)
                .setColor(Color.ORANGE)
                .setTimestamp(Instant.now())
                .setFooter("Powered by Spedcord", this.jda.getSelfUser().getEffectiveAvatarUrl())
                .build()).queue();
    }

    private void handleJobEnd(TextChannel channel, User discordUser, Job jobData) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("Delivery has been delivered")
                .addField("Driver", discordUser.getAsMention(), false)
                .addField("Route", jobData.getFromCity() + " -> " + jobData.getToCity(), false)
                .addField("Cargo", jobData.getCargo() + " (" + jobData.getCargoWeight() + "t)", false)
                .addField("Truck", jobData.getTruck(), false)
                .addField("Pay", new DecimalFormat("#,###").format(jobData.getPay()) + "$", false)
                .setColor(Color.GREEN)
                .setTimestamp(Instant.now())
                .setFooter("Powered by Spedcord", this.jda.getSelfUser().getEffectiveAvatarUrl())
                .build()).queue();
    }

    private void handleJobCancel(TextChannel channel, User discordUser, Job jobData) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("Delivery has been cancelled")
                .addField("Driver", discordUser.getAsMention(), false)
                .addField("Route", jobData.getFromCity() + " -> " + jobData.getToCity(), false)
                .addField("Cargo", jobData.getCargo() + " (" + jobData.getCargoWeight() + "t)", false)
                .addField("Truck", jobData.getTruck(), false)
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .setFooter("Powered by Spedcord", this.jda.getSelfUser().getEffectiveAvatarUrl())
                .build()).queue();
    }

}

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
import xyz.spedcord.discordbot.settings.GuildSettings;
import xyz.spedcord.discordbot.settings.GuildSettingsProvider;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.Instant;

public class WebhookHandler extends Endpoint {

    private final JDA jda;
    private final ApiClient apiClient;
    private final GuildSettingsProvider settingsProvider;

    public WebhookHandler(JDA jda, ApiClient apiClient, GuildSettingsProvider settingsProvider) {
        this.jda = jda;
        this.apiClient = apiClient;
        this.settingsProvider = settingsProvider;
    }

    @Override
    public void handle(Context ctx) {
        if (!ctx.ip().equals("127.0.0.1")) {
            ctx.status(401);
            return;
        }

        JsonObject jsonObject = JsonParser.parseString(ctx.body()).getAsJsonObject();
        long userId = jsonObject.get("user").getAsLong();

        switch (jsonObject.get("event").getAsString()) {
            case "NEW_USER":
                User user = jda.getUserById(userId);
                handleNewUser(user, jsonObject);
                break;
            case "JOB":
                handleJob(jsonObject, userId);
                break;
        }
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
                        .build()).queue());
    }

    private void handleJob(JsonObject jsonObject, long userId) {
        xyz.spedcord.discordbot.api.User userInfo = apiClient.getUserInfo(userId, false);
        Company companyInfo = apiClient.getCompanyInfo(userInfo.getCompanyId());
        GuildSettings guildSettings = settingsProvider.getGuildSettings(companyInfo.getDiscordServerId());

        if (guildSettings.getLogChannelId() == -1) {
            return;
        }

        TextChannel channel = jda.getTextChannelById(guildSettings.getLogChannelId());
        if (channel == null) {
            return;
        }

        User discordUser = jda.getUserById(userId);
        if (discordUser == null) {
            return;
        }

        JsonObject jobObj = jsonObject.get("data").getAsJsonObject();
        Job jobData = SpedcordDiscordBot.GSON.fromJson(jobObj, Job.class);
        String state = jobObj.get("state").getAsString();

        switch (state) {
            case "START":
                handleJobStart(channel, discordUser, jobData);
                break;
            case "END":
                handleJobEnd(channel, discordUser, jobData);
                break;
            case "CANCEL":
                handleJobCancel(channel, discordUser, jobData);
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
                .setFooter("Powered by Spedcord", jda.getSelfUser().getEffectiveAvatarUrl())
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
                .setFooter("Powered by Spedcord", jda.getSelfUser().getEffectiveAvatarUrl())
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
                .setFooter("Powered by Spedcord", jda.getSelfUser().getEffectiveAvatarUrl())
                .build()).queue();
    }

}

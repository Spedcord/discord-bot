package xyz.spedcord.discordbot;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.google.gson.*;
import dev.lukaesebrot.jal.endpoints.Endpoint;
import dev.lukaesebrot.jal.endpoints.HttpServer;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import xyz.spedcord.common.config.Config;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.api.Company;
import xyz.spedcord.discordbot.api.Job;
import xyz.spedcord.discordbot.command.*;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.settings.GuildSettings;
import xyz.spedcord.discordbot.settings.GuildSettingsProvider;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SpedcordDiscordBot {

    public static final boolean DEV = false;
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .create();
    public static String KEY = null;

    public void start() throws IOException, LoginException, InterruptedException {
        Config config = new Config(new File("config.cfg"), new String[]{
                "key", "ENTER_A_SECRET_KEY",
                "discord-token", "TOKEN"
        });
        Config guildSettingsConfig = new Config(new File("guildsettings.cfg"));

        KEY = config.get("key");
        String token = config.get("discord-token");

        JDA jda = JDABuilder.create(
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_EMOJIS,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES
        )
                .setToken(token)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
                .awaitReady();

        GuildSettingsProvider settingsProvider = new GuildSettingsProvider(guildSettingsConfig);

        ApiClient apiClient = new ApiClient();

        CommandSettings settings = new CommandSettings("&", jda, true);
        settings.put(new SetupCommand(apiClient), "setup")
                .put(new ProfileCommand(apiClient), "profile")
                .put(new CreateJoinLinkCommand(apiClient), "createjoinlink")
                .put(new SetLogChannelCommand(settingsProvider), "setlogchannel")
                .put(new InfoCommand(settingsProvider), "info")
                .put(new KickMemberCommand(apiClient), "kickmember")
                .put(new CompanyCommand(apiClient), "company")
                .put(new ChangeKeyCommand(apiClient), "changekey")
                .put(new DisplayKeyCommand(apiClient), "displaykey", "showkey", "key")
                .put(new CancelJobCommand(apiClient), "canceljob")
                .put(new HelpCommand(), "help")
                .activate();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            private int idx = 0;

            @Override
            public void run() {
                switch (idx) {
                    case 0:
                        jda.getPresence().setActivity(Activity.listening("&help"));
                        idx++;
                        break;
                    case 1:
                        jda.getPresence().setActivity(Activity.playing("Euro Truck Simulator 2"));
                        idx++;
                        break;
                    case 2:
                        jda.getPresence().setActivity(Activity.playing("American Truck Simulator"));
                        idx++;
                        break;
                    case 3:
                        jda.getPresence().setActivity(Activity.listening("TruckersFM"));
                        idx = 0;
                        break;
                }
            }
        }, 0, 5, TimeUnit.MINUTES);

        startServer(jda, apiClient, settingsProvider);
    }

    private void startServer(JDA jda, ApiClient apiClient, GuildSettingsProvider settingsProvider) {
        HttpServer server = new HttpServer(Javalin.create().start("localhost", 5675));
        server.endpoint("/", HandlerType.POST, new Endpoint() {
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
                        if (user != null) {
                            user.openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(new EmbedBuilder()
                                            .setTitle("Welcome to Spedcord!")
                                            .setDescription(String.format("Welcome! This is your private key: " +
                                                            "||%s|| Please **do not** share this key with anyone!",
                                                    jsonObject.get("data").getAsJsonObject().get("key").getAsString()))
                                            .setColor(Color.WHITE)
                                            .setTimestamp(Instant.now())
                                            .build()).queue());
                        } else {
                            System.out.println("null");
                        }
                        break;
                    case "JOB":
                        xyz.spedcord.discordbot.api.User userInfo = apiClient.getUserInfo(userId, false);
                        Company companyInfo = apiClient.getCompanyInfo(userInfo.getCompanyId());
                        GuildSettings guildSettings = settingsProvider.getGuildSettings(companyInfo.getDiscordServerId());

                        if (guildSettings.getLogChannelId() == -1) {
                            break;
                        }

                        TextChannel channel = jda.getTextChannelById(guildSettings.getLogChannelId());
                        if (channel == null) {
                            break;
                        }

                        User discordUser = jda.getUserById(userId);
                        if (discordUser == null) {
                            break;
                        }

                        JsonObject jobObj = jsonObject.get("data").getAsJsonObject();
                        Job jobData = GSON.fromJson(jobObj, Job.class);
                        String state = jobObj.get("state").getAsString();

                        switch (state) {
                            case "START":
                                channel.sendMessage(Messages.custom("Job started", Color.ORANGE,
                                        String.format("User: %s\n```\n%s -> %s\n%s (%.2ft)\n%s\n```", discordUser.getAsTag(),
                                                jobData.getFromCity(), jobData.getToCity(), jobData.getCargo(),
                                                jobData.getCargoWeight(), jobData.getTruck()))).queue();
                                break;
                            case "END":
                                channel.sendMessage(Messages.custom("Job ended", Color.GREEN,
                                        String.format("User: %s\n```\n%s -> %s\n%s (%.2ft)\n%s\n%.0f\n```", discordUser.getAsTag(),
                                                jobData.getFromCity(), jobData.getToCity(), jobData.getCargo(),
                                                jobData.getCargoWeight(), jobData.getTruck(), jobData.getPay()))).queue();
                                break;
                            case "CANCEL":
                                channel.sendMessage(Messages.custom("Job cancelled", Color.RED,
                                        String.format("User: %s\n```\n%s -> %s\n%s (%.2ft)\n```", discordUser.getAsTag(),
                                                jobData.getFromCity(), jobData.getToCity(), jobData.getCargo(),
                                                jobData.getCargoWeight()))).queue();
                                break;
                        }
                        break;
                }
            }
        });
        System.out.println("Server is listening");
    }

}

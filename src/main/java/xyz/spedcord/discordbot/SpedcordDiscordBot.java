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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import xyz.spedcord.common.config.Config;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.command.*;
import xyz.spedcord.discordbot.settings.GuildSettingsProvider;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SpedcordDiscordBot {

    public static final boolean DEV = true;
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
                .put(new HelpCommand(), "help")
                .activate();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            private int idx = 0;

            @Override
            public void run() {
                switch (idx) {
                    case 0:
                        jda.getPresence().setActivity(Activity.listening("!help"));
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

        startServer(jda);
    }

    private void startServer(JDA jda) {
        HttpServer server = new HttpServer(Javalin.create().start("localhost", 82));
        server.endpoint("/", HandlerType.POST, new Endpoint() {
            @Override
            public void handle(Context ctx) {
                if (!ctx.ip().equals("127.0.0.1")) {
                    ctx.status(401);
                    return;
                }

                JsonObject jsonObject = JsonParser.parseString(ctx.body()).getAsJsonObject();
                switch (jsonObject.get("event").getAsString()) {
                    case "NEW_USER":
                        long userId = jsonObject.get("user").getAsLong();
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
                        //TODO
                        break;
                }
            }
        });
        System.out.println("Server is listening");
    }

}

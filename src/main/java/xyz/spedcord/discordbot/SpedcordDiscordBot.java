package xyz.spedcord.discordbot;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import dev.lukaesebrot.jal.endpoints.HttpServer;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import xyz.spedcord.common.config.Config;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.command.*;
import xyz.spedcord.discordbot.javalin.KoFiHandler;
import xyz.spedcord.discordbot.javalin.LocalhostHandler;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.settings.GuildSettingsProvider;
import xyz.spedcord.discordbot.util.CommandUtil;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpedcordDiscordBot {

    public static final boolean DEV = System.getenv("SPEDCORD_DEV") != null
            && System.getenv("SPEDCORD_DEV").equals("true");
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
                .addEventListeners(new ListenerAdapter() {
                    @Override
                    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
                        event.getGuild().getSystemChannel().sendMessage(Messages.custom("Welcome to Spedcord",
                                Color.GREEN, "To use Spedcord commands you need to set the bot channel with " +
                                        "`&setBotChannel`.\nIf you need help [join our Discord](http://discord.spedcord.xyz) " +
                                        "or take a look at our [guide](https://spedcord.xyz/guide).")).queue();
                    }
                })
                .build()
                .awaitReady();
        Messages.setJda(jda);

        GuildSettingsProvider settingsProvider = new GuildSettingsProvider(guildSettingsConfig);
        CommandUtil.init(settingsProvider);

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
                .put(new BalanceCommand(apiClient), "balance")
                .put(new LeaveCompanyCommand(apiClient), "leavecompany")
                .put(new SetCommandChannelCommand(settingsProvider), "setcommandchannel", "setbotchannel")
                .put(new SyncRolesCommand(apiClient), "syncroles")
                .put(new RoleCommand(), "role", "roles")
                //.put(new ShopCommand(apiClient), "shop")
                .put(new HelpCommand(), "help")
                .setCooldownMessage(() -> new MessageBuilder()
                        .setEmbed(Messages.error("This command is currently on " +
                                "cooldown, please wait a few seconds."))
                        .build())
                .activate();

        Runtime.getRuntime().addShutdownHook(new Thread(jda::shutdown));

        this.startActivityTask(jda);
        this.startServer(jda, apiClient, settingsProvider);
        this.readInput();
    }

    private void startActivityTask(JDA jda) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            private int idx = 0;

            @Override
            public void run() {
                switch (this.idx) {
                    case 0:
                        jda.getPresence().setActivity(Activity.listening("&help"));
                        this.idx++;
                        break;
                    case 1:
                        jda.getPresence().setActivity(Activity.playing("Euro Truck Simulator 2"));
                        this.idx++;
                        break;
                    case 2:
                        jda.getPresence().setActivity(Activity.playing("American Truck Simulator"));
                        this.idx++;
                        break;
                    case 3:
                        jda.getPresence().setActivity(Activity.listening("TruckersFM"));
                        this.idx = 0;
                        break;
                }
            }
        }, 0, 5, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    private void startServer(JDA jda, ApiClient apiClient, GuildSettingsProvider settingsProvider) {
        Javalin app = Javalin.create().start(DEV ? "localhost" : "192.95.58.241", 5675);
        HttpServer server = new HttpServer(app);
        server.endpoint("/", HandlerType.POST, new LocalhostHandler(jda, apiClient, settingsProvider));
        server.endpoint("/kofi", HandlerType.POST, new KoFiHandler(jda));
        System.out.println("Server is listening");

        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }

    private void readInput() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String s;
        try {
            while ((s = reader.readLine()) != null) {
                if (s.toLowerCase().matches("q|quit|exit|stop|shutdown")) {
                    System.out.println("Goodbye");
                    reader.close();
                    System.exit(0);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

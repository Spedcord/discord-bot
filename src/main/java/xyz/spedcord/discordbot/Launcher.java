package xyz.spedcord.discordbot;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Launcher {
    public static void main(String[] args) throws LoginException, InterruptedException, IOException {
        SpedcordDiscordBot spedcordDiscordBot = new SpedcordDiscordBot();
        spedcordDiscordBot.start();
    }
}

package xyz.spedcord.discordbot.message;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;

public class Messages {

    private Messages() {
    }

    public static MessageEmbed error(String desc) {
        return custom("Error", Color.RED, desc);
    }

    public static MessageEmbed wrongUsage(String correctUsage) {
        return custom("Wrong usage", Color.RED, String.format("Correct usage:\n```%s```", correctUsage));
    }

    public static MessageEmbed success(String desc) {
        return custom("Success", Color.GREEN, desc);
    }

    public static MessageEmbed pleaseWait() {
        return pleaseWait("");
    }

    public static MessageEmbed pleaseWait(String desc) {
        return custom("Please wait", Color.ORANGE, desc);
    }

    public static MessageEmbed custom(String title, Color color, String desc) {
        return new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .setDescription(desc)
                .setTimestamp(Instant.now())
                .build();
    }

}

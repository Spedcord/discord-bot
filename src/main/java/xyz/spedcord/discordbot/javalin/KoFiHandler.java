package xyz.spedcord.discordbot.javalin;

import dev.lukaesebrot.jal.endpoints.Endpoint;
import io.javalin.http.Context;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.SpedcordDiscordBot;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class KoFiHandler extends Endpoint {

    private final TextChannel textChannel;

    public KoFiHandler(JDA jda) {
        this.textChannel = jda.getTextChannelById(718458279185088612L);
    }

    @Override
    public void handle(Context ctx) {
        // Ko-Fi ip = 104.45.229.169
        if(!ctx.ip().equals("104.45.229.169")) {
            ctx.status(401);
            return;
        }

        String bodyStr = URLDecoder.decode(ctx.body(), StandardCharsets.UTF_8);

        KoFiRequestBody body;
        try {
            body = SpedcordDiscordBot.GSON.fromJson(bodyStr.substring(5), KoFiRequestBody.class);
        } catch (Exception ignored) {
            ctx.status(400);
            return;
        }

        if(!body.verify()) {
            ctx.status(400);
            return;
        }

        textChannel.sendMessage(new EmbedBuilder()
                .setTitle("New donation")
                .addField("From", body.from_name, false)
                .addField("Amount", String.format("%.2f", body.amount), false)
                .addField("Message", body.message, false)
                .setColor(Color.ORANGE)
                .setTimestamp(Instant.now())
                .build()).queue();
        ctx.status(200);
    }

    private static class KoFiRequestBody {
        public String message_id;
        public String timestamp;
        public String type;
        public String from_name;
        public String message;
        public String url;
        public double amount;

        public KoFiRequestBody(String message_id, String timestamp, String type, String from_name, String message, String url, double amount) {
            this.message_id = message_id;
            this.timestamp = timestamp;
            this.type = type;
            this.from_name = from_name;
            this.message = message;
            this.url = url;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return "KoFiRequestBody{" +
                    "message_id='" + message_id + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    ", type='" + type + '\'' +
                    ", from_name='" + from_name + '\'' +
                    ", message='" + message + '\'' +
                    ", url='" + url + '\'' +
                    ", amount=" + amount +
                    '}';
        }

        public boolean verify() {
            return message != null && from_name != null;
        }
    }
}

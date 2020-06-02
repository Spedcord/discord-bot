package xyz.spedcord.discordbot.api;

import xyz.spedcord.discordbot.SpedcordDiscordBot;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {

    private static final String API_URL = "https://api.spedcord.xyz";
    private static final String DEV_API_URL = "http://localhost:81";

    public User getUserInfo(long discordId, boolean elevated) {
        ApiResponse response = makeRequestSilent("/user/" + (elevated ? "get" : "info") + "/" + discordId,
                "GET", new HashMap<>(), new HashMap<>() {
                    {
                        put("Authorization", "");
                    }
                }, "");

        if (response == null || response.status != 200) {
            return null;
        }
        return SpedcordDiscordBot.GSON.fromJson(response.body, User.class);
    }

    public Company getCompanyInfo(long discordId) {
        ApiResponse response = makeRequestSilent("/company/info?discordServerId=" + discordId,
                "GET", new HashMap<>(), new HashMap<>(), "");

        if (response == null || response.status != 200) {
            return null;
        }
        return SpedcordDiscordBot.GSON.fromJson(response.body, Company.class);
    }

    public Company getCompanyInfo(int id) {
        ApiResponse response = makeRequestSilent("/company/info?id=" + id,
                "GET", new HashMap<>(), new HashMap<>(), "");

        if (response == null || response.status != 200) {
            return null;
        }
        return SpedcordDiscordBot.GSON.fromJson(response.body, Company.class);
    }

    public ApiResponse registerCompany(String name, long serverId, long ownerId) {
        return makeRequestSilent("/company/register", "POST", new HashMap<>(), new HashMap<>() {
            {
                put("Authorization", "");
            }
        }, SpedcordDiscordBot.GSON.toJson(new Company(-1, serverId, name, ownerId, new ArrayList<>())));
    }

    public ApiResponse createJoinLink(int companyId, int maxUses) {
        return makeRequestSilent("/company/createjoinlink/" + companyId + "?maxUses=" + maxUses,
                "POST", new HashMap<>(), new HashMap<>() {
                    {
                        put("Authorization", "");
                    }
                }, "");
    }

    public ApiResponse kickMember(long companyDiscordId, long userDiscordId) {
        return makeRequestSilent("/company/kickmember",
                "POST", new HashMap<>() {
                    {
                        put("companyDiscordId", String.valueOf(companyDiscordId));
                        put("userDiscordId", String.valueOf(userDiscordId));
                    }
                }, new HashMap<>() {
                    {
                        put("Authorization", "");
                    }
                }, "");
    }

    public ApiResponse changeUserKey(long userDiscordId) {
        return makeRequestSilent("/user/changekey",
                "POST", new HashMap<>() {
                    {
                        put("discordId", String.valueOf(userDiscordId));
                    }
                }, new HashMap<>() {
                    {
                        put("Authorization", "");
                    }
                }, "");
    }

    public ApiResponse cancelJob(long userDiscordId) {
        String key = getUserInfo(userDiscordId, true).getKey();
        return makeRequestSilent("/job/cancel",
                "POST", new HashMap<>() {
                    {
                        put("discordId", String.valueOf(userDiscordId));
                        put("key", key);
                    }
                }, new HashMap<>() {
                    {
                        put("Authorization", "");
                    }
                }, "");
    }

    private ApiResponse makeRequestSilent(String path, String method, Map<String, String> params, Map<String, String> header, String body) {
        try {
            return makeRequest(path, method, params, header, body);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ApiResponse makeRequest(String path, String method, Map<String, String> params, Map<String, String> header, String body) throws IOException {
        StringBuilder queryBuilder = new StringBuilder("?");
        params.forEach((s, s2) -> queryBuilder.append(s).append("=").append(s2).append("&"));
        queryBuilder.deleteCharAt(queryBuilder.length() - 1);

        if (header.containsKey("Authorization")) {
            header.put("Authorization", "Bearer " + SpedcordDiscordBot.KEY);
        }

        URL url = new URL((SpedcordDiscordBot.DEV ? DEV_API_URL : API_URL) + path + queryBuilder.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        header.forEach(connection::setRequestProperty);

        connection.setDoInput(true);
        if (!body.equals("")) {
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(body.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
        } catch (Exception e) {
            inputStream = connection.getErrorStream();
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder responseBodyBuilder = new StringBuilder();
        String s;
        while ((s = bufferedReader.readLine()) != null)
            responseBodyBuilder.append(s).append('\n');

        return new ApiResponse(connection.getResponseCode(), responseBodyBuilder.toString().trim());
    }

    private boolean isBad(int responseCode) {
        return responseCode == 400 || responseCode == 404 || responseCode == 401;
    }

    public static class ApiResponse {
        public int status;
        public String body;

        public ApiResponse() {
            this(200, "");
        }

        public ApiResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}

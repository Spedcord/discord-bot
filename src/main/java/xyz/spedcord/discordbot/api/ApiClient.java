package xyz.spedcord.discordbot.api;

import xyz.spedcord.discordbot.SpedcordDiscordBot;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    private static final String API_URL = "https://api.spedcord.xyz";
    private static final String DEV_API_URL = "http://localhost:81";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ApiClient() {
    }

    public CompletableFuture<User> getUserInfoAsync(long discordId, boolean elevated) {
        CompletableFuture<User> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            User userInfo = this.getUserInfo(discordId, elevated);
            future.complete(userInfo);
        });
        return future;
    }

    @Deprecated(since = "2.1.0")
    public User getUserInfo(long discordId, boolean elevated) {
        ApiResponse response = this.makeRequestSilent("/user/" + (elevated ? "get" : "info") + "/" + discordId,
                "GET", new HashMap<>(), new HashMap<>() {
                    {
                        this.put("Authorization", "");
                    }
                }, "");

        if (response == null || response.status != 200) {
            return null;
        }
        return SpedcordDiscordBot.GSON.fromJson(response.body, User.class);
    }

    public CompletableFuture<Company> getCompanyInfoAsync(long discordId) {
        CompletableFuture<Company> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            Company companyInfo = this.getCompanyInfo(discordId);
            future.complete(companyInfo);
        });
        return future;
    }

    @Deprecated(since = "2.1.0")
    public Company getCompanyInfo(long discordId) {
        ApiResponse response = this.makeRequestSilent("/company/info?discordServerId=" + discordId,
                "GET", new HashMap<>(), new HashMap<>(), "");

        if (response == null || response.status != 200) {
            return null;
        }
        return SpedcordDiscordBot.GSON.fromJson(response.body, Company.class);
    }

    public CompletableFuture<Company> getCompanyInfoAsync(int id) {
        CompletableFuture<Company> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            Company companyInfo = this.getCompanyInfo(id);
            future.complete(companyInfo);
        });
        return future;
    }

    @Deprecated(since = "2.1.0")
    public Company getCompanyInfo(int id) {
        ApiResponse response = this.makeRequestSilent("/company/info?id=" + id,
                "GET", new HashMap<>(), new HashMap<>(), "");

        if (response == null || response.status != 200) {
            return null;
        }
        return SpedcordDiscordBot.GSON.fromJson(response.body, Company.class);
    }

    public CompletableFuture<ApiResponse> registerCompanyAsync(String name, long serverId, long ownerId) {
        CompletableFuture<ApiResponse> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            ApiResponse apiResponse = this.registerCompany(name, serverId, ownerId);
            future.complete(apiResponse);
        });
        return future;
    }

    @Deprecated(since = "2.1.0")
    public ApiResponse registerCompany(String name, long serverId, long ownerId) {
        return this.makeRequestSilent("/company/register", "POST", new HashMap<>(), new HashMap<>() {
            {
                this.put("Authorization", "");
            }
        }, SpedcordDiscordBot.GSON.toJson(new Company(-1, serverId, name, ownerId, 0, 0, new ArrayList<>(), new ArrayList<>(), "")));
    }

    public CompletableFuture<ApiResponse> createJoinLinkAsync(int companyId, int maxUses, String customId) {
        CompletableFuture<ApiResponse> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            ApiResponse apiResponse = this.createJoinLink(companyId, maxUses, customId);
            future.complete(apiResponse);
        });
        return future;
    }

    @Deprecated(since = "2.1.0")
    public ApiResponse createJoinLink(int companyId, int maxUses, String customId) {
        return this.makeRequestSilent("/company/createjoinlink/" + companyId + "?maxUses="
                        + maxUses + (customId == null ? "" : "&customId=" + customId),
                "POST", new HashMap<>(), new HashMap<>() {
                    {
                        this.put("Authorization", "");
                    }
                }, "");
    }

    public CompletableFuture<ApiResponse> kickMemberAsync(long companyDiscordId, long kickerDiscordId, long userDiscordId) {
        CompletableFuture<ApiResponse> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            ApiResponse apiResponse = this.kickMember(companyDiscordId, kickerDiscordId, userDiscordId);
            future.complete(apiResponse);
        });
        return future;
    }

    @Deprecated(since = "2.1.0")
    public ApiResponse kickMember(long companyDiscordId, long kickerDiscordId, long userDiscordId) {
        return this.makeRequestSilent("/company/member/kick",
                "POST", new HashMap<>() {
                    {
                        this.put("companyDiscordId", String.valueOf(companyDiscordId));
                        this.put("kickerDiscordId", String.valueOf(kickerDiscordId));
                        this.put("userDiscordId", String.valueOf(userDiscordId));
                    }
                }, new HashMap<>() {
                    {
                        this.put("Authorization", "");
                    }
                }, "");
    }

    public CompletableFuture<ApiResponse> changeUserKeyAsync(long userDiscordId) {
        CompletableFuture<ApiResponse> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            ApiResponse apiResponse = this.changeUserKey(userDiscordId);
            future.complete(apiResponse);
        });
        return future;
    }

    @Deprecated(since = "2.1.0")
    public ApiResponse changeUserKey(long userDiscordId) {
        return this.makeRequestSilent("/user/changekey",
                "POST", new HashMap<>() {
                    {
                        this.put("discordId", String.valueOf(userDiscordId));
                    }
                }, new HashMap<>() {
                    {
                        this.put("Authorization", "");
                    }
                }, "");
    }

    public CompletableFuture<ApiResponse> cancelJobAsync(long userDiscordId) {
        CompletableFuture<ApiResponse> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            ApiResponse apiResponse = this.cancelJob(userDiscordId);
            future.complete(apiResponse);
        });
        return future;
    }

    @Deprecated(since = "2.1.0")
    public ApiResponse cancelJob(long userDiscordId) {
        String key = this.getUserInfo(userDiscordId, true).getKey();
        return this.makeRequestSilent("/job/cancel",
                "POST", new HashMap<>() {
                    {
                        this.put("discordId", String.valueOf(userDiscordId));
                        this.put("key", key);
                    }
                }, new HashMap<>() {
                    {
                        this.put("Authorization", "");
                    }
                }, "");
    }

    public CompletableFuture<ApiResponse> leaveCompanyAsync(long userDiscordId) {
        CompletableFuture<ApiResponse> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            ApiResponse apiResponse = this.leaveCompany(userDiscordId);
            future.complete(apiResponse);
        });
        return future;
    }

    @Deprecated(since = "2.1.0")
    public ApiResponse leaveCompany(long userDiscordId) {
        return this.makeRequestSilent("/user/leavecompany",
                "POST", new HashMap<>() {
                    {
                        this.put("discordId", String.valueOf(userDiscordId));
                    }
                }, new HashMap<>() {
                    {
                        this.put("Authorization", "");
                    }
                }, "");
    }

    private ApiResponse makeRequestSilent(String path, String method, Map<String, String> params, Map<String, String> header, String body) {
        try {
            return this.makeRequest(path, method, params, header, body);
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
        while ((s = bufferedReader.readLine()) != null) {
            responseBodyBuilder.append(s).append('\n');
        }
        bufferedReader.close();

        return new ApiResponse(connection.getResponseCode(), responseBodyBuilder.toString().trim());
    }

    private boolean isBad(int responseCode) {
        return responseCode == 400 || responseCode == 404 || responseCode == 401;
    }

    public ExecutorService getExecutorService() {
        return this.executorService;
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

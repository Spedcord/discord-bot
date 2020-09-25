package xyz.spedcord.discordbot.api;

import java.util.List;

public class User {

    private final int id;
    private final long discordId;
    private final double balance;
    private final List<Integer> jobList;
    private String key;
    private int companyId;
    private final Flag[] flags;

    public User(int id, long discordId, String key, int companyId, double balance, List<Integer> jobList, Flag[] flags) {
        this.id = id;
        this.discordId = discordId;
        this.key = key;
        this.companyId = companyId;
        this.balance = balance;
        this.jobList = jobList;
        this.flags = flags;
    }

    public int getId() {
        return this.id;
    }

    public long getDiscordId() {
        return this.discordId;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getCompanyId() {
        return this.companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public List<Integer> getJobList() {
        return this.jobList;
    }

    public double getBalance() {
        return this.balance;
    }

    public Flag[] getFlags() {
        return this.flags;
    }

    public enum Flag {
        CHEATER,
        DONOR,
        EARLY_BIRD
    }
}

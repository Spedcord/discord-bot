package xyz.spedcord.discordbot.api;

import java.util.List;

public class User {

    private final int id;
    private final long discordId;
    private String key;
    private int companyId;
    private final double balance;
    private final List<Integer> jobList;
    private Flag[] flags;

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
        return id;
    }

    public long getDiscordId() {
        return discordId;
    }

    public String getKey() {
        return key;
    }

    public int getCompanyId() {
        return companyId;
    }

    public List<Integer> getJobList() {
        return jobList;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getBalance() {
        return balance;
    }

    public Flag[] getFlags() {
        return flags;
    }

    public enum Flag {
        CHEATER
    }
}

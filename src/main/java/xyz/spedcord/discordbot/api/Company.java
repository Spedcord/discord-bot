package xyz.spedcord.discordbot.api;

import java.util.List;

public class Company {

    private final int id;
    private final long discordServerId;
    private String name;
    private final long ownerDiscordId;
    private final double balance;
    private final List<Long> memberDiscordIds;

    public Company(int id, long discordServerId, String name, long ownerDiscordId, double balance, List<Long> memberDiscordIds) {
        this.id = id;
        this.discordServerId = discordServerId;
        this.name = name;
        this.ownerDiscordId = ownerDiscordId;
        this.balance = balance;
        this.memberDiscordIds = memberDiscordIds;
    }

    public int getId() {
        return id;
    }

    public long getDiscordServerId() {
        return discordServerId;
    }

    public String getName() {
        return name;
    }

    public long getOwnerDiscordId() {
        return ownerDiscordId;
    }

    public List<Long> getMemberDiscordIds() {
        return memberDiscordIds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }
}

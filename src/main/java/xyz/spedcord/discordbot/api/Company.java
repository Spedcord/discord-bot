package xyz.spedcord.discordbot.api;

import java.util.List;

public class Company {

    private final int id;
    private final long discordServerId;
    private String name;
    private final long ownerDiscordId;
    private final double balance;
    public final int rank;
    private final List<Long> memberDiscordIds;
    private final List<CompanyRole> roles;
    private String defaultRole;

    public Company(int id, long discordServerId, String name, long ownerDiscordId, double balance, int rank, List<Long> memberDiscordIds, List<CompanyRole> roles, String defaultRole) {
        this.id = id;
        this.discordServerId = discordServerId;
        this.name = name;
        this.ownerDiscordId = ownerDiscordId;
        this.balance = balance;
        this.rank = rank;
        this.memberDiscordIds = memberDiscordIds;
        this.roles = roles;
        this.defaultRole = defaultRole;
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

    public int getRank() {
        return rank;
    }

    public String getDefaultRole() {
        return defaultRole;
    }

    public List<CompanyRole> getRoles() {
        return roles;
    }

}

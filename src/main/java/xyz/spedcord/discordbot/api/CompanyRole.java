package xyz.spedcord.discordbot.api;

import java.util.List;

public class CompanyRole {

    private String name;
    private double payout;
    private final List<Long> memberDiscordIds;

    public CompanyRole(String name, double payout, List<Long> memberDiscordIds) {
        this.name = name;
        this.payout = payout;
        this.memberDiscordIds = memberDiscordIds;
    }

    public String getName() {
        return name;
    }

    public double getPayout() {
        return payout;
    }

    public List<Long> getMemberDiscordIds() {
        return memberDiscordIds;
    }

}

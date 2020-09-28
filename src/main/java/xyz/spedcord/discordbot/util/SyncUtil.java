package xyz.spedcord.discordbot.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import xyz.spedcord.discordbot.api.Company;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SyncUtil {

    private SyncUtil() {
    }

    public static boolean synchronize(Company company, Guild guild) {
        Color specialColor = new Color(137, 138, 139);
        int highestPos = guild.getRoles().stream()
                .filter(role -> role.getColor() != null)
                .filter(role -> role.getColor().getRGB() == specialColor.getRGB())
                .map(Role::getPosition)
                .max(Comparator.comparingInt(value -> (int) value))
                .orElse(-1);
        int selfHighest = guild.getSelfMember().getRoles().stream()
                .map(Role::getPosition)
                .max(Comparator.comparingInt(value -> (int) value))
                .orElse(-1);

        if (highestPos > selfHighest) {
            return false;
        }

        List<Role> guildRoles = new ArrayList<>(guild.getRoles());
        company.getRoles().forEach(companyRole -> {
            if (guildRoles.stream().noneMatch(role -> role.getColor() != null
                    && role.getColor().getRGB() == specialColor.getRGB()
                    && role.getName().equals(companyRole.getName()))) {
                guild.createRole()
                        .setName(companyRole.getName())
                        .setColor(specialColor)
                        .queue(role -> companyRole.getMemberDiscordIds().forEach(id -> guild.addRoleToMember(id, role).queue()));
            } else {
                Role role = guildRoles.stream()
                        .filter(_role -> _role.getName().equals(companyRole.getName()))
                        .filter(_role -> _role.getColor() != null)
                        .filter(_role -> _role.getColor().getRGB() == specialColor.getRGB())
                        .findFirst().get();
                List<Long> roleMemberIds = new ArrayList<>(companyRole.getMemberDiscordIds());

                guild.getMembersWithRoles(role).forEach(roleMember -> {
                    if (roleMemberIds.contains(roleMember.getIdLong())) {
                        roleMemberIds.remove(roleMember.getIdLong());
                    } else {
                        guild.removeRoleFromMember(roleMember, role).queue();
                    }
                });

                roleMemberIds.forEach(id -> guild.addRoleToMember(id, role).queue());
            }
        });

        guildRoles.stream()
                .filter(role -> role.getColor() != null)
                .filter(role -> role.getColor().getRGB() == specialColor.getRGB())
                .filter(role -> company.getRoles().stream().noneMatch(companyRole -> companyRole.getName().equals(role.getName())))
                .forEach(role -> role.delete().queue());
        return true;
    }

}

package xyz.spedcord.discordbot.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import xyz.spedcord.discordbot.api.Company;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SyncUtil {

    private SyncUtil() {
    }

    public static void synchronize(Company company, Guild guild) {
        List<Role> guildRoles = new ArrayList<>(guild.getRoles());
        company.getRoles().forEach(companyRole -> {
            if (guildRoles.stream().noneMatch(role -> role.getName().equals(companyRole.getName()))) {
                guild.createRole()
                        .setName(companyRole.getName())
                        .setColor(new Color(137, 138, 139))
                        .queue(role -> companyRole.getMemberDiscordIds().forEach(id -> guild.addRoleToMember(id, role).queue()));
            } else {
                Role role = guildRoles.stream().filter(_role -> _role.getName().equals(companyRole.getName())).findFirst().get();
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
                .filter(role -> role.getColor().getRed() == 137 && role.getColor().getGreen() == 136 && role.getColor().getBlue() == 139)
                .filter(role -> company.getRoles().stream().noneMatch(companyRole -> companyRole.getName().equals(role.getName())))
                .forEach(role -> role.delete().queue());
    }

}

package xyz.spedcord.discordbot.command;

import com.github.johnnyjayjay.discord.commandapi.AbstractCommand;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.SubCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.spedcord.discordbot.api.ApiClient;
import xyz.spedcord.discordbot.api.Company;
import xyz.spedcord.discordbot.message.Messages;
import xyz.spedcord.discordbot.util.CommandUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SyncRolesCommand extends AbstractCommand {

    private final ApiClient apiClient;

    public SyncRolesCommand(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @SubCommand(isDefault = true)
    public void onExecution(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!CommandUtil.isInCommandChannel(channel)) {
            return;
        }

        if (!CommandUtil.isBotAdmin(member)) {
            event.respond(Messages.error("You need the `Spedcord Bot Admin` role or `Administrator` permission for this command."));
            return;
        }

        channel.sendMessage(Messages.pleaseWait()).queue(message -> {
            Company company = this.apiClient.getCompanyInfo(channel.getGuild().getIdLong());
            if (company == null) {
                message.editMessage(Messages.error("This server is not a vtc")).queue();
                return;
            }

            List<Role> guildRoles = new ArrayList<>(channel.getGuild().getRoles());
            company.getRoles().forEach(companyRole -> {
                if (guildRoles.stream().noneMatch(role -> role.getName().equals(companyRole.getName()))) {
                    channel.getGuild().createRole()
                            .setName(companyRole.getName())
                            .setColor(new Color(137, 138, 139))
                            .queue(role -> companyRole.getMemberDiscordIds().forEach(id -> channel.getGuild().addRoleToMember(id, role).queue()));
                } else {
                    Role role = guildRoles.stream().filter(_role -> _role.getName().equals(companyRole.getName())).findFirst().get();
                    List<Long> roleMemberIds = new ArrayList<>(companyRole.getMemberDiscordIds());

                    channel.getGuild().getMembersWithRoles(role).forEach(roleMember -> {
                        if (roleMemberIds.contains(roleMember.getIdLong())) {
                            roleMemberIds.remove(roleMember.getIdLong());
                        } else {
                            channel.getGuild().removeRoleFromMember(roleMember, role).queue();
                        }
                    });

                    roleMemberIds.forEach(id -> channel.getGuild().addRoleToMember(id, role).queue());
                }
            });

            guildRoles.stream()
                    .filter(role -> role.getColor() != null)
                    .filter(role -> role.getColor().getRed() == 137 && role.getColor().getGreen() == 136 && role.getColor().getBlue() == 139)
                    .filter(role -> company.getRoles().stream().noneMatch(companyRole -> companyRole.getName().equals(role.getName())))
                    .forEach(role -> role.delete().queue());

            message.editMessage(Messages.success("The role synchronisation was started! This can take up to 5 minutes.")).queue();
        });
    }

    @Override
    public Message info(Member member, String prefix, Set<String> labels) {
        return new MessageBuilder().setEmbed(Messages.custom("&balance [@member]", Color.PINK,
                "Shows the balance of a member.")).build();
    }

}

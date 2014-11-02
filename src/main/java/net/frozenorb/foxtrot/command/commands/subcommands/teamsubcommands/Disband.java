package net.frozenorb.foxtrot.command.commands.subcommands.teamsubcommands;

import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.command.annotations.Command;
import net.frozenorb.foxtrot.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by chasechocolate.
 */
public class Disband {

    @Command(names={ "team disband", "t disband", "f disband", "faction disband", "fac disband" }, permissionNode="")
    public static void teamDisband(Player player) {
        Team team = FoxtrotPlugin.getInstance().getTeamHandler().getPlayerTeam(player.getName());

        if(team == null){
            player.sendMessage(ChatColor.RED + "You are not on a team!");
            return;
        }

        //Owner check
        if(!(team.isOwner(player.getName()))){
            player.sendMessage(ChatColor.RED + "You must be the leader of the team to disband it!");
            return;
        }

        if (team.isRaidable()) {
            player.sendMessage(ChatColor.RED + "You cannot disband your team while raidable.");
            return;
        }

        //Disband team
        for(Player online : team.getOnlineMembers()){
            online.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + player.getName() + " has disbanded the team.");
        }

        FoxtrotPlugin.getInstance().getTeamHandler().removeTeam(team.getName());
    }

}
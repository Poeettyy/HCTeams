package net.frozenorb.foxtrot.team.commands.team;

import lombok.Getter;
import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.command.annotations.Command;
import net.frozenorb.foxtrot.command.annotations.Param;
import net.frozenorb.foxtrot.teamactiontracker.TeamActionTracker;
import net.frozenorb.foxtrot.team.Team;
import net.frozenorb.foxtrot.teamactiontracker.enums.TeamActionType;
import net.frozenorb.foxtrot.util.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by macguy8 on 11/2/2014.
 */
public class TeamMuteCommand {

    @Getter private static Map<String, String> teamMutes = new HashMap<String, String>();

    @Command(names={ "team mute", "t mute", "f mute", "faction mute", "fac mute" }, permissionNode="foxtrot.mutefaction")
    public static void teamMute(Player sender, @Param(name="team") final Team target, @Param(name="minutes") String time, @Param(name="reason", wildcard=true) String reason) {
        int timeSeconds = Integer.valueOf(time) * 60;

        for (String player : target.getMembers()) {
            teamMutes.put(player, target.getName());

            Player bukkitPlayer = FoxtrotPlugin.getInstance().getServer().getPlayerExact(player);

            if (bukkitPlayer != null) {
                bukkitPlayer.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Your team has been muted for " + TimeUtils.getDurationBreakdown(timeSeconds * 1000L) + " for " + reason + ".");
            }
        }

        TeamActionTracker.logAction(target, TeamActionType.GENERAL, "Mute: Team mute added. [Duration: " + time + ", Muted by: " + sender.getName() + "]");

        new BukkitRunnable() {

            public void run() {
                TeamActionTracker.logAction(target, TeamActionType.GENERAL, "Mute: Team mute expired.");

                Iterator<Map.Entry<String, String>> mutesIterator = teamMutes.entrySet().iterator();

                while (mutesIterator.hasNext()) {
                    Map.Entry<String, String> mute = mutesIterator.next();

                    if (mute.getValue().equalsIgnoreCase(target.getName())) {
                        Player bukkitPlayer = FoxtrotPlugin.getInstance().getServer().getPlayerExact(mute.getKey());

                        if (bukkitPlayer != null) {
                            bukkitPlayer.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Your team's mute has expired!");
                        }

                        mutesIterator.remove();
                    }
                }
            }

        }.runTaskLater(FoxtrotPlugin.getInstance(), timeSeconds * 20L);

        sender.sendMessage(ChatColor.GRAY + "Muted the team " + target.getName() + ChatColor.GRAY + " for " + TimeUtils.getDurationBreakdown(timeSeconds * 1000L) + " for " + reason + ".");
    }

}
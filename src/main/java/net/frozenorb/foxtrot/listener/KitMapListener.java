package net.frozenorb.foxtrot.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.server.SpawnTagHandler;
import net.frozenorb.foxtrot.team.Team;
import net.frozenorb.foxtrot.team.claims.LandBoard;
import net.frozenorb.foxtrot.team.dtr.DTRBitmask;
import net.frozenorb.qlib.economy.FrozenEconomyHandler;

public class KitMapListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        
        if (Foxtrot.getInstance().getMapHandler().getScoreboardTitle().contains("cane")) {
            return;
        }
        
        Player p = e.getEntity();
        if ((p.getKiller() instanceof Player)) {
            String killerName = p.getKiller().getName();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                    "crate givekey " + killerName + " KillReward 1");
            FrozenEconomyHandler.deposit(p.getKiller().getUniqueId(), 100);
            p.getKiller().sendMessage(ChatColor.RED + "You recieved a reward for killing " + ChatColor.GREEN
                    + p.getName() + ChatColor.RED + ".");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Bukkit.getScheduler().runTaskLater(Foxtrot.getInstance(), event.getEntity()::remove, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Team team = LandBoard.getInstance().getTeam(event.getEntity().getLocation());
        if (team != null && event.getEntity() instanceof Arrow && team.hasDTRBitmask(DTRBitmask.SAFE_ZONE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        if (command.startsWith("/pv") || command.startsWith("/playervault") || command.startsWith("pv") || command.startsWith("playervaults") || command.startsWith("/vault") || command.startsWith("vault") || command.startsWith("vc") || command.startsWith("/vc")) {
            if (SpawnTagHandler.isTagged(event.getPlayer())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You can't /pv in combat.");
            }
        }
    }
}

package net.frozenorb.foxtrot.listener;

import lombok.Getter;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.team.Team;
import net.frozenorb.foxtrot.team.claims.LandBoard;
import net.frozenorb.foxtrot.team.dtr.DTRBitmask;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EnderpearlListener implements Listener {

    @Getter private static Map<String, Long> enderpearlCooldown = new ConcurrentHashMap<>();

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) event.getEntity().getShooter();

        if (event.getEntity() instanceof EnderPearl) {
            if (DTRBitmask.THIRTY_SECOND_ENDERPEARL_COOLDOWN.appliesAt(event.getEntity().getLocation())) {
                enderpearlCooldown.put(shooter.getName(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30));
            } else {
                enderpearlCooldown.put(shooter.getName(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(16));
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) || event.getItem().getType() != Material.ENDER_PEARL) {
            return;
        }

        if (enderpearlCooldown.containsKey(event.getPlayer().getName()) && enderpearlCooldown.get(event.getPlayer().getName()) > System.currentTimeMillis()) {
            long millisLeft = enderpearlCooldown.get(event.getPlayer().getName()) - System.currentTimeMillis();

            double value = (millisLeft / 1000D);
            double sec = value > 0.1 ? Math.round(10.0 * value) / 10.0 : 0.1; // don't tell user 0.0

            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot use this for another " + ChatColor.BOLD + sec + ChatColor.RED + " seconds!");
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        } else if(!enderpearlCooldown.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true); // only reason for this would be player died before pearl landed, so cancel it!
            return;
        }

        Location target = event.getTo();
        Location from = event.getFrom();

        if (DTRBitmask.SAFE_ZONE.appliesAt(target)) {
            if (!DTRBitmask.SAFE_ZONE.appliesAt(from)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Invalid Pearl! " + ChatColor.YELLOW + "You cannot Enderpearl into spawn!");
                return;
            }
        }

        if (DTRBitmask.NO_ENDERPEARL.appliesAt(target)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Invalid Pearl! " + ChatColor.YELLOW + "You cannot Enderpearl into this region!");
            return;
        }

        Team ownerTo = LandBoard.getInstance().getTeam(event.getTo());

        if (Foxtrot.getInstance().getPvPTimerMap().hasTimer(event.getPlayer().getUniqueId()) && ownerTo != null) {
            if (ownerTo.isMember(event.getPlayer().getUniqueId())) {
                Foxtrot.getInstance().getPvPTimerMap().removeTimer(event.getPlayer().getUniqueId());
            } else if (ownerTo.getOwner() != null || (DTRBitmask.KOTH.appliesAt(event.getTo()) || DTRBitmask.CITADEL.appliesAt(event.getTo()))) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Invalid Pearl! " + ChatColor.YELLOW + "You cannot Enderpearl into claims while having a PvP Timer!");
                return;
            }
        }

        Material mat = event.getTo().getBlock().getType();

        if (((mat == Material.THIN_GLASS || mat == Material.IRON_FENCE) && clippingThrough(target, from, 0.65)) || ((mat == Material.FENCE || mat == Material.NETHER_FENCE) && clippingThrough(target, from, 0.45))) {
            event.setTo(from);
            return;
        }

        target.setX(target.getBlockX() + 0.5);
        target.setZ(target.getBlockZ() + 0.5);
        event.setTo(target);
    }

    public boolean clippingThrough(Location target, Location from, double thickness) {
        return ((from.getX() > target.getX() && (from.getX() - target.getX() < thickness)) || (target.getX() > from.getX() && (target.getX() - from.getX() < thickness)) || (from.getZ() > target.getZ() && (from.getZ() - target.getZ() < thickness)) || (target.getZ() > from.getZ() && (target.getZ() - from.getZ() < thickness)));
    }
}
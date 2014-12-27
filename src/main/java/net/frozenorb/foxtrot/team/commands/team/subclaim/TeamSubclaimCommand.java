package net.frozenorb.foxtrot.team.commands.team.subclaim;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.command.annotations.Command;
import net.frozenorb.foxtrot.team.Team;
import net.frozenorb.foxtrot.team.claims.LandBoard;
import net.frozenorb.foxtrot.team.claims.Subclaim;
import net.frozenorb.foxtrot.util.ListUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class TeamSubclaimCommand implements Listener {

    @Getter private static Map<String, Selection> selections = new HashMap<String, TeamSubclaimCommand.Selection>();
    public static final ItemStack SELECTION_WAND = new ItemStack(Material.WOOD_SPADE);

    static {
        ItemMeta meta = SELECTION_WAND.getItemMeta();

        meta.setDisplayName("§a§oSubclaim Wand");
        meta.setLore(ListUtils.wrap(" | §eRight/Left Click§6 Block   §b- §fSelect subclaim's corners", ""));
        SELECTION_WAND.setItemMeta(meta);
    }

    @Command(names={ "team subclaim", "t subclaim", "f subclaim", "faction subclaim", "fac subclaim", "team sub", "t sub", "f sub", "faction sub", "fac sub" }, permissionNode="")
    public static void teamSubclaim(Player sender) {
        sender.sendMessage(ChatColor.RED + "/f subclaim start - starts the subclaiming process");
        sender.sendMessage(ChatColor.RED + "/f subclaim map - toggles a visual subclaim map");
        sender.sendMessage(ChatColor.RED + "/f subclaim create <subclaim> - creates a subclaim");
        sender.sendMessage(ChatColor.RED + "/f subclaim addplayer <subclaim> <player> - adds a player to a subclaim");
        sender.sendMessage(ChatColor.RED + "/f subclaim removeplayer <subclaim> <player> - removes a player from a subclaim");
        sender.sendMessage(ChatColor.RED + "/f subclaim list - views all subclaims");
        sender.sendMessage(ChatColor.RED + "/f subclaim info <subclaim> - views info about a subclaim");
        sender.sendMessage(ChatColor.RED + "/f subclaim unclaim <subclaim> <player> - unclaims a subclaim");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Team team = FoxtrotPlugin.getInstance().getTeamHandler().getPlayerTeam(event.getPlayer().getName());

        if (event.getItem() != null && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && event.getItem().getType() == SELECTION_WAND.getType()) {
            if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().getDisplayName() != null && event.getItem().getItemMeta().getDisplayName().contains("Subclaim")) {
                event.setCancelled(true);

                if (team != null) {
                    Subclaim subclaim = team.getSubclaim(event.getClickedBlock().getLocation());

                    if (subclaim != null) {
                        event.getPlayer().sendMessage(ChatColor.RED + "(" + event.getClickedBlock().getX() + ", " + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ() + ") is a part of " + subclaim.getName() + "!");
                        return;
                    }

                    if (LandBoard.getInstance().getTeam(event.getClickedBlock().getLocation()) != team) {
                        event.getPlayer().sendMessage(ChatColor.RED + "This block is not a part of your teams' territory!");
                        return;
                    }
                }

                Selection selection = new Selection(null, null);

                if (selections.containsKey(event.getPlayer().getName())) {
                    selection = selections.get(event.getPlayer().getName());
                }

                int set = 0;

                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    set = 2;
                    selection.setLoc1(event.getClickedBlock().getLocation());
                } else {
                    set = 1;
                    selection.setLoc2(event.getClickedBlock().getLocation());
                }

                event.getPlayer().sendMessage(ChatColor.YELLOW + "Set subclaim's location " + ChatColor.LIGHT_PURPLE + set + ChatColor.YELLOW + " to " + ChatColor.GREEN + "(" + ChatColor.WHITE + event.getClickedBlock().getX() + ", " + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ() + ChatColor.GREEN + ")" + ChatColor.YELLOW + ".");
                selections.put(event.getPlayer().getName(), selection);
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class Selection {

        private Location loc1;
        private Location loc2;

        public boolean isComplete() {
            return (loc1 != null && loc2 != null);
        }

    }

}
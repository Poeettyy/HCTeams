package net.frozenorb.foxtrot.citadel.commands.citadel;

import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.citadel.CitadelHandler;
import net.frozenorb.foxtrot.command.annotations.Command;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CitadelRespawnChestsCommand {

    @Command(names={"citadel respawnchests"}, permissionNode="op")
    public static void citadelRespawnChests(Player sender) {
        FoxtrotPlugin.getInstance().getCitadelHandler().respawnCitadelChests();
        sender.sendMessage(CitadelHandler.PREFIX + " " + ChatColor.YELLOW + "Respawned all Citadel chests.");
    }

}
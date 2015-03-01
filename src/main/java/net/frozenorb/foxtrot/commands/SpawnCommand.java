package net.frozenorb.foxtrot.commands;

import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.qlib.command.annotations.Command;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SpawnCommand {

    @Command(names={ "spawn" }, permissionNode="")
    public static void spawn(Player sender) {
        if (sender.hasPermission("foxtrot.spawn")) {
            sender.teleport(FoxtrotPlugin.getInstance().getServerHandler().getSpawnLocation());
        } else {
            // Make this pretty.
            sender.sendMessage(ChatColor.RED + "HCTeams does not have a spawn command! You must walk there!");
        }
    }

}
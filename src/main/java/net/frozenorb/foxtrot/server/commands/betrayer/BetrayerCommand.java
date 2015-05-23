package net.frozenorb.foxtrot.server.commands.betrayer;

import net.frozenorb.qlib.command.Command;
import org.bukkit.entity.Player;

public class BetrayerCommand {

    @Command(names={ "betrayer", "betrayers" }, permissionNode="op")
    public static void betrayer(Player sender) {
        String[] msges = {
                "§c/betrayer list - Shows all betrayers.",
                "§c/betrayer add <player> - Add a betrayer.",
                "§c/betrayer remove <player> - Remove a betrayer."};

        sender.sendMessage(msges);
    }

}
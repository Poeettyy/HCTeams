package net.frozenorb.foxtrot.koth.commands.koth;

import net.frozenorb.foxtrot.koth.KOTH;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KOTHLevelCommand {

    @Command(names={ "KOTH Level" }, permissionNode="op")
    public static void kothLevel(Player sender, @Parameter(name="koth") KOTH koth, @Parameter(name="level") int level) {
        koth.setLevel(level);
        sender.sendMessage(ChatColor.GRAY + "Set level for the " + koth.getName() + " KOTH.");
    }

}
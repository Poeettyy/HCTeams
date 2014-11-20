package net.frozenorb.foxtrot.command.commands.koth;

import net.frozenorb.foxtrot.command.annotations.Command;
import net.frozenorb.foxtrot.command.annotations.Param;
import net.frozenorb.foxtrot.koth.KOTH;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by macguy8 on 11/4/2014.
 */
public class KOTHLevelCommand {

    @Command(names={ "KOTH Level" }, permissionNode="foxtrot.koth")
    public static void kothLevel(Player sender, @Param(name="KOTH") KOTH target, @Param(name="Tier") int level) {
        target.setLevel(level);
        sender.sendMessage(ChatColor.GRAY + "Set level for the " + target.getName() + " KOTH.");
    }

}
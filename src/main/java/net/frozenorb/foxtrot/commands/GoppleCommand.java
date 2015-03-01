package net.frozenorb.foxtrot.commands;

import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.qlib.command.annotations.Command;
import net.frozenorb.qlib.command.annotations.Parameter;
import net.frozenorb.foxtrot.util.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GoppleCommand {

    @Command(names={ "Gopple", "Opple", "GoppleTime", "OppleTime", "GoppleTimer", "OppleTimer" }, permissionNode="")
    public static void gopple(Player sender, @Parameter(name="Target", defaultValue="self") String target) {
        String name = sender.getName();

        if (!sender.isOp() || target.equals("self")) {
            name = sender.getName();
        }

        if (FoxtrotPlugin.getInstance().getOppleMap().isOnCooldown(name)) {
            long millisLeft = FoxtrotPlugin.getInstance().getOppleMap().getCooldown(name) - System.currentTimeMillis();
            String msg = TimeUtils.getDurationBreakdown(millisLeft);

            if (sender.getName().equals(name)) {
                sender.sendMessage(ChatColor.GOLD + "Gopple cooldown§f: " + msg);
            } else {
                sender.sendMessage(ChatColor.GOLD + name + "'s gopple cooldown§f: " + msg);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No current gopple cooldown!");
        }
    }

}
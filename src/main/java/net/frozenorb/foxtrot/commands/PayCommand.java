package net.frozenorb.foxtrot.commands;

import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.qlib.util.UUIDUtils;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.UUID;

public class PayCommand {

    @Command(names={ "Pay", "P2P" }, permissionNode="")
    public static void pay(Player sender, @Parameter(name="Target") UUID target, @Parameter(name="Amount") float value) {
        double balance = Basic.get().getEconomyManager().getBalance(sender.getName());

        if (!Foxtrot.getInstance().getPlaytimeMap().hasPlayed(target)) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        Player bukkitPlayer = Foxtrot.getInstance().getServer().getPlayer(target);

        if (sender.equals(bukkitPlayer)) {
            sender.sendMessage(ChatColor.RED + "You cannot send money to yourself!");
            return;
        }

        if (value < 5) {
            sender.sendMessage(ChatColor.RED + "You must send at least $5!");
            return;
        }

        if (Float.isNaN(value)) {
            sender.sendMessage(ChatColor.RED + "Nope.");
            return;
        }

        if (balance < value) {
            sender.sendMessage(ChatColor.RED + "You do not have $" + value + "!");
            return;
        }

        Basic.get().getEconomyManager().depositPlayer(UUIDUtils.name(target), value);
        Basic.get().getEconomyManager().withdrawPlayer(sender.getName(), value);

        sender.sendMessage(ChatColor.YELLOW + "You sent " + ChatColor.LIGHT_PURPLE + NumberFormat.getCurrencyInstance().format(value) + ChatColor.YELLOW + " to " + ChatColor.LIGHT_PURPLE + UUIDUtils.name(target) + ChatColor.YELLOW + ".");

        if (bukkitPlayer != null) {
            bukkitPlayer.sendMessage(ChatColor.LIGHT_PURPLE + sender.getName() + ChatColor.YELLOW + " sent you " + ChatColor.LIGHT_PURPLE + NumberFormat.getCurrencyInstance().format(value) + ChatColor.YELLOW + ".");
        }
    }

}
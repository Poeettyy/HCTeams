package net.frozenorb.foxtrot.command.commands.koth;

import net.frozenorb.foxtrot.command.annotations.Command;
import net.frozenorb.foxtrot.koth.KOTHHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by macguy8 on 11/23/2014.
 */
public class KOTHScheduleCommand {

    @Command(names={ "KOTH Schedule" }, permissionNode="foxtrot.koth")
    public static void kothSchedule(Player sender) {
        int sent = 0;

        for (Map.Entry<Integer, String> entry : KOTHHandler.getKothSchedule().entrySet()) {
            sent++;
            Calendar activationTime = Calendar.getInstance();

            activationTime.set(Calendar.HOUR_OF_DAY, entry.getKey());
            activationTime.set(Calendar.MINUTE, 0);
            activationTime.set(Calendar.SECOND, 0);
            activationTime.set(Calendar.MILLISECOND, 0);

            sender.sendMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.YELLOW + entry.getValue() + ChatColor.GOLD + " can be captured at " + ChatColor.BLUE + (new SimpleDateFormat()).format(activationTime.getTime()) + ChatColor.GOLD + ".");
        }

        if (sent == 0) {
            sender.sendMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.RED + "KOTH Schedule: " + ChatColor.YELLOW + "Undefined");
        }
    }

}
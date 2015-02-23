package net.frozenorb.foxtrot.command.commands;

import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.command.annotations.Command;
import net.frozenorb.foxtrot.listener.BorderListener;
import net.frozenorb.foxtrot.server.ServerHandler;
import net.frozenorb.mBasic.CommandSystem.Commands.Enchant;
import net.minecraft.server.v1_7_R4.Enchantment;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class HelpCommand {

    @Command(names={ "Help" }, permissionNode="")
    public static void help(Player sender) {

         final String sharp = "Sharpness " + Enchantment.DAMAGE_ALL.getMaxLevel();
         final String prot = "Protection " + Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel();

        String[] msg = {



                "§6§m-----------------------------------------------------",
                "§9§lHCTeams Help §7- §eInformation on HCTeams",
                "§7§m-----------------------------------------------------",
                "§9Map Information:",
                "§eCurrent Map: §7Map 3 - Started January 31, 2015", //We should add this as a String in the mapInfo.json, I don't know how.
                "§eMap Border: §7" + BorderListener.BORDER_SIZE,
                "§eWarzone Until: §7" +  ServerHandler.WARZONE_RADIUS,
                "§eEnchant Limits: §7" + sharp + ", " + prot,
                "§eDeathban: §7" + "§6PRO§7: 1 Hour, §aVIP§7: 2 Hours, §fDefault§7: 3 Hours",
                "§eWorld Map: §7" + "http://www.hcteams.com/map/",

                "",
                "§9Helpful Commands:",
                "§e/report <player> <reason> §7- Report cheaters with this command!",
                "§e/request <message> §7- Request staff assistance.",

                "",
                "§9Chat Commands:",
                "§eGlobal Chat §7- Prefix your message with '§d!§7' or type '§d/gc§7' to set.",
                "§eTeam Chat §7- Prefix your message with '§d@§7' or type '§d/tc§7' to set.",
                "§eHide/Show Global Chat §7- Type '§d/tgc§7' to toggle chat visibility.",

                "",
                "§9Other Information:",
                "§eOfficial Teamspeak §7- §dts.minehq.com",
                "§eMineHQ Rules §7- §dwww.minehq.com/rules",
                "§eStore §7- §dwww.minehq.com/store",
                "§eHCTeams Website §7- §dwww.hcteams.com",
                "§6§m-----------------------------------------------------",

        };

        sender.sendMessage(msg);
    }

}
package net.frozenorb.foxtrot.command.subcommand.subcommands.kothsubcommands;

import net.frozenorb.foxtrot.command.subcommand.Subcommand;
import net.frozenorb.foxtrot.game.games.koth.KOTH;
import net.frozenorb.foxtrot.game.games.koth.KOTHs;
import org.bukkit.ChatColor;

/**
 * Created by macguy8 on 10/31/2014.
 */
public class KOTHDelete extends Subcommand {

    public KOTHDelete(String name, String errorMessage, String... aliases) {
        super(name, errorMessage, aliases);
    }

    @Override
    public void syncExecute() {
        if (sender.hasPermission("foxtrot.koth.admin")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Error: " + getErrorMessage());
                return;
            }

            KOTH koth = KOTHs.getKOTH(args[1]);

            if (koth == null) {
                sender.sendMessage(ChatColor.RED + "No KOTH named " + args[1] + " found.");
                return;
            }

            KOTHs.getKOTHs().remove(koth);
            KOTHs.saveKOTHs();
            sender.sendMessage(ChatColor.GRAY + "Deleted KOTH " + koth.getName() + ".");
        }
    }

}
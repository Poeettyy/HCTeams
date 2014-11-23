package net.frozenorb.foxtrot.team.subclaim.transformer;

import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.command.objects.ParamTransformer;
import net.frozenorb.foxtrot.team.Team;
import net.frozenorb.foxtrot.team.claims.Subclaim;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by macguy8 on 11/21/2014.
 */
public class SubclaimTransformer extends ParamTransformer {

    @Override
    public Object transform(CommandSender sender, String source) {
        Team team = FoxtrotPlugin.getInstance().getTeamHandler().getPlayerTeam(sender.getName());

        if (team == null) {
            sender.sendMessage(ChatColor.RED + "You must be on a team to execute this command!");
            return (null);
        }

        if (sender instanceof Player && source.equals("location")) {
            Subclaim subclaim = team.getSubclaim(((Player) sender).getLocation());

            if (subclaim == null) {
                sender.sendMessage(ChatColor.RED + "You are not inside of a subclaim.");
                return (null);
            }

            return (subclaim);
        }

        Subclaim subclaim = team.getSubclaim(source);

        if (subclaim == null) {
            sender.sendMessage(ChatColor.RED + "No subclaim with the name " + source + " found.");
            return (null);
        }

        return (subclaim);
    }

}
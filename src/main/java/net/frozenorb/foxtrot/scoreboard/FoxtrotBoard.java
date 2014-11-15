package net.frozenorb.foxtrot.scoreboard;

import lombok.Getter;
import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chasechocolate.
 */
public class FoxtrotBoard {

    @Getter private Player player;
    @Getter private Objective obj;
    @Getter private Set<String> displayedScores = new HashSet<String>();

    public FoxtrotBoard(Player player) {
        this.player = player;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        obj = board.registerNewObjective("HCTeams", "dummy");
        obj.setDisplayName(FoxtrotPlugin.getInstance().getMapHandler().getScoreboardTitle());
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        update();
        player.setScoreboard(board);
    }

    public void update() {
        int nextVal = 14;

        for (ScoreGetter getter : ScoreGetter.SCORES) {
            long millis = getter.getMillis(player);
            String title = getter.getTitle(player);

            if (millis == ScoreGetter.NO_SCORE) {
                if (displayedScores.contains(title)) {
                    obj.getScoreboard().resetScores(title);
                    displayedScores.remove(title);
                }
            } else {
                displayedScores.add(title);
                obj.getScore(title).setScore(nextVal);
                getTeam(title, millis).addEntry(title);
                nextVal -= 1;
            }
        }

        if (nextVal < 14) {
            obj.getScore(ChatColor.RESET + " ").setScore(15);
        } else {
            obj.getScoreboard().resetScores(ChatColor.RESET + " ");
        }
    }

    private Team getTeam(String title, long millis) {
        String name = ChatColor.stripColor(title);
        Team team = obj.getScoreboard().getTeam(name);

        if (team == null) {
            team = obj.getScoreboard().registerNewTeam(name);
        }

        String time;
        double secs = (millis / 1000.0D);

        if (secs >= 60) {
            time = TimeUtils.getMMSS((int) secs);
        } else {
            time = Math.round(10.0D * secs) / 10.0D + "s";
        }

        team.setSuffix(ChatColor.GRAY + ": " + ChatColor.RED + time);

        return (team);
    }

}
package net.frozenorb.foxtrot.koth;

import lombok.Getter;
import lombok.Setter;
import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.koth.events.*;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.com.google.gson.annotations.SerializedName;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by macguy8 on 10/31/2014.
 */
public class KOTH {

    //***************************//

    public static String LAST_ACTIVE_KOTH = "";

    //***************************//

    @SerializedName("Name")
    @Getter private String name;
    @SerializedName("Location")
    @Getter private BlockVector capLocation;
    @SerializedName("World")
    @Getter private String world;
    @SerializedName("MaxDistance")
    @Getter private int capDistance;
    @SerializedName("CapTime")
    @Getter private int capTime;

    @Getter private transient boolean active;
    @Getter private transient String currentCapper;
    @Getter private transient int remainingCapTime;
    @Getter @Setter private transient int level;

    //***************************//

    public KOTH(String name, Location location) {
        this.name = name;
        this.capLocation = location.toVector().toBlockVector();
        this.world = location.getWorld().getName();
        this.capDistance = 3;
        this.capTime = 60 * 15;
        this.level = 2;

        KOTHHandler.getKOTHs().add(this);
        KOTHHandler.saveKOTHs();
    }

    //***************************//

    public void setLocation(Location location) {
        this.capLocation = location.toVector().toBlockVector();
        this.world = location.getWorld().getName();
        KOTHHandler.saveKOTHs();
    }

    public void setCapDistance(int capDistance) {
        this.capDistance = capDistance;
        KOTHHandler.saveKOTHs();
    }

    public void setCapTime(int capTime) {
        this.capTime = capTime;
        KOTHHandler.saveKOTHs();
    }

    public boolean activate() {
        if (active) {
            return (false);
        }

        FoxtrotPlugin.getInstance().getServer().getPluginManager().callEvent(new KOTHActivatedEvent(this));

        this.active = true;
        this.currentCapper = null;
        this.remainingCapTime = this.capTime;
        this.level = 2;

        return (true);
    }

    public boolean deactivate() {
        if (!active) {
            return (false);
        }

        FoxtrotPlugin.getInstance().getServer().getPluginManager().callEvent(new KOTHDeactivatedEvent(this));

        this.active = false;
        this.currentCapper = null;
        this.remainingCapTime = this.capTime;
        this.level = 2;

        return (true);
    }

    public void startCapping(Player player) {
        if (currentCapper != null) {
            resetCapTime();
        }

        this.currentCapper = player.getName();
        this.remainingCapTime = capTime;
    }

    public boolean finishCapping() {
        Player capper = FoxtrotPlugin.getInstance().getServer().getPlayerExact(currentCapper);

        if (capper == null) {
            resetCapTime();
            return (false);
        }

        KOTHCapturedEvent event = new KOTHCapturedEvent(this, capper);
        FoxtrotPlugin.getInstance().getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return (false);
        }

        deactivate();
        return (true);
    }

    public void resetCapTime() {
        FoxtrotPlugin.getInstance().getServer().getPluginManager().callEvent(new KOTHControlLostEvent(this));

        this.currentCapper = null;
        this.remainingCapTime = capTime;
    }

    protected void tick() {
        if (currentCapper != null) {
            Player capper = FoxtrotPlugin.getInstance().getServer().getPlayerExact(currentCapper);

            if (capper == null || !onCap(capper) || capper.isDead() || capper.getGameMode() != GameMode.SURVIVAL) {
                resetCapTime();
            } else {
                if (remainingCapTime % 10 == 0 && remainingCapTime > 1) {
                    boolean citadel = name.equalsIgnoreCase("Citadel");
                    capper.sendMessage(ChatColor.GOLD + (citadel ? "[Citadel]" : "[KingOfTheHill]") + ChatColor.YELLOW + " Attempting to control " + ChatColor.BLUE + getName() + ChatColor.YELLOW + ".");
                }

                if (remainingCapTime <= 0) {
                    finishCapping();
                } else if (remainingCapTime % 180 == 0) {
                    FoxtrotPlugin.getInstance().getServer().getPluginManager().callEvent(new KOTHControlTickEvent(this));
                }

                this.remainingCapTime--;
            }
        } else {
            List<Player> onCap = new ArrayList<Player>();

            for (Player player : FoxtrotPlugin.getInstance().getServer().getOnlinePlayers()) {
                if (onCap(player) && !player.isDead() && player.getGameMode() != GameMode.CREATIVE) {
                    onCap.add(player);
                }
            }

            Collections.shuffle(onCap);

            if (onCap.size() != 0) {
                startCapping(onCap.get(0));
            }
        }
    }

    public boolean onCap(Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase(world)) {
            return (false);
        }

        int yDistance = player.getLocation().getBlockY() - capLocation.getBlockY();
        return (Math.abs(player.getLocation().getBlockX() - capLocation.getBlockX()) <= capDistance && yDistance >= 0 && yDistance <= 5  && Math.abs(player.getLocation().getBlockZ() - capLocation.getBlockZ()) <= capDistance);
    }

}
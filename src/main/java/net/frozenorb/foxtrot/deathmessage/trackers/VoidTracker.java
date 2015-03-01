package net.frozenorb.foxtrot.deathmessage.trackers;

import mkremins.fanciful.FancyMessage;
import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.deathmessage.DeathMessageHandler;
import net.frozenorb.foxtrot.deathmessage.event.CustomPlayerDamageEvent;
import net.frozenorb.foxtrot.deathmessage.objects.Damage;
import net.frozenorb.foxtrot.deathmessage.objects.PlayerDamage;
import net.frozenorb.foxtrot.util.ClickableUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;

public class VoidTracker implements Listener {

    @EventHandler(priority=EventPriority.LOW)
    public void onCustomPlayerDamage(CustomPlayerDamageEvent event) {
        if (event.getCause().getCause() != EntityDamageEvent.DamageCause.VOID) {
            return;
        }

        List<Damage> record = DeathMessageHandler.getDamage(event.getPlayer());
        Damage knocker = null;
        long knockerTime = 0L;

        if (record != null) {
            for (Damage damage : record) {
                if (damage instanceof VoidDamage || damage instanceof VoidDamageByPlayer) {
                    continue;
                }

                if (damage instanceof PlayerDamage && (knocker == null || damage.getTime() > knockerTime)) {
                    knocker = damage;
                    knockerTime = damage.getTime();
                }
            }
        }

        if (knocker != null) {
            event.setTrackerDamage(new VoidDamageByPlayer(event.getPlayer().getName(), event.getDamage(), ((PlayerDamage) knocker).getDamager()));
        } else {
            event.setTrackerDamage(new VoidDamage(event.getPlayer().getName(), event.getDamage()));
        }
    }

    public static class VoidDamage extends Damage {

        public VoidDamage(String damaged, double damage) {
            super(damaged, damage);
        }

        public FancyMessage getDeathMessage() {
            return (ClickableUtils.deathMessageName(getDamaged()).then(ChatColor.YELLOW + " fell into the void."));
        }

    }

    public static class VoidDamageByPlayer extends PlayerDamage {

        public VoidDamageByPlayer(String damaged, double damage, String damager) {
            super(damaged, damage, damager);
        }

        public FancyMessage getDeathMessage() {
            FancyMessage deathMessage = ClickableUtils.deathMessageName(getDamaged());

            deathMessage.then(ChatColor.YELLOW + " fell into the void thanks to ").then();
            ClickableUtils.appendDeathMessageName(getDamager(), deathMessage);
            deathMessage.then(ChatColor.YELLOW + ".");

            return (deathMessage);
        }

    }

}
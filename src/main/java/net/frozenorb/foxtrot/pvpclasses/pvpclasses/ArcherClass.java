package net.frozenorb.foxtrot.pvpclasses.pvpclasses;

import lombok.Getter;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.deathmessage.DeathMessageHandler;
import net.frozenorb.foxtrot.deathmessage.trackers.ArrowTracker;
import net.frozenorb.foxtrot.pvpclasses.PvPClass;
import net.frozenorb.foxtrot.pvpclasses.PvPClassHandler;
import net.frozenorb.qlib.nametag.FrozenNametagHandler;
import net.frozenorb.qlib.util.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.com.google.gson.internal.Pair;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class ArcherClass extends PvPClass {

    public static final int MARK_SECONDS = 10;

    private static Map<String, Long> lastSpeedUsage = new HashMap<>();
    @Getter private static Map<String, Long> markedPlayers = new ConcurrentHashMap<>();

    @Getter private static Map<String, Set<Pair<String, Long>>> markedBy = new HashMap<>();

    public ArcherClass() {
        super("Archer", 15, "LEATHER_", Arrays.asList(Material.SUGAR));
    }

    @Override
    public void apply(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0), true);
    }

    @Override
    public void tick(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
        }

        if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onEntityArrowHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            final Player player = (Player) event.getEntity();

            if (!(arrow.getShooter() instanceof Player)) {
                return;
            }

            Player shooter = (Player) arrow.getShooter();
            float pullback = arrow.getMetadata("Pullback").get(0).asFloat();

            if (!PvPClassHandler.hasKitOn(shooter, this)) {
                return;
            }

            // 2 hearts for a marked shot
            // 1.5 hearts for a marking / unmarked shot.
            int damage = isMarked(player) ? 4 : 3; // Ternary for getting damage!

            // If the bow isn't 100% pulled back we do 1 heart no matter what.
            if (pullback < 0.5F) {
                damage = 2; // 1 heart
            }

            if (player.getHealth() - damage <= 0D) {
                event.setCancelled(true);
            } else {
                event.setDamage(0D);
            }

            // The 'ShotFromDistance' metadata is applied in the deathmessage module.
            Location shotFrom = (Location) arrow.getMetadata("ShotFromDistance").get(0).value();
            double distance = shotFrom.distance(player.getLocation());

            DeathMessageHandler.addDamage(player, new ArrowTracker.ArrowDamageByPlayer(player.getName(), damage, ((Player) arrow.getShooter()).getName(), shotFrom, distance));
            player.setHealth(Math.max(0D, player.getHealth() - damage));

            if (PvPClassHandler.hasKitOn(player, this)) {
                shooter.sendMessage(ChatColor.YELLOW + "[" + ChatColor.BLUE + "Arrow Range" + ChatColor.YELLOW + " (" + ChatColor.RED + (int) distance + ChatColor.YELLOW + ")] " + ChatColor.RED + "Cannot mark other Archers. " + ChatColor.BLUE.toString() + ChatColor.BOLD + "(" + damage / 2 + " heart" + ((damage / 2 == 1) ? "" : "s") + ")");
            } else if (pullback >= 0.5F) {
                shooter.sendMessage(ChatColor.YELLOW + "[" + ChatColor.BLUE + "Arrow Range" + ChatColor.YELLOW + " (" + ChatColor.RED + (int) distance + ChatColor.YELLOW + ")] " + ChatColor.GOLD + "Marked player for " + MARK_SECONDS + " seconds. " + ChatColor.BLUE.toString() + ChatColor.BOLD + "(" + damage / 2 + " heart" + ((damage / 2 == 1) ? "" : "s") + ")");

                // Only send the message if they're not already marked.
                if (!isMarked(player)) {
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Marked! " + ChatColor.YELLOW + "An archer has shot you and marked you (+25% damage) for " + MARK_SECONDS + " seconds.");
                }

                PotionEffect invis = null;

                for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                    if (potionEffect.getType().equals(PotionEffectType.INVISIBILITY)) {
                        invis = potionEffect;
                        break;
                    }
                }

                if (invis != null) {
                    final PotionEffect invisFinal = invis;

                    player.removePotionEffect(invis.getType());

                    new BukkitRunnable() {

                        public void run() {
                            player.addPotionEffect(invisFinal);
                        }

                    }.runTaskLater(Foxtrot.getInstance(), (MARK_SECONDS * 20) + 5);
                }

                getMarkedPlayers().put(player.getName(), System.currentTimeMillis() + (MARK_SECONDS * 1000));

                getMarkedBy().putIfAbsent(shooter.getName(), new HashSet<>());
                getMarkedBy().get(shooter.getName()).add(new Pair<>(player.getName(), System.currentTimeMillis() + (MARK_SECONDS * 1000)));

                FrozenNametagHandler.reloadPlayer(player);

                new BukkitRunnable() {

                    public void run() {
                        FrozenNametagHandler.reloadPlayer(player);
                    }

                }.runTaskLater(Foxtrot.getInstance(), (MARK_SECONDS * 20) + 5);
            } else {
                shooter.sendMessage(ChatColor.YELLOW + "[" + ChatColor.BLUE + "Arrow Range" + ChatColor.YELLOW + " (" + ChatColor.RED + (int) distance + ChatColor.YELLOW + ")] " + ChatColor.RED + "Bow wasn't fully drawn back. " + ChatColor.BLUE.toString() + ChatColor.BOLD + "(" + damage / 2 + " heart" + ((damage / 2 == 1) ? "" : "s") + ")");
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (isMarked(player)) {
                Player damager = null;
                if (event.getDamager() instanceof Player) {
                    damager = (Player) event.getDamager();
                } else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    damager = (Player) ((Projectile) event.getDamager()).getShooter();
                }

                if (damager != null && !canUseMark(damager, player)) {
                    return;
                }

                // Apply 125% damage if they're 'marked'
                event.setDamage(event.getDamage() * 1.25D);
            }
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        event.getProjectile().setMetadata("Pullback", new FixedMetadataValue(Foxtrot.getInstance(), event.getForce()));
    }

    @Override
    public boolean itemConsumed(Player player, Material material) {
        if (lastSpeedUsage.containsKey(player.getName()) && lastSpeedUsage.get(player.getName()) > System.currentTimeMillis()) {
            long millisLeft = lastSpeedUsage.get(player.getName()) - System.currentTimeMillis();
            String msg = TimeUtils.formatIntoDetailedString((int) millisLeft / 1000);

            player.sendMessage(ChatColor.RED + "You cannot use this for another §c§l" + msg + "§c.");
            return (false);
        }

        lastSpeedUsage.put(player.getName(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 3), true);
        return (true);
    }

    public static boolean isMarked(Player player) {
        return (getMarkedPlayers().containsKey(player.getName()) && getMarkedPlayers().get(player.getName()) > System.currentTimeMillis());
    }

    private boolean canUseMark(Player player, Player victim) {
        if (markedBy.containsKey(player.getName())) {
            for (Pair<String, Long> pair : markedBy.get(player.getName())) {
                if (victim.getName().equals(pair.first) && pair.second > System.currentTimeMillis()) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

}
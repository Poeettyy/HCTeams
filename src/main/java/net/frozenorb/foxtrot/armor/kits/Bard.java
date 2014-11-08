package net.frozenorb.foxtrot.armor.kits;

import lombok.Getter;
import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.armor.Armor;
import net.frozenorb.foxtrot.armor.ArmorMaterial;
import net.frozenorb.foxtrot.armor.Kit;
import net.frozenorb.foxtrot.listener.FoxListener;
import net.frozenorb.foxtrot.team.Team;
import net.frozenorb.foxtrot.util.ParticleEffects;
import net.minecraft.util.com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Connor Hollasch
 * @since 10/10/14
 */
public class Bard extends Kit implements Listener {

    public static final HashMap<Material, PotionEffect> BARD_CLICK_EFFECTS = new HashMap<Material, PotionEffect>();
    public static final HashMap<Material, PotionEffect> BARD_PASSIVE_EFFECTS = new HashMap<Material, PotionEffect>();

    @Getter private static Map<String, Long> positiveEffectCooldown = new HashMap<>();
    @Getter private static Map<String, Long> negativeEffectCooldown = new HashMap<>();

    public static final int BARD_RANGE = 20;

    static {
        BARD_CLICK_EFFECTS.put(Material.IRON_INGOT, PotionEffectType.DAMAGE_RESISTANCE.createEffect(20*5, 7));
        BARD_CLICK_EFFECTS.put(Material.BLAZE_ROD, PotionEffectType.INCREASE_DAMAGE.createEffect(3*20, 0));
        BARD_CLICK_EFFECTS.put(Material.FEATHER, PotionEffectType.JUMP.createEffect(10*20, 5));
        BARD_CLICK_EFFECTS.put(Material.RED_MUSHROOM, PotionEffectType.POISON.createEffect(20*2, 0));
        BARD_CLICK_EFFECTS.put(Material.BROWN_MUSHROOM, PotionEffectType.WEAKNESS.createEffect(20*10, 0));
        BARD_CLICK_EFFECTS.put(Material.SLIME_BALL, PotionEffectType.SLOW.createEffect(20*10, 0));
        BARD_CLICK_EFFECTS.put(Material.RAW_FISH, PotionEffectType.WATER_BREATHING.createEffect(20*45, 0));
        BARD_CLICK_EFFECTS.put(Material.SPIDER_EYE, PotionEffectType.WITHER.createEffect(20*7, 0));
        BARD_CLICK_EFFECTS.put(Material.SUGAR, PotionEffectType.SPEED.createEffect(20*10, 3));
        BARD_CLICK_EFFECTS.put(Material.MAGMA_CREAM, PotionEffectType.FIRE_RESISTANCE.createEffect(20 * 45, 0));
        BARD_CLICK_EFFECTS.put(Material.GHAST_TEAR, PotionEffectType.REGENERATION.createEffect(20 * 5, 1));

        BARD_CLICK_EFFECTS.put(Material.SPECKLED_MELON, null);
        //BARD_CLICK_EFFECTS.put(Material.EYE_OF_ENDER, null);
        BARD_CLICK_EFFECTS.put(Material.WHEAT, null);

        BARD_PASSIVE_EFFECTS.put(Material.GHAST_TEAR, PotionEffectType.REGENERATION.createEffect(20*6, 0));
        BARD_PASSIVE_EFFECTS.put(Material.MAGMA_CREAM, PotionEffectType.FIRE_RESISTANCE.createEffect(20 * 6, 0));
        BARD_PASSIVE_EFFECTS.put(Material.SUGAR, PotionEffectType.SPEED.createEffect(20*6, 1));
        BARD_PASSIVE_EFFECTS.put(Material.IRON_INGOT, PotionEffectType.DAMAGE_RESISTANCE.createEffect(20*6, 0));
        BARD_PASSIVE_EFFECTS.put(Material.FEATHER, PotionEffectType.JUMP.createEffect(20*6, 1));

        //Custom code
        //Glistering Melon - Heals 6 Hearts Instantly
        //Eye Of Ender - Reveals Invisible Rouge Players within 80 blocks. (Forces a 30 second cool-down on the Rouge Player before they can go Invisible again)
        //Wheat - Heals 6 hunger points
    }

    public Bard() {
        new BukkitRunnable() {

            public void run() {
                for (Player player : FoxtrotPlugin.getInstance().getServer().getOnlinePlayers()) {
                    if (hasKitOn(player)) {
                        apply(player);

                        if (player.getItemInHand() != null && BARD_PASSIVE_EFFECTS.containsKey(player.getItemInHand().getType()) && (FoxtrotPlugin.getInstance().getServerHandler().isEOTW() || !FoxtrotPlugin.getInstance().getServerHandler().isGlobalSpawn(player.getLocation()))) {
                            giveBardEffect(player, BARD_PASSIVE_EFFECTS.get(player.getItemInHand().getType()), true);
                        }
                    }
                }
            }

        }.runTaskTimer(FoxtrotPlugin.getInstance(), 1L, 1L);
    }

    @Override
    public boolean qualifies(Armor armor) {
        return (armor.isFullSet(ArmorMaterial.GOLD));
    }

    @Override
    public String getName() {
        return ("Bard");
    }

    @Override
    public void apply(Player player) {
        player.addPotionEffect(PotionEffectType.SPEED.createEffect(Integer.MAX_VALUE, 0));
        player.addPotionEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(Integer.MAX_VALUE, 1));
        player.addPotionEffect(PotionEffectType.REGENERATION.createEffect(Integer.MAX_VALUE, 1));
        player.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(Integer.MAX_VALUE, 1));
    }

    @Override
    public void remove(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
    }

    @Override
    public int getWarmup() {
        return (10);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_") || !event.hasItem() || !BARD_CLICK_EFFECTS.containsKey(event.getItem().getType()) || !hasKitOn(event.getPlayer())) {
            return;
        }

        if (!FoxtrotPlugin.getInstance().getServerHandler().isEOTW() && FoxtrotPlugin.getInstance().getServerHandler().isGlobalSpawn(event.getPlayer().getLocation())) {
            event.getPlayer().sendMessage(ChatColor.RED + "Bard effects cannot be used while in spawn.");
            return;
        }

        boolean negative = BARD_CLICK_EFFECTS.get(event.getItem().getType()) != null && Arrays.asList(FoxListener.DEBUFFS).contains(BARD_CLICK_EFFECTS.get(event.getItem().getType()).getType());

        if (negative) {
            if (negativeEffectCooldown.containsKey(event.getPlayer().getName()) && negativeEffectCooldown.get(event.getPlayer().getName()) > System.currentTimeMillis() && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                long millisLeft = negativeEffectCooldown.get(event.getPlayer().getName()) - System.currentTimeMillis();

                double value = (millisLeft / 1000D);
                double sec = Math.round(10.0 * value) / 10.0;

                event.getPlayer().sendMessage(ChatColor.RED + "You cannot use this for another " + ChatColor.BOLD + sec + ChatColor.RED + " seconds!");
                return;
            }

            negativeEffectCooldown.put(event.getPlayer().getName(), System.currentTimeMillis() + (1000L * 60));
        } else {
            if (positiveEffectCooldown.containsKey(event.getPlayer().getName()) && positiveEffectCooldown.get(event.getPlayer().getName()) > System.currentTimeMillis() && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                long millisLeft = positiveEffectCooldown.get(event.getPlayer().getName()) - System.currentTimeMillis();

                double value = (millisLeft / 1000D);
                double sec = Math.round(10.0 * value) / 10.0;

                event.getPlayer().sendMessage(ChatColor.RED + "You cannot use this for another " + ChatColor.BOLD + sec + ChatColor.RED + " seconds!");
                return;
            }

            positiveEffectCooldown.put(event.getPlayer().getName(), System.currentTimeMillis() + (1000L * 60));
        }

        if (negative) {
            ParticleEffects.sendToLocation(ParticleEffects.WITCH_MAGIC, event.getPlayer().getLocation(), 1, 1, 1, 1, 50);
        } else {
            ParticleEffects.sendToLocation(ParticleEffects.HAPPY_VILLAGER, event.getPlayer().getLocation(), 1, 1, 1, 1, 50);
        }

        giveBardEffect(event.getPlayer(), BARD_CLICK_EFFECTS.get(event.getItem().getType()), !negative);

        if (event.getItem().getType() != Material.FEATHER) {
            event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);

            if (event.getPlayer().getItemInHand().getAmount() == 0) {
                event.getPlayer().setItemInHand(null);
                event.getPlayer().updateInventory();
            }
        }
    }

    public static void giveBardEffect(Player source, PotionEffect potionEffect, boolean friendly) {
        for (Player player : getNearbyPlayers(source, friendly)) {
            if (!FoxtrotPlugin.getInstance().getServerHandler().isEOTW() && FoxtrotPlugin.getInstance().getServerHandler().isGlobalSpawn(player.getLocation())) {
                continue;
            }

            if (potionEffect != null) {
                boolean needEffect = true;

                if (player.hasPotionEffect(potionEffect.getType())) {
                    for (PotionEffect activePotionEffect : player.getActivePotionEffects()) {
                        if (activePotionEffect.getType().equals(potionEffect.getType())) {
                            if (potionEffect.getAmplifier() < activePotionEffect.getAmplifier()) {
                                needEffect = false;
                            }

                            if (potionEffect.getAmplifier() == activePotionEffect.getAmplifier() && potionEffect.getDuration() > activePotionEffect.getDuration()) {
                                needEffect = false;
                            }

                            break;
                        }
                    }
                }

                if (needEffect) {
                    player.removePotionEffect(potionEffect.getType());
                    player.addPotionEffect(potionEffect);
                }
            } else {
                Material material = source.getItemInHand().getType();

                if (material == Material.SPECKLED_MELON) {
                    double add = 6.0;

                    if ((player.getHealth() + add) > player.getMaxHealth()) {
                        player.setHealth(player.getMaxHealth());
                    } else {
                        player.setHealth(player.getHealth() + add);
                    }
                } else if (material == Material.WHEAT) {
                    int add = 6;

                    if ((player.getFoodLevel() + add) > 20) {
                        player.setFoodLevel(20);
                    } else {
                        player.setFoodLevel(player.getFoodLevel() + add);
                    }
                }
            }
        }
    }

    public static List<Player> getNearbyPlayers(Player player, boolean friendly) {
        List<Player> valid = Lists.newArrayList();
        Team sourceTeam = FoxtrotPlugin.getInstance().getTeamHandler().getPlayerTeam(player.getName());

        for (Entity entity : player.getNearbyEntities(BARD_RANGE, BARD_RANGE, BARD_RANGE)) {
            if (entity instanceof Player) {
                Player nearbyPlayer = (Player) entity;

                if (sourceTeam == null) {
                    if (!friendly) {
                        valid.add(nearbyPlayer);
                    }

                    continue;
                }

                boolean isTeammate = sourceTeam.isMember(nearbyPlayer.getName());

                if (friendly && isTeammate) {
                    valid.add(nearbyPlayer);
                } else if (!friendly && !isTeammate) {
                    valid.add(nearbyPlayer);
                }
            }
        }

        valid.add(player);
        return (valid);
    }

}
package net.frozenorb.foxtrot.listener;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import net.frozenorb.foxtrot.Foxtrot;

public class MapListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        double multiplier = Foxtrot.getInstance().getMapHandler().getBaseLootingMultiplier();

        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();

            if (player.getItemInHand() != null && player.getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
                switch (player.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)) {
                    case 1:
                        multiplier = Foxtrot.getInstance().getMapHandler().getLevel1LootingMultiplier();
                        break;
                    case 2:
                        multiplier = Foxtrot.getInstance().getMapHandler().getLevel2LootingMultiplier();
                        break;
                    case 3:
                        multiplier = Foxtrot.getInstance().getMapHandler().getLevel3LootingMultiplier();
                        break;
                    default:
                        break;
                }
            }
        }

        event.setDroppedExp((int) Math.ceil(event.getDroppedExp() * multiplier));
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true) // This is actually 'Lowest', but Bukkit calls listeners LOWEST -> HIGHEST, so HIGHEST is what's actually called last. #BukkitBeLike
    public void onBlockBreak(BlockBreakEvent event) {
        if (!Foxtrot.getInstance().getMapHandler().isFastSmeltEnabled() || event.getPlayer().getItemInHand() == null || !event.getPlayer().getItemInHand().getType().name().contains("PICKAXE") || event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }

        Material blockType = event.getBlock().getType();

        if (blockType == Material.GOLD_ORE || blockType == Material.IRON_ORE) {
            ItemStack drop;

            if (blockType == Material.GOLD_ORE) {
                drop = new ItemStack(Material.GOLD_INGOT);
                Foxtrot.getInstance().getGoldMinedMap().setMined(event.getPlayer().getUniqueId(), Foxtrot.getInstance().getGoldMinedMap().getMined(event.getPlayer().getUniqueId()) + 1);
            } else {
                drop = new ItemStack(Material.IRON_INGOT);
                Foxtrot.getInstance().getIronMinedMap().setMined(event.getPlayer().getUniqueId(), Foxtrot.getInstance().getIronMinedMap().getMined(event.getPlayer().getUniqueId()) + 1);
            }

            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
            event.setCancelled(true);
            event.getPlayer().giveExp(4);
            event.getBlock().setType(Material.AIR);
        }
    }

}
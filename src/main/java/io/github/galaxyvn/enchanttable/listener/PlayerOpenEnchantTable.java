package io.github.galaxyvn.enchanttable.listener;

import de.tr7zw.changeme.nbtapi.NBTBlock;
import io.github.galaxyvn.enchanttable.gui.EnchantmentGui;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.EnchantingTable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class PlayerOpenEnchantTable implements Listener {

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            if (event.getInventory().getType().equals(InventoryType.ENCHANTING)) {
                event.setCancelled(true);

                int bookshelfPower = 0;
                Block block = player.getTargetBlock(null, 5);
                if (block.getState() instanceof EnchantingTable) {
                    EnchantingTable enchantingTable = (EnchantingTable) block.getState();

                    World world = enchantingTable.getWorld();
                    int centerX = enchantingTable.getLocation().getBlockX();
                    int centerZ = enchantingTable.getLocation().getBlockZ();
                    int centerY = enchantingTable.getLocation().getBlockY();

                    for (int x = centerX - 2; x <= centerX + 2; x++) {
                        for (int y = centerY; y <= centerY + 2; y++) {
                            for (int z = centerZ - 2; z <= centerZ + 2; z++) {
                                Block blockAround = world.getBlockAt(x, y, z);
                                if (blockAround.getType() == Material.BOOKSHELF) {
                                    NBTBlock nbtBlock = new NBTBlock(blockAround);
                                    if (nbtBlock.getData().getString("EnchantedBlock").equals("Bookshelf")) {
                                        bookshelfPower += nbtBlock.getData().getInteger("EnchantedBookShelf");
                                        continue;
                                    }
                                    bookshelfPower++;
                                }
                            }
                        }
                    }
                }

                new EnchantmentGui(player, bookshelfPower, new ItemStack(Material.AIR), false, false, false).open();
            }
        }
    }
}

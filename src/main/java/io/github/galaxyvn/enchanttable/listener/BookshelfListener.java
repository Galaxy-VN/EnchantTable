package io.github.galaxyvn.enchanttable.listener;

import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.changeme.nbtapi.NBTBlock;
import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.galaxyvn.enchanttable.EnchantTable;
import io.github.galaxyvn.enchanttable.manager.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BookshelfListener implements Listener {

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        NBTItem nbtItem = new NBTItem(item);
        if (nbtItem.hasTag("EnchantedBlock")) {
            Block block = event.getBlockPlaced();
            NBTBlock nbtBlock = new NBTBlock(block);
            nbtBlock.getData().setString("EnchantedBlock", "Bookshelf");
            nbtBlock.getData().setInteger("EnchantedBookShelf", nbtItem.getInteger("EnchantedBookShelf"));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        NBTBlock nbtBlock = new NBTBlock(block);

        if (nbtBlock.getData().getString("EnchantedBlock").equals("Bookshelf")) {
            // Cancel drop items
            event.setDropItems(false);

            // Drop Item
            block.getWorld().dropItemNaturally(block.getLocation(), buildItem(nbtBlock));

            // Clear NBTBlock pos
            nbtBlock.getData().clearNBT();
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        for (Block explodeBlock : event.blockList()) {
            NBTBlock nbtBlock = new NBTBlock(explodeBlock);
            if (nbtBlock.getData().hasTag("EnchantedBlock")) {
                // Cancel drop
                explodeBlock.setType(Material.AIR);

                explodeBlock.getWorld().dropItemNaturally(explodeBlock.getLocation(), buildItem(nbtBlock));

                // Clear NBTBlock pos
                nbtBlock.getData().clearNBT();
            }
        }
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        NBTBlock nbtBlock = new NBTBlock(block);
        if (nbtBlock.getData().getString("EnchantedBlock").equals("Bookshelf"))
            nbtBlock.getData().clearNBT();
    }

//    @EventHandler
//    public void onPush(BlockPistonExtendEvent event) {
//        for (Block involvedBlock : event.getBlocks()) {
//            NBTBlock nbtBlock = new NBTBlock(involvedBlock);
//
//        }
//    }

    static ItemStack buildItem(NBTBlock nbtBlock) {
        ConfigurationSection section = EnchantTable.getPlugin().getConfig().getConfigurationSection("item");

        // Build Item
        List<String> loresReplace = new ArrayList<>();
        for (String lore : section.getStringList("lore")) {
            loresReplace.add(lore.replace("{bookshelf-power}", String.valueOf(section.getInt("bookshelf-power"))));
        }

        ItemStack item = new ItemBuilder(XMaterial.matchXMaterial(section.getString("material")).orElseThrow().parseItem())
                .setName(section.getString("name"))
                .setSkull(section.contains("skull") ? section.getString("skull") : null)
                .setGlowing(section.contains("glow") ? section.getBoolean("glow") : false)
                .addLore(loresReplace)
                .build();
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("EnchantedBlock", nbtBlock.getData().getString("EnchantedBlock"));
        nbtItem.setInteger("EnchantedBookShelf", nbtBlock.getData().getInteger("EnchantedBookShelf"));
        item = nbtItem.getItem();

        return item;
    }
}

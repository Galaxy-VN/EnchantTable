package io.github.galaxyvn.enchanttable.command;

import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.changeme.nbtapi.NBTItem;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import io.github.galaxyvn.enchanttable.EnchantTable;
import io.github.galaxyvn.enchanttable.gui.EnchantmentGui;
import io.github.galaxyvn.enchanttable.manager.items.ItemBuilder;
import io.github.galaxyvn.enchanttable.util.HexUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    ConfigurationSection configSection = EnchantTable.getPlugin().getConfig().getConfigurationSection("messages.prefix");

    public void loadCommands() {
        new CommandAPICommand("enchanttable")
                .withAliases("et", "etable")
                .withPermission("enchanttable.open")
                .withSubcommand(getReloadCommand())
                .withSubcommand(getGiveEnchantedBookshelfCommand())
                .executesPlayer((player, args) -> {
                    int bookPower = 0;

                    World world = player.getWorld();
                    int centerX = player.getLocation().getBlockX();
                    int centerZ = player.getLocation().getBlockZ();
                    int centerY = player.getLocation().getBlockY();

                    for (int x = centerX - 2; x <= centerX + 2; x++) {
                        for (int y = centerY; y <= centerY + 2; y++) {
                            for (int z = centerZ - 2; z <= centerZ + 2; z++) {
                                Block blockAround = world.getBlockAt(x, y, z);
                                if (blockAround.getType() == Material.BOOKSHELF) {
                                    bookPower++;
                                }
                            }
                        }
                    }

                    new EnchantmentGui(player, bookPower, new ItemStack(Material.AIR), false, false, false).open();
                })
                .register();
    }

    private CommandAPICommand getReloadCommand() {
        return new CommandAPICommand("reload")
                .withPermission("enchanttable.admin")
                .executes(((sender, args) -> {
                    EnchantTable.getPlugin().reloadConfig();
                    sender.sendMessage(HexUtils.colorify(configSection.getString("prefix") + configSection.getString("reload")));
                }));
    }

    private CommandAPICommand getGiveEnchantedBookshelfCommand() {
        return new CommandAPICommand("give")
                .withPermission("enchanttable.admin")
                .withArguments(new PlayerArgument("player"),
                        new IntegerArgument("amount"))
                .executes(((sender, args) -> {
                    final Player player = (Player) args.get("player");
                    final int amount = (int) args.get("amount");
                    final FileConfiguration config = EnchantTable.getPlugin().getConfig();

                    ConfigurationSection section = config.getConfigurationSection("item");
                    int bookshelfPower = section.getInt("bookshelf-power");

                    // Build Item
                    List<String> loresReplace = new ArrayList<>();
                    for (String lore : section.getStringList("lore")) {
                        loresReplace.add(lore.replace("{bookshelf-power}", String.valueOf(bookshelfPower)));
                    }

                    ItemStack item = new ItemBuilder(XMaterial.matchXMaterial(section.getString("material")).orElseThrow().parseItem())
                            .setName(section.getString("name"))
                            .setSkull(section.contains("skull") ? section.getString("skull") : null)
                            .setGlowing(section.contains("glow") ? section.getBoolean("glow") : false)
                            .addLore(loresReplace)
                            .build();
                    NBTItem nbtItem = new NBTItem(item);
                    nbtItem.setString("EnchantedBlock", "Bookshelf");
                    nbtItem.setInteger("EnchantedBookShelf", bookshelfPower);
                    item = nbtItem.getItem();

                    if (amount != 0) {
                        for (int i = 0; i < amount; i++)
                            player.getInventory().addItem(item);
                    } else player.getInventory().addItem(item);
                    ItemStack finalItem = item;
                    player.sendMessage(
                            Component.text(HexUtils.colorify(config.getString("messages.give")
                                    .replace("{player}", player.getName())
                                    .replace("{amount}", String.valueOf(amount))
                            )).replaceText(builder -> builder
                                    .matchLiteral("{item}")
                                    .replacement(finalItem.getItemMeta().displayName()))
                    );
                }));
    }
}

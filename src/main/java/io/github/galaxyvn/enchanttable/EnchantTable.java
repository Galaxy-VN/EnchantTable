package io.github.galaxyvn.enchanttable;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.galaxyvn.enchanttable.command.CommandManager;
import io.github.galaxyvn.enchanttable.listener.PlayerOpenEnchantTable;
import io.github.galaxyvn.enchanttable.listener.BookshelfListener;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class EnchantTable extends JavaPlugin {
    private static EnchantTable plugin;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        saveDefaultConfig();
        reloadConfig();

        // Commands
        CommandAPI.onEnable();
        new CommandManager().loadCommands();

        // Register Events
        Bukkit.getPluginManager().registerEvents(new PlayerOpenEnchantTable(), this);
        Bukkit.getPluginManager().registerEvents(new BookshelfListener(), this);

        // Inventory Init
        new InventoryAPI(this).init();
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    public static EnchantTable getPlugin() {
        return plugin;
    }
}

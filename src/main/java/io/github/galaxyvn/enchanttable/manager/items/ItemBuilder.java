package io.github.galaxyvn.enchanttable.manager.items;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Strings;
import io.github.galaxyvn.enchanttable.util.HexUtils;
import io.github.galaxyvn.enchanttable.util.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ItemBuilder {

    private ItemStack stack;
    private Material type;
    private int amount = 1;
    private String name;
    private List<String> lore = new ArrayList<>();
    private byte data = 0;
    private PotionType potionType;
    private boolean glowing = false;
    private int customModel = -1;
    private String skull;

    public ItemBuilder(Material type) {
        this.type = type;
    }

    public ItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public ItemBuilder setAmount(int amount) {
        if (amount > type.getMaxStackSize())
            amount = type.getMaxStackSize();
        this.amount = amount;
        return this;
    }

    public ItemBuilder setData(byte data) {
        this.data = data;
        return this;
    }

    public ItemBuilder setName(String name) {
        this.name = HexUtils.colorify(name);
        return this;
    }

    public ItemBuilder setName(String name, ChatColor color) {
        this.name = color + name;
        return this;
    }

    public ItemBuilder addLore(List<String> lore) {
        for (String s : lore) {
            this.lore.add(HexUtils.colorify(s));
        }
        return this;
    }

    public ItemBuilder addLore(String... lore) {
        for (String s : lore) {
            this.lore.add(HexUtils.colorify(ChatColor.GRAY + s));
        }
        return this;
    }

    public ItemBuilder addDescription(String desc) {
        return addDescription(desc, ChatColor.of(new Color(207, 215, 217)));
    }

    public ItemBuilder addDescription(String desc, ChatColor color) {
        return addDescription(desc, color, true);
    }

    public ItemBuilder addDescription(String desc, ChatColor color, boolean paddingBottom) {
        StringBuilder line = new StringBuilder();
        boolean complete = false;
        String[] words = desc.split(" ");
        for (int i = 0; i < words.length; i++) {
            String s = words[i];
            // Subtract the color codes from lines length
            int trulLen = line.length() - Utils.getColorCodeCount(line.toString()) * 2;
            if (trulLen >= 25) {
                addLore(color + line.toString());
                line = new StringBuilder();
                if (i >= words.length - 1)
                    complete = true;
            } else {
                complete = false;
            }
            line.append(s);
            line.append(" ");
        }
        if (!complete)
            addLore(color.toString() + line.toString());
        if (paddingBottom) addLore("");
        return this;
    }

    public ItemBuilder setPotionType(PotionType type) {
        this.potionType = type;
        return this;
    }

    public ItemBuilder setGlowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }

    public ItemBuilder setCustomModelData(int id) {
        this.customModel = id;
        return this;
    }

    public ItemBuilder setSkull(String skull) {
        this.skull = skull;
        return this;
    }

    public ItemStack build() {
        ItemStack item = stack == null ? new ItemStack(this.type, this.amount) : stack;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(name);
        meta.setLore(lore);
        if (this.data > 0) {
            item.setDurability(this.data);
        }
        if (customModel > 0) {
            meta.setCustomModelData(customModel);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
        if (glowing) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }
        if (meta instanceof SkullMeta) {
            String skull = this.skull;
            if (skull != null) SkullUtils.applySkin(meta, skull);
        } else if (meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta;
            if (potionType != null) potionMeta.setBasePotionData(new PotionData(potionType));
        }
        item.setItemMeta(meta);
        return item;
    }

    private static List<String> splitNewLine(String str) {
        int len = str.length();
        List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false, lastMatch = false;

        while (i < len) {
            if (str.charAt(i) == '\n') {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }

        if (match || lastMatch) {
            list.add(str.substring(start, i));
        }

        return list;
    }
}


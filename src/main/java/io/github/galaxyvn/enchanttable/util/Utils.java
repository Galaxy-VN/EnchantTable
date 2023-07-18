package io.github.galaxyvn.enchanttable.util;

import com.cryptomorin.xseries.XEnchantment;
import com.google.gson.JsonStreamParser;
import io.github.galaxyvn.enchanttable.EnchantTable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern COLOR_PATTERN = Pattern.compile("(&[a-fk-or0-9])", Pattern.CASE_INSENSITIVE);

    public static List<Enchantment> getEnchantmentsForItem(ItemStack itemStack, boolean sortMissing, boolean sort, boolean revert) {
        List<Enchantment> validEnchantments = new ArrayList<>();

        for (XEnchantment xenchantment : XEnchantment.values()) {
            if (!xenchantment.isSupported())
                continue;

            if (xenchantment.getEnchant().canEnchantItem(itemStack)) {
                validEnchantments.add(xenchantment.getEnchant());
            }
        }

        if (sortMissing) {
            validEnchantments.sort(Comparator.comparingInt(enchantment -> {
                if (itemStack.getEnchantments().keySet().contains(enchantment)) {
                    return 1;
                } else {
                    return -1;
                }
            }));
        } else if (sort) {
            Collections.sort(validEnchantments, Comparator.comparing(enchantment -> enchantment.getKey().getKey()));
            if (revert)
                Collections.sort(validEnchantments, Comparator.comparing(enchantment -> enchantment.getKey().getKey(), Comparator.reverseOrder()));
        }

        return validEnchantments;
    }

    public static String getEnchantmentDescription(Enchantment enchantment) {
        FileConfiguration config = EnchantTable.getPlugin().getConfig();
        switch (enchantment.getKey().getKey()) {
            case "protection":
                return config.getString("enchant-description.protection");
            case "fire_protection":
                return config.getString("enchant-description.fire-protection");
            case "feather_falling":
                return config.getString("enchant-description.feather-falling");
            case "blast_protection":
                return config.getString("enchant-description.blast-protection");
            case "projectile_protection":
                return config.getString("enchant-description.projectile-protection");
            case "respiration":
                return config.getString("enchant-description.respiration");
            case "aqua_affinity":
                return config.getString("enchant-description.aqua-affinity");
            case "thorns":
                return config.getString("enchant-description.thorns");
            case "depth_strider":
                return config.getString("enchant-description.depth-strider");
            case "frost_walker":
                return config.getString("enchant-description.frost-walker");
            case "binding_curse":
                return config.getString("enchant-description.binding-curse");
            case "sharpness":
                return config.getString("enchant-description.sharpness");
            case "smite":
                return config.getString("enchant-description.smite");
            case "bane_of_arthropods":
                return config.getString("enchant-description.bane-of-arthropods");
            case "knockback":
                return config.getString("enchant-description.knockback");
            case "fire_aspect":
                return config.getString("enchant-description.fire-aspect");
            case "looting":
                return config.getString("enchant-description.looting");
            case "sweeping":
                return config.getString("enchant-description.sweeping");
            case "efficiency":
                return config.getString("enchant-description.efficiency");
            case "silk_touch":
                return config.getString("enchant-description.silk-touch");
            case "unbreaking":
                return config.getString("enchant-description.unbreaking");
            case "fortune":
                return config.getString("enchant-description.fortune");
            case "power":
                return config.getString("enchant-description.power");
            case "punch":
                return config.getString("enchant-description.punch");
            case "flame":
                return config.getString("enchant-description.flame");
            case "infinity":
                return config.getString("enchant-description.infinity");
            case "luck_of_the_sea":
                return config.getString("enchant-description.luck-of-the-sea");
            case "lure":
                return config.getString("enchant-description.lure");
            case "loyalty":
                return config.getString("enchant-description.loyalty");
            case "impaling":
                return config.getString("enchant-description.impaling");
            case "riptide":
                return config.getString("enchant-description.riptide");
            case "channeling":
                return config.getString("enchant-description.channeling");
            case "multishot":
                return config.getString("enchant-description.multishot");
            case "quick_charge":
                return config.getString("enchant-description.quick-charge");
            case "piercing":
                return config.getString("enchant-description.piercing");
            case "mending":
                return config.getString("enchant-description.mending");
            case "vanishing_curse":
                return config.getString("enchant-description.vanishing-curse");
            case "soul_speed":
                return config.getString("enchant-description.soul-speed");
            default:
                return "Không có mô tả.";
        }
    }

    public static String getRomanLevel(int level) {
        switch (level) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            default:
                return "Unknown";
        }
    }

    public static List<String> getApplied(Enchantment enchantment) {
        List<String> appliedList = new ArrayList<>();

        if (enchantment.getKey().getKey().equals("looting")) {
            appliedList.add("&7 - &fSword");
            appliedList.add("&7 - &fFishing Rod");
        }

        if (enchantment.getItemTarget() == null) return appliedList;
        if (enchantment.getItemTarget().equals(EnchantmentTarget.ARMOR_HEAD)) {
            appliedList.add("&7 - &fHelmet");
        } else if (enchantment.getItemTarget().equals(EnchantmentTarget.ARMOR_TORSO)) {
            appliedList.add("&7 - &fChestplate");
        } else if (enchantment.getItemTarget().equals(EnchantmentTarget.ARMOR_FEET)) {
            appliedList.add("&7 - &fBoots");
        } else {
            appliedList.add("&7 - &f" + String.valueOf(enchantment.getItemTarget()).substring(0, 1).toUpperCase() + enchantment.getItemTarget().name().substring(1).toLowerCase());
        }

        if (!appliedList.isEmpty()) appliedList.add(0, "&6Áp Dụng Cho:");
        return appliedList;
    }

    public static List<String> getSources(Enchantment enchantment) {
        List<String> sourcesList = new ArrayList<>();
        sourcesList.add("&6Các Nguồn Lấy:");
        if (enchantment.getStartLevel() == enchantment.getMaxLevel()) {
            sourcesList.add("&7 - Bàn phù phép (&a" + getRomanLevel(enchantment.getStartLevel()) + "&7)");
            sourcesList.add("&7 - Câu cá (&a" + getRomanLevel(enchantment.getStartLevel()) + "&7)");
            sourcesList.add("&7 - Dân làng (&a" + getRomanLevel(enchantment.getStartLevel()) + "&7)");
        } else {
            sourcesList.add("&7 - Bàn phù phép (&a" + getRomanLevel(enchantment.getStartLevel()) + "-" + getRomanLevel(enchantment.getMaxLevel()) + "&7)");
            sourcesList.add("&7 - Câu cá (&a" + getRomanLevel(enchantment.getStartLevel()) + "-" + getRomanLevel(enchantment.getMaxLevel()) + "&7)");
            sourcesList.add("&7 - Dâng làng (&a" + getRomanLevel(enchantment.getStartLevel()) + "-" + getRomanLevel(enchantment.getMaxLevel()) + "&7)");
            sourcesList.add("&7 - Các sự kiện trong máy chủ (&a" + getRomanLevel(enchantment.getMaxLevel() + 1) + "-" + Utils.getRomanLevel(enchantment.getMaxLevel() + 3) + "&7)");
        }
        sourcesList.add("");
        return sourcesList;
    }

    public static List<Enchantment> searchEnchantmentsName(String searchEnchantment) {
        List<Enchantment> resultEnchantments = new ArrayList<>();
        for (XEnchantment xenchantment : XEnchantment.values()) {
            if (!xenchantment.isSupported())
                continue;

            Enchantment enchantment = xenchantment.getEnchant();
            if (enchantment.getKey().getKey().replace("-", " ").toLowerCase().contains(searchEnchantment.toLowerCase()))
                resultEnchantments.add(enchantment);
        }

        return resultEnchantments;
    }

    public static int getColorCodeCount(String text) {
        Matcher matcher = COLOR_PATTERN.matcher(text);
        return (text.length() - matcher.replaceAll("").length()) / 2;
    }

    public static String getCapitalizedName(String name) {
        String[] words = name.split(" ");
        StringBuilder capitalizedName = new StringBuilder();

        for (String word : words) {
            String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
            capitalizedName.append(capitalizedWord).append(" ");
        }

        capitalizedName.setLength(capitalizedName.length() - 1);

        return capitalizedName.toString();
    }
}

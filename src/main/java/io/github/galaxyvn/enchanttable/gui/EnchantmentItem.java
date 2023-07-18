package io.github.galaxyvn.enchanttable.gui;

import io.github.galaxyvn.enchanttable.EnchantTable;
import io.github.galaxyvn.enchanttable.manager.items.ItemBuilder;
import io.github.galaxyvn.enchanttable.util.HexUtils;
import io.github.galaxyvn.enchanttable.util.Utils;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.advancedslot.AdvancedSlot;
import mc.obliviate.inventory.advancedslot.AdvancedSlotManager;
import mc.obliviate.inventory.pagination.PaginationManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EnchantmentItem extends Gui {

    private final PaginationManager pagination = new PaginationManager(this);
    private final AdvancedSlotManager advancedSlotManager = new AdvancedSlotManager(this);
    private final Icon blank = new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(HexUtils.colorify("&r"));

    private final ItemStack itemStack;
    private final Enchantment enchantment;
    private final int bookshelfPower;

    private final List<Integer> blackList = new ArrayList<>();

    FileConfiguration config = EnchantTable.getPlugin().getConfig();

    private final boolean sortMissing;
    private final boolean sort;
    private final boolean revert;

    public EnchantmentItem(Player player, Enchantment enchantment, ItemStack itemStack, int bookshelfPower, boolean sortMissing, boolean sort, boolean revert) {
        super(player, "enchant-item", HexUtils.colorify("&0Phù Phép Vật Phẩm ➜ " + Utils.getCapitalizedName(enchantment.getKey().getKey().replace("_", " "))), 6);
        this.itemStack = itemStack;
        this.enchantment = enchantment;
        this.bookshelfPower = bookshelfPower;
        this.sortMissing = sortMissing;
        this.sort = sort;
        this.revert = revert;

        // Pagination
        pagination.registerPageSlotsBetween(21, 25);

        // Blacklist
        blackList.addAll(Arrays.asList(19, 23, 28, 48, 49, 50));
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(blank, blackList);

        // Item Slot
        AdvancedSlot advancedSlot = advancedSlotManager.addAdvancedIcon(19, new Icon(Material.RED_STAINED_GLASS_PANE));
        advancedSlotManager.putIconToAdvancedSlot(advancedSlot, itemStack);
        advancedSlot.onPickup((clickEvent, item) ->
                new EnchantmentGui(player, bookshelfPower, new ItemStack(Material.AIR), sortMissing, sort, revert).open()
        );

        addItem(23, new ItemBuilder(Material.GRAY_DYE)
                .setName("&cPhù Phép Vật Phẩm")
                .addLore(Arrays.asList(
                        "&7Đặt vật phẩm vào ô trống mở",
                        "&7để phù phép nó!"
                ))
                .build()
        );
        addItem(28, new ItemBuilder(Material.ENCHANTING_TABLE)
                .setName("&aPhù Phép Vật Phẩm")
                .addLore(Arrays.asList(
                        "&7Thêm hoặc xóa các phù phép đến từ",
                        "&7vật phẩm ở ô trên."
                ))
                .build()
        );

        addItem(45, new Icon(Material.ARROW)
                .setName("§aQuay Trở Lại")
                .appendLore("§7Đến Phù Phép Vật Phẩm")
                .onClick(clickEvent -> {
                    ItemStack itemInput = advancedSlot.getItemStack();
                    advancedSlot.setRefundOnClose(false);
                    new EnchantmentGui(player, bookshelfPower, itemInput, sortMissing, sort, revert).open();
                })
        );
        addItem(48, new ItemBuilder(Material.BOOKSHELF)
                .setName("&dSức Mạnh Tủ Sách")
                .addLore(Arrays.asList(
                        "&7Phù phép mạnh hơn đòi hỏi nhiều",
                        "&7sức mạnh của giá sách hơn, sức",
                        "&7mạnh này có thể tăng lên bằng",
                        "&7cách đặt các giá sách gần đó.",
                        "",
                        "&7Sức mạnh tủ sách hiện tại: &d" + bookshelfPower
                ))
                .build()
        );
        addItem(49, new Icon(Material.BARRIER).setName(HexUtils.colorify("&cĐóng")).onClick(clickEvent ->
                player.closeInventory())
        );
        addItem(50, new Icon(new ItemBuilder(Material.BOOK)
                        .setName("&aHướng Dẫn Phù Phép")
                        .addLore(Arrays.asList(
                                "&7Xem danh sách đầy đủ tất cả",
                                "&7các phù phép và yêu cầu của",
                                "&7chúng.",
                                "",
                                "&eNhấn để xem!"
                        ))
                        .build()).onClick(clickEvent ->
                new EnchantmentsGuide(player, bookshelfPower, sortMissing, sort, revert, "").open())
        );

        if (advancedSlot.getItemStack() != null && !advancedSlot.getItemStack().getType().isAir()) {
            calculateAndUpdatePagination(itemStack, advancedSlot);
        }
    }

    private void calculateAndUpdatePagination(ItemStack item, AdvancedSlot advancedSlot) {
        pagination.getItems().clear();

        int level = enchantment.getStartLevel();
        int itemLevel = item.getEnchantmentLevel(enchantment);
        String itemName = item.getItemMeta().getDisplayName().isEmpty() ? Utils.getCapitalizedName(item.getType().name().replace("_", " ").toLowerCase()) : item.getItemMeta().getDisplayName();
        String enchantmentName = Utils.getCapitalizedName(enchantment.getKey().getKey().replace("_", " "));

        while (level <= enchantment.getMaxLevel()) {
            if (level < itemLevel) {
                pagination.addItem(new Icon(
                        new ItemBuilder(Material.GRAY_DYE)
                                .setName(HexUtils.colorify("&9" + enchantmentName  + " " + Utils.getRomanLevel(level)))
                                .addDescription(Utils.getEnchantmentDescription(enchantment))
                                .addLore("&7Chi Phí")
                                .addLore("&3<cost> Cấp Độ Kinh Nghiệm".replace("<cost>", String.valueOf(config.getInt("enchant-cost." + enchantment.getKey().getKey().replace("_", "-") + "." + level))))
                                .addLore("")
                                .addLore("&cĐã có sẵn cấp độ cao hơn!")
                                .build()
                ).onClick(clickEvent ->
                        player.sendMessage(HexUtils.colorify("&cVật phẩm này đã có cấp độ cao hơn của phù phép đó!"))));
            } else if (level == itemLevel) {
                int cost = config.getInt("enchant-cost." + enchantment.getKey().getKey().replace("_", "-") + "." + level);
                pagination.addItem(new Icon(
                        new ItemBuilder(Material.ENCHANTED_BOOK)
                                .setName(HexUtils.colorify("&9" + enchantmentName + " " + Utils.getRomanLevel(level)))
                                .addDescription(Utils.getEnchantmentDescription(enchantment))
                                .addLore("&cPhù phép đã có sẵn và")
                                .addLore("&ccó thể được loại bỏ.")
                                .addLore("")
                                .addLore("&7Chi Phí")
                                .addLore("&3<cost> Cấp Độ Kinh Nghiệm".replace("<cost>", String.valueOf(cost)))
                                .addLore("")
                                .addLore("&eNhấn để xóa bỏ!")
                                .build()
                ).onClick(clickEvent -> {
                    // Remove Enchant
                    if (player.getLevel() < cost) {
                        player.sendMessage(HexUtils.colorify("&cBạn không có đủ cấp độ kinh nghiệm để làm điều này!"));
                        return;
                    }
                    player.setLevel(player.getLevel() - cost);
                    player.sendMessage(HexUtils.colorify("&cBạn đã xóa " + enchantmentName + " khỏi " + itemName + "&c của bạn!"));
                    advancedSlot.getItemStack().removeEnchantment(enchantment);
                    calculateAndUpdatePagination(advancedSlot.getItemStack(), advancedSlot);
                }));
            } else {
                int cost = config.getInt("enchant-cost." + enchantment.getKey().getKey().replace("_", "-") + "." + level);
                int finalLevel = level;
                ItemStack finalItem = advancedSlot.getItemStack();

                List<Enchantment> conflictEnchantments = new ArrayList<>();
                for (Enchantment conflictEnchantment : item.getEnchantments().keySet()) {
                    if (enchantment.equals(conflictEnchantment)) continue;
                    if (enchantment.conflictsWith(conflictEnchantment))
                        conflictEnchantments.add(conflictEnchantment);
                }

                String listConflict = "";
                if (!conflictEnchantments.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (Enchantment conflictEnchantment : conflictEnchantments)
                        sb.append(Utils.getCapitalizedName(conflictEnchantment.getKey().getKey().replace("_", " "))).append(", ");
                    if (sb.length() > 0)
                        sb.setLength(sb.length() - 2);
                    listConflict = sb.toString();
                }

                pagination.addItem(new Icon(
                        new ItemBuilder(Material.ENCHANTED_BOOK)
                                .setName(HexUtils.colorify("&9" + enchantmentName + " " + Utils.getRomanLevel(level)))
                                .addDescription(Utils.getEnchantmentDescription(enchantment))
                                .addLore("&7Chi Phí")
                                .addLore("&3<cost> Cấp Độ Kinh Nghiệm".replace("<cost>", String.valueOf(config.getInt("enchant-cost." + enchantment.getKey().getKey().replace("_", "-") + "." + level))))
                                .addLore(
                                        !conflictEnchantments.isEmpty()
                                        ? Arrays.asList("", "&c&lLƯU Ý: Việc này có thể loại bỏ", "&c&lphù phép " + listConflict + ".", "")
                                        : Arrays.asList("")
                                )
                                .addLore("&eNhấn để phù phép!")
                                .build()
                ).onClick(clickEvent -> {
                    // Add Enchant
                    if (!conflictEnchantments.isEmpty()) {
                        // Set player exp level
                        if (player.getLevel() < cost) {
                            player.sendMessage(HexUtils.colorify("&cBạn không có đủ cấp độ exp để làm điều này!"));
                            return;
                        }
                        player.setLevel(player.getLevel() - cost);

                        // Remove conflict
                        for (Enchantment conflictEnchantment : conflictEnchantments)
                            finalItem.removeEnchantment(conflictEnchantment);

                        // Enchant item
                        finalItem.addEnchantment(enchantment, finalLevel);

                        // Send Success Message
                        player.sendMessage(HexUtils.colorify("&aBạn đã phù phép " + itemName + "&a của bạn với " + enchantmentName + " " + Utils.getRomanLevel(finalLevel) + "!"));

                        // Update
                        calculateAndUpdatePagination(advancedSlot.getItemStack(), advancedSlot);
                    } else {
                        // Set player exp level
                        if (player.getLevel() < cost) {
                            player.sendMessage(HexUtils.colorify("&cBạn không có đủ cấp độ exp để làm điều này!"));
                            return;
                        }
                        player.setLevel(player.getLevel() - cost);

                        // Enchant item
                        finalItem.addEnchantment(enchantment, finalLevel);

                        // Send Success Message
                        player.sendMessage(HexUtils.colorify("&aBạn đã phù phép " + itemName + "&a của bạn với " + enchantmentName + " " + Utils.getRomanLevel(finalLevel) + "!"));

                        // Update
                        calculateAndUpdatePagination(advancedSlot.getItemStack(), advancedSlot);
                    }
                }));
            }
            level++;
        }

        // Fill the blank slot
        int leftSlot = pagination.getSlots().size() - pagination.getItems().size();
        int start = 0;

        while (start++ < leftSlot)
            pagination.addItem(blank);

        pagination.update();
    }
}

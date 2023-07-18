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
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class EnchantmentGui extends Gui {

    private final PaginationManager pagination = new PaginationManager(this);
    private final AdvancedSlotManager advancedSlotManager = new AdvancedSlotManager(this);
    private final List<Integer> refill = new ArrayList<>();
    private final List<Integer> blackList = new ArrayList<>();

    private final int bookshelfPower;
    private final ItemStack itemStack;

    private final Icon blank = new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(HexUtils.colorify("&r"));

    private final boolean sortMissing;
    private final boolean sort;
    private final boolean revert;

    public EnchantmentGui(Player player, int bookshelfPower, ItemStack itemStack, boolean sortMissing, boolean sort, boolean revert) {
        super(player, "enchant-gui", HexUtils.colorify("&0Phù Phép Vật Phẩm"), 6);
        this.bookshelfPower = bookshelfPower;
        this.itemStack = itemStack;
        this.sortMissing = sortMissing;
        this.sort = sort;
        this.revert = revert;
        IntStream.rangeClosed(12, 16).forEach(refill::add);
        IntStream.rangeClosed(21, 22).forEach(refill::add);
        IntStream.rangeClosed(24, 25).forEach(refill::add);
        IntStream.rangeClosed(30, 34).forEach(refill::add);

        // Pagination
        pagination.registerPageSlotsBetween(12, 16);
        pagination.registerPageSlotsBetween(21, 25);
        pagination.registerPageSlotsBetween(30, 34);

        // Blacklist
        blackList.addAll(Arrays.asList(19, 23, 28, 48, 49, 50, 51));
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(blank, blackList);

        // Item Slot
        AdvancedSlot advancedSlot = advancedSlotManager.addAdvancedIcon(19, new Icon(Material.AIR));
        advancedSlotManager.putIconToAdvancedSlot(advancedSlot, itemStack);

        // Information Item
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

        // Close Item
        addItem(49, new Icon(Material.BARRIER).setName(HexUtils.colorify("&cĐóng")).onClick(clickEvent ->
                player.closeInventory())
        );

        // Enchantment Guide Item
        addItem(50, new Icon(new ItemBuilder(Material.BOOK)
                .setName("&aHướng Dẫn Phù Phép")
                .addLore(Arrays.asList(
                        "&7Xem danh sách đầy đủ tất cả",
                        "&7các phù phép và yêu cầu của",
                        "&7chúng.",
                        "",
                        "&eNhấn để xem!"
                ))
                .build()).onClick(clickEvent -> {
                    new EnchantmentsGuide(player, bookshelfPower, sortMissing, sort, revert, "").open();
                })
        );

        List<String> currentSort = new ArrayList<>();
        if (!sortMissing && !sort && !revert) {
            currentSort.addAll(Arrays.asList("&b► Mặc Định", "&7Các Phù Phép Còn Thiếu Đầu Tiên", "&7A đến Z", "&7Z đến A"));
        } else if (sortMissing && !sort && !revert) {
            currentSort.addAll(Arrays.asList("&7Mặc Định", "&b► Các Phù Phép Còn Thiếu Đầu Tiên", "&7A đến Z", "&7Z đến A"));
        } else if (!sortMissing && sort && !revert) {
            currentSort.addAll(Arrays.asList("&7Mặc Định", "&7Các Phù Phép Còn Thiếu Đầu Tiên", "&b► A đến Z", "&7Z đến A"));
        } else if (!sortMissing && sort && revert) {
            currentSort.addAll(Arrays.asList("&7Mặc Định", "&7Các Phù Phép Còn Thiếu Đầu Tiên", "&7A đến Z", "&b► Z đến A"));
        }

        // Sort Item
        addItem(51, new Icon(new ItemBuilder(Material.HOPPER)
                .setName("&aSắp Xếp")
                .addLore("&7Thay đổi cách các Phù phép được")
                .addLore("&7sắp xếp.")
                .addLore("")
                .addLore(currentSort)
                .addLore("")
                .addLore("&eNhấn để chuyển cách sắp xếp!")
                .build())
                .onClick(clickEvent -> {
                    advancedSlot.setRefundOnClose(false);
                    if (!sortMissing && !sort && !revert) {
                        new EnchantmentGui(player, bookshelfPower, advancedSlot.getItemStack(), true, false, false).open();
                    } else if (sortMissing && !sort && !revert) {
                        new EnchantmentGui(player, bookshelfPower, advancedSlot.getItemStack(), false, true, false).open();
                    } else if (sort && !revert) {
                        new EnchantmentGui(player, bookshelfPower, advancedSlot.getItemStack(), false, true, true).open();
                    } else if (sort && revert) {
                        new EnchantmentGui(player, bookshelfPower, advancedSlot.getItemStack(), false, false, false).open();
                    }
                })
        );

        if (advancedSlot.getItemStack() != null && !advancedSlot.getItemStack().getType().isAir()) {
            calculateAndUpdatePagination(advancedSlot.getItemStack(), advancedSlot);
        }

        advancedSlot.onPrePutClick((clickEvent, item) -> {
            if (Utils.getEnchantmentsForItem(item, false, false, false).isEmpty()) {
                player.sendMessage("Bạn không thể đặt vật phẩm này vào được!");
                return true;
            }
            return false;
        }).onPut((clickEvent, item) -> calculateAndUpdatePagination(item, advancedSlot));

        advancedSlot.onPickup((clickEvent, item) -> {
            refill.forEach(slot -> addItem(slot, blank));
            addItem(23, new ItemBuilder(Material.GRAY_DYE)
                    .setName("&cPhù Phép Vật Phẩm")
                    .addLore(Arrays.asList(
                            "&7Đặt vật phẩm vào ô trống mở",
                            "&7để phù phép nó!"
                    ))
                    .build()
            );
        });


    }

    private void calculateAndUpdatePagination(ItemStack item, AdvancedSlot advancedSlot) {

        // Clear Items Before
        pagination.getItems().clear();

        // Start adding item
        for (Enchantment enchantment : Utils.getEnchantmentsForItem(item, sortMissing, sort, revert)) {
            int requireBookshelfPower = EnchantTable.getPlugin().getConfig().getInt("bookshelf-power." + enchantment.getKey().getKey().replace("_", "-"));

            pagination.addItem(new Icon(
                    new ItemBuilder(Material.ENCHANTED_BOOK)
                            .setName(HexUtils.colorify("&a" + Utils.getCapitalizedName(enchantment.getKey().getKey().replace("_", " "))))
                            .addDescription(Utils.getEnchantmentDescription(enchantment))
                            .addLore(
                                    item.containsEnchantment(enchantment)
                                            ? "&a  " + Utils.getCapitalizedName(enchantment.getKey().getKey().replace("_", " ")) + " " + Utils.getRomanLevel(item.getEnchantmentLevel(enchantment)) + " ✔"
                                            : "&c  " + Utils.getCapitalizedName(enchantment.getKey().getKey().replace("_", " ")) + " &l✖"
                            )
                            .addLore("")
                            .addLore(
                                    bookshelfPower < requireBookshelfPower
                                            ? "&cYêu cầu <bookshelfPower> Sức Mạnh Tủ Sách!".replace("<bookshelfPower>", String.valueOf(requireBookshelfPower))
                                            : "&eNhấn để xem!"
                            )
                            .build()
            ).onClick(clickEvent -> {
                if (EnchantTable.getPlugin().getConfig().contains("bookshelf-power." + enchantment.getKey().getKey().replace("_", "-")))
                    if (bookshelfPower < requireBookshelfPower) return;

                advancedSlot.setRefundOnClose(false);
                new EnchantmentItem(player, enchantment, advancedSlot.getItemStack(), bookshelfPower, sortMissing, sort, revert).open();
            }));
        }

        // Fill the blank slot
        int leftSlot = pagination.getSlots().size() - pagination.getItems().size();
        int start = 0;

        while (start++ < leftSlot)
            pagination.addItem(blank);

        // Update
        pagination.update();
    }
}
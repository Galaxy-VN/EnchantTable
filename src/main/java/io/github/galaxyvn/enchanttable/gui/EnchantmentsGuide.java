package io.github.galaxyvn.enchanttable.gui;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import de.rapha149.signgui.SignGUI;
import io.github.galaxyvn.enchanttable.EnchantTable;
import io.github.galaxyvn.enchanttable.manager.items.ItemBuilder;
import io.github.galaxyvn.enchanttable.util.HexUtils;
import io.github.galaxyvn.enchanttable.util.Utils;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class EnchantmentsGuide extends Gui {

    private final PaginationManager pagination = new PaginationManager(this);
    private final List<Integer> blackList = new ArrayList<>();
    private final int bookshelfPower;

    private final Icon blank = new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(HexUtils.colorify("&r"));

    private final boolean sortMissing;
    private final boolean sort;
    private final boolean revert;
    private final String searchEnchantment;

    public EnchantmentsGuide(Player player, int bookshelfPower, boolean sortMissing, boolean sort, boolean revert, String searchEnchantment) {
        super(player, "enchantments-guide", !searchEnchantment.isEmpty() ? HexUtils.colorify("&0(1/1) Hướng Dẫn Phù Phép") :  HexUtils.colorify("&0(1/2) Hướng Dẫn Phù Phép"), 6);
        this.bookshelfPower = bookshelfPower;
        this.sortMissing = sortMissing;
        this.sort = sort;
        this.revert = revert;
        this.searchEnchantment = searchEnchantment;

        pagination.registerPageSlotsBetween(10, 16);
        pagination.registerPageSlotsBetween(19, 25);
        pagination.registerPageSlotsBetween(28, 34);
        pagination.registerPageSlotsBetween(37, 43);

        blackList.addAll(Arrays.asList(4, 45, 48, 49, 50, 53));
        IntStream.rangeClosed(10, 16).forEach(blackList::add);
        IntStream.rangeClosed(19, 25).forEach(blackList::add);
        IntStream.rangeClosed(28, 34).forEach(blackList::add);
        IntStream.rangeClosed(37, 43).forEach(blackList::add);
        IntStream.rangeClosed(48, 49).forEach(blackList::add);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(blank, blackList);

        addItem(4, new ItemBuilder(Material.BOOK)
                .setName("&aHướng Dẫn Phù Phép")
                .addLore(Arrays.asList(
                        "&7Xem danh sách đầy đủ tất cả",
                        "&7các phù phép và yêu cầu của",
                        "&7chúng."
                ))
                .build()
        );

        addItem(48, new Icon(Material.ARROW)
                .setName("§aQuay Trở Lại")
                .appendLore("§7Đến Phù Phép Vật Phẩm")
                .onClick(clickEvent -> new EnchantmentGui(player, bookshelfPower, new ItemStack(Material.AIR), sortMissing, sort, revert).open())
        );
        addItem(49, new Icon(Material.BARRIER).setName(HexUtils.colorify("&cĐóng")).onClick(clickEvent ->
                player.closeInventory()));
        addItem(50, new Icon(new ItemBuilder(Material.OAK_SIGN)
                .setName("&aTìm Kiếm")
                .addLore("&7tìm kiếm phù phép cụ thể trong")
                .addLore("&7hướng dẫn.")
                .addLore(
                        searchEnchantment.isEmpty()
                        ? Arrays.asList("")
                        : Arrays.asList(
                                "",
                                "&7Lọc tìm kiếm: &e" + searchEnchantment,
                                "",
                                "&bNhấp chuột trái để xóa lọc!"
                        )
                )
                .addLore(
                        searchEnchantment.isEmpty()
                        ? "&eNhấn để tìm kiếm!"
                        : "&eNhấn để chỉnh sửa lọc tìm kiếm!"
                )
                .build())
                .onClick(clickEvent -> {
                    if (!searchEnchantment.isEmpty() && clickEvent.isLeftClick()) {
                        new EnchantmentsGuide(player, bookshelfPower, sortMissing, sort, revert, "").open();
                        return;
                    }
                    new SignGUI()
                            .lines("", "", "^^^^^^^^^^^^^^^", "Nhập Truy Vấn")
                            .type(Material.OAK_SIGN)
                            .onFinish((p, lines) -> {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        new EnchantmentsGuide(player, bookshelfPower, sortMissing, sort, revert, lines[0]).open();
                                    }
                                }.runTask(EnchantTable.getPlugin());
                                return null;
                            })
                            .open(player);
                })
        );

        calculateAndUpdatePagination();
        if (!pagination.isFirstPage()) {
            addItem(45, new Icon(XMaterial.ARROW.parseItem())
                    .setName("§aTrang Trước")
                    .appendLore("§eTrang " + pagination.getCurrentPage())
                    .onClick(e -> {
                pagination.goPreviousPage();
                sendTitleUpdate(HexUtils.colorify("&0(" + (pagination.getCurrentPage() + 1) + "/" + (pagination.getLastPage() + 1) + ") Hướng Dẫn Phù Phép"));
            }));
        } else addItem(45, blank);
        if (!pagination.isLastPage()) {
            addItem(53, new Icon(XMaterial.ARROW.parseItem())
                    .setName("§aTrang Tiếp")
                    .appendLore("§eTrang " + (pagination.getCurrentPage() + 2))
                    .onClick(e -> {
                pagination.goNextPage();
                sendTitleUpdate(HexUtils.colorify("&0(" + (pagination.getCurrentPage() + 1) + "/" + (pagination.getLastPage() + 1) + ") Hướng Dẫn Phù Phép"));
            }));
        } else addItem(53, blank);
    }

    private void calculateAndUpdatePagination() {
        pagination.getItems().clear();

        if (!searchEnchantment.isEmpty()) {
            for (Enchantment enchantment : Utils.searchEnchantmentsName(searchEnchantment)) {
                List<String> conflicts = new ArrayList<>();
                for (Enchantment other : Enchantment.values()) {
                    if (enchantment.equals(other))
                        continue;
                    if (enchantment.conflictsWith(other)) {
                        conflicts.add("&7 - &c" + Utils.getCapitalizedName(other.getKey().getKey().replace("_", " ")));
                    }
                }
                if (!conflicts.isEmpty()) conflicts.addAll(0, Arrays.asList("", "&6Xung Đột:"));

                pagination.addItem(new Icon(
                        new ItemBuilder(Material.ENCHANTED_BOOK)
                                .setName(HexUtils.colorify("&a" + Utils.getCapitalizedName(enchantment.getKey().getKey().replace("_", " ")) + " " + Utils.getRomanLevel(enchantment.getMaxLevel())))
                                .addDescription(Utils.getEnchantmentDescription(enchantment))
                                .addLore(Utils.getSources(enchantment))
                                .addLore(Utils.getApplied(enchantment))
                                .addLore(conflicts)
                                .build()
                ));
            }
        } else {
            for (XEnchantment xenchantment : XEnchantment.values()) {
                if (!xenchantment.isSupported()) continue;
                Enchantment enchantment = xenchantment.getEnchant();

                List<String> conflicts = new ArrayList<>();
                for (Enchantment other : Enchantment.values()) {
                    if (enchantment.equals(other))
                        continue;
                    if (enchantment.conflictsWith(other)) {
                        conflicts.add("&7 - &c" + Utils.getCapitalizedName(other.getKey().getKey().replace("_", " ")));
                    }
                }
                if (!conflicts.isEmpty()) conflicts.addAll(0, Arrays.asList("", "&6Xung Đột:"));

                pagination.addItem(new Icon(
                        new ItemBuilder(Material.ENCHANTED_BOOK)
                                .setName(HexUtils.colorify("&a" + Utils.getCapitalizedName(enchantment.getKey().getKey().replace("_", " ")) + " " + Utils.getRomanLevel(enchantment.getMaxLevel())))
                                .addDescription(Utils.getEnchantmentDescription(enchantment))
                                .addLore(Utils.getSources(enchantment))
                                .addLore(Utils.getApplied(enchantment))
                                .addLore(conflicts)
                                .build()
                ));
            }
        }
        pagination.update();
    }
}

package com.moyskleytech.mc.obsidiancore.ui;

import java.util.List;
import java.util.stream.Collectors;

import com.moyskleytech.mc.obsidiancore.ObsidianCore;
import com.moyskleytech.mc.obsidiancore.config.BankingConfig;
import com.moyskleytech.mc.obsidiancore.config.LanguageConfig;
import com.moyskleytech.mc.obsidiancore.storage.BankOre;
import com.moyskleytech.mc.obsidiancore.utils.BankingUtil;
import com.moyskleytech.mc.obsidiancore.utils.Logger;
import com.moyskleytech.mc.obsidiancore.utils.ObsidianUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

public class UI implements InventoryHolder {
    private int currentPage = 0;
    private int numberPage;
    private Player player;
    private @NotNull Inventory inventory;

    public UI(Player player) {
        this.player = player;
    }

    @NotNull
    @Override
    public Inventory getInventory() {

        inventory = Bukkit.createInventory(this, 27, ObsidianUtil.component(BankingConfig.getInstance().getString("name")));

        List<BankOre> bank = ObsidianCore.getInstance().getBankingStorage().retrieveBankStatus(player, null);

        numberPage = (int) Math.ceil(bank.size() / 8.0);

        updateUI();

        return inventory;
    }

    

    @SuppressWarnings("deprecation")
    private void updateUI() {
        var config = BankingConfig.getInstance();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, new ItemStack(Material.RED_STAINED_GLASS_PANE));
        }

        List<BankOre> bank = ObsidianCore.getInstance().getBankingStorage().retrieveBankStatus(player, null);
        int offset = 8 * currentPage;
        for (int i = 0; i < 8 && i + offset < bank.size(); i++) {
            BankOre bo = bank.get(i + offset);
            ItemStack oreStack;
            inventory.setItem(i + config.gui().offset().ore(),
                    oreStack = withTitleAndLore(bo.getMaterial(), null, List.of(
                            "Balance: " + bo.getInBank())));

            if (bo.getInBank() > 0) {
                int withdrawable = bo.getInBank();
                withdrawable = Math.min(withdrawable, new ItemStack(bo.getMaterial()).getMaxStackSize());
                withdrawable = Math.min(withdrawable, BankingUtil.getAmountOfSpaceFor(bo.getStackOfOne(), player.getInventory()));
                if (withdrawable > 0)
                    inventory.setItem(i + config.gui().offset().withdraw(),
                            withTitleAndLore(Material.PLAYER_HEAD, oreStack.getI18NDisplayName(), List.of(
                                    "Withdraw: " + withdrawable), withdrawable));
            }

            int inInventory = BankingUtil.getAmountOfInInventory(bo.getStackOfOne(), player.getInventory());

            if (inInventory > 0) {
                int depositable = inInventory;
                depositable = Math.min(depositable, new ItemStack(bo.getMaterial()).getMaxStackSize());

                inventory.setItem(i + config.gui().offset().deposit(),
                        withTitleAndLore(Material.CHEST, oreStack.getI18NDisplayName(), List.of(
                                "Deposit: " + depositable), depositable));
            }
        }

        if (currentPage > 0)
            inventory.setItem(8, withTitleAndLore(config.gui().previous(), "Back", null, 1));
        inventory.setItem(17, withTitleAndLore(config.gui().close(), "Close", null, 1));
        if (currentPage < numberPage - 1)
            inventory.setItem(26, withTitleAndLore(config.gui().next(), "Next", null, 1));
    }
    @SuppressWarnings("deprecation")
    private ItemStack withTitleAndLore(ItemStack itemStack, String title, List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();

        if (title != null)
            meta.displayName(ObsidianUtil.component(title));
        if (lore != null) {
            try {
                meta.lore(lore.stream().map(l -> ObsidianUtil.component(l)).collect(Collectors.toList()));
            } catch (java.lang.NoSuchMethodError e) {
                meta.setLore(lore);
            }
        }
        if (meta instanceof SkullMeta) {
            SkullMeta smeta = (SkullMeta) meta;
            smeta.setOwningPlayer(player);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack withTitleAndLore(Material m, String title, List<String> lore) {
        var itemStack = new ItemStack(m);
        itemStack.setAmount(itemStack.getMaxStackSize());
        return withTitleAndLore(itemStack, title, lore);
    }

    private ItemStack withTitleAndLore(Material m, String title, List<String> lore, int amount) {
        var itemStack = new ItemStack(m);
        itemStack.setAmount(amount);
        return withTitleAndLore(itemStack, title, lore);
    }

    public void click(InventoryClickEvent event) {
        Logger.trace("Clicked on slot {}", event.getSlot());

        int slot = event.getSlot();
        if (slot == 8) {
            if (currentPage > 0)
                currentPage--;
        }
        if (slot == 17) {
            inventory.close();
        }
        if (slot == 26) {
            if (currentPage < numberPage - 1)
                currentPage++;
        }
        handleOreClick(event.getSlot());
        
        updateUI();
    }

    private void handleOreClick(int slot) {
        var config = BankingConfig.getInstance();
        List<BankOre> bank = ObsidianCore.getInstance().getBankingStorage().retrieveBankStatus(player, null);
        int offset = 8 * currentPage;
        for (int i = 0; i < 8 && i + offset < bank.size(); i++) {
            BankOre bo = bank.get(i + offset);

            if (slot == i + config.gui().offset().withdraw()) {
                int withdrawable = bo.getInBank();
                withdrawable = Math.min(withdrawable, new ItemStack(bo.getMaterial()).getMaxStackSize());
                withdrawable = Math.min(withdrawable, BankingUtil.getAmountOfSpaceFor(new ItemStack(bo.getMaterial()), player.getInventory()));
                if (withdrawable > 0) {
                    if (ObsidianCore.getInstance().getBankingStorage().removeFromPlayerBank(bo.getMaterial(), player, withdrawable,
                            null)) {
                        BankingUtil.addToInventory(bo.getStackOfOne(), withdrawable, player.getInventory());
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);

                        player.sendMessage(LanguageConfig.getInstance().with(player).ore(bo.getMaterial())
                                .withdrawed(withdrawable));
                    } else
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
                } else
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
            }

            var inInventory = BankingUtil.getAmountOfInInventory(bo.getStackOfOne(), player.getInventory());

            if (slot == i + config.gui().offset().deposit()) {
                int depositable = inInventory;
                depositable = Math.min(depositable, new ItemStack(bo.getMaterial()).getMaxStackSize());
                if (depositable > 0) {
                    if (ObsidianCore.getInstance().getBankingStorage().addToPlayerBank(bo.getMaterial(), player, depositable,
                            null)) {
                        BankingUtil.removeFromInventory(bo.getStackOfOne(), depositable, player.getInventory());

                        player.sendMessage(
                                LanguageConfig.getInstance().with(player).ore(bo.getMaterial()).deposited(depositable));
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                    } else
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
                } else
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);

            }
        }
    }

}

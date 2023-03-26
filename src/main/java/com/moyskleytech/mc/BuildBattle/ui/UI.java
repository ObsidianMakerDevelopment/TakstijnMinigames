package com.moyskleytech.mc.BuildBattle.ui;

import java.util.List;
import java.util.stream.Collectors;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

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


public class UI implements InventoryHolder {
    private Player player;
    private  Inventory inventory;

    public UI(Player player) {
        this.player = player;
    }

    
    @Override
    public Inventory getInventory() {
        //TODO: Title
        inventory = Bukkit.createInventory(this, 27, ObsidianUtil.component(""));

        updateUI();

        return inventory;
    }
    private void updateUI() {
        
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

        updateUI();
    }

}

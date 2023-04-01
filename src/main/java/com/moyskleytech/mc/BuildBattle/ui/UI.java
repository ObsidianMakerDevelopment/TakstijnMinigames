package com.moyskleytech.mc.BuildBattle.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

import lombok.Getter;
import net.kyori.adventure.text.Component;

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

public abstract class UI implements InventoryHolder {
    @Getter
    private Player player;
    protected Inventory inventory;
    private List<UI> attachedUIs = new ArrayList<>();

    public UI(Player player) {
        this.player = player;
    }

    public void attach(UI second) {
        attachedUIs.add(second);
    }

    @Override
    public Inventory getInventory() {
        // TODO: Title
        inventory = Bukkit.createInventory(this, getSize(), getTitle());

        updateUI(true);

        return inventory;
    }

    public abstract int getSize();
    public abstract Component getTitle();

    public void updateUI(boolean propagate) {
        if (propagate) {
            attachedUIs.forEach(ui -> ui.updateUI(false));
        }
    }

    protected ItemStack withTitleAndLore(ItemStack itemStack, Component title, List<Component> lore) {
        ItemMeta meta = itemStack.getItemMeta();

        if (title != null)
            meta.displayName(title);
        if (lore != null) {
            meta.lore(lore);
        }
        if (meta instanceof SkullMeta) {
            SkullMeta smeta = (SkullMeta) meta;
            smeta.setOwningPlayer(player);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    protected ItemStack withTitleAndLore(Material m, Component title, List<Component> lore) {
        var itemStack = new ItemStack(m);
        itemStack.setAmount(itemStack.getMaxStackSize());
        return withTitleAndLore(itemStack, title, lore);
    }

    protected ItemStack withTitleAndLore(Material m, Component title, List<Component> lore, int amount) {
        var itemStack = new ItemStack(m);
        itemStack.setAmount(amount);
        return withTitleAndLore(itemStack, title, lore);
    }

    public void click(InventoryClickEvent event) {
        Logger.trace("Clicked on slot {}", event.getSlot());

        updateUI(true);
    }

}

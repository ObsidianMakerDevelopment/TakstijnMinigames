package com.moyskleytech.mc.BuildBattle.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

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

public class VotingUI extends UI {
    private List<VotingUI> attachedUIs = new ArrayList<>();
    private List<AtomicInteger> voteCounts;
    private List<String> themes;
    private Integer currentVote = null;

    public VotingUI(Player player, List<String> themes, List<AtomicInteger> voteCounts) {
        super(player);
        this.themes = themes;
        this.voteCounts = voteCounts;
    }

    public void attach(VotingUI second) {
        attachedUIs.add(second);
    }

    public void detach(VotingUI second) {
        attachedUIs.remove(second);
    }

    @Override
    public void updateUI(boolean propagate) {
        super.updateUI(propagate);
        for (int i = 0; i < themes.size(); i++) {
            String theme = themes.get(i);
            AtomicInteger votes = voteCounts.get(i);
            double votePercentage = (double) votes.get() / (attachedUIs.size() + 1);
            Component item_name = LanguageConfig.getInstance().ui().votingItemName().replace("%theme%", theme)
                    .with(getPlayer()).component();

            List<Component> lore = LanguageConfig
                    .getInstance().ui().votingLore().stream().map(lore_line -> (Component) lore_line.with(getPlayer())
                            .replace("%theme%", theme).replace("%votes%", String.valueOf(votePercentage)).component())
                    .toList();
            ItemStack votingItem = withTitleAndLore(Material.SPRUCE_SIGN, item_name, lore, 1);
            inventory.setItem(i * 9, votingItem);

            ItemStack bar = withTitleAndLore(Material.IRON_BARS, Component.empty(), lore);

            inventory.setItem(i * 9 + 1, bar);

            int number_green = (int) (votePercentage * 7);
            for (int idx = 0; idx < 7; idx++) {
                ItemStack pane = withTitleAndLore(
                        idx < number_green ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        item_name, lore);
                inventory.setItem(i * 9 + 2 + idx, pane);
            }
        }
    }

    public void removeVote() {
        if (currentVote != null)
        {
            voteCounts.get(currentVote.intValue()).decrementAndGet();
            currentVote=null;
        }
    }

    public void click(InventoryClickEvent event) {
        if (currentVote != null)
            voteCounts.get(currentVote.intValue()).decrementAndGet();
        int row = event.getSlot() / 9;
        currentVote = row;
        voteCounts.get(row).incrementAndGet();
        updateUI(true);
    }

    @Override
    public int getSize() {
        return 9 * themes.size();
    }

    @Override
    public Component getTitle() {
        return LanguageConfig.getInstance().ui().votingTitle().with(getPlayer()).component();
    }

}

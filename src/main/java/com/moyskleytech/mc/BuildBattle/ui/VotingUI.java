package com.moyskleytech.mc.BuildBattle.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.utils.Logger;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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

            ItemStack bar = withTitleAndLore(Material.IRON_BARS, Component.empty(), lore,1);

            inventory.setItem(i * 9 + 1, bar);

            int number_green = (int) (votePercentage * 7);
            for (int idx = 0; idx < 7; idx++) {
                ItemStack pane = withTitleAndLore(
                        idx < number_green ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        item_name, lore,1);
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
        event.setCancelled(true);
        if (currentVote != null)
            voteCounts.get(currentVote.intValue()).decrementAndGet();
        int row = event.getSlot() / 9;
        currentVote = row;
        voteCounts.get(row).incrementAndGet();
        updateUI(true);
    }

    @Override
    public int getSize() {
        Logger.trace("VotingUI::getSize {}", themes.size());
        return 9 * themes.size();
    }

    @Override
    public Component getTitle() {
        Component title = LanguageConfig.getInstance().ui().votingTitle().with(getPlayer()).component();
        Logger.trace("VotingUI::getTitle {}", title);

        return title;
    }

}

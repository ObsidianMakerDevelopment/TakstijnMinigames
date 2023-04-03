package com.moyskleytech.mc.BuildBattle.config;

import java.util.List;

import org.bukkit.Material;

import com.moyskleytech.obsidian.material.ObsidianItemTemplate;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

public class VotingItems {
    public List<VoteItem> voteItems = List.of(
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.BROWN_TERRACOTTA)).name("Poop")),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.RED_TERRACOTTA)).name("Bad")),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.ORANGE_TERRACOTTA)).name("Average")),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.YELLOW_TERRACOTTA)).name("Good")),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.BLUE_TERRACOTTA)).name("Excellent")),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.LIME_TERRACOTTA)).name("Exceptional")),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.PAPER)).name("Vote"))
    );
}

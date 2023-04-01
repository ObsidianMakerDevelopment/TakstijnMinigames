package com.moyskleytech.mc.BuildBattle.config;

import java.util.List;

import org.bukkit.Material;

import com.moyskleytech.obsidian.material.ObsidianItemTemplate;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

public class VotingItems {
    public List<VoteItem> voteItems = List.of(
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.BROWN_TERRACOTTA))),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.RED_TERRACOTTA))),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.ORANGE_TERRACOTTA))),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.YELLOW_TERRACOTTA))),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.BLUE_TERRACOTTA))),
        new VoteItem(new ObsidianItemTemplate().material(ObsidianMaterial.wrap(Material.LIME_TERRACOTTA)))
    );
}

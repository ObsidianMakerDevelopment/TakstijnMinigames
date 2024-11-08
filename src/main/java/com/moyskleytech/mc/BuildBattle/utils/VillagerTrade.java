package com.moyskleytech.mc.BuildBattle.utils;

import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;

@Getter
public class VillagerTrade {

    private Profession profession;
    private Level level;
    private ItemStack source;
    private ItemStack source2;
    private ItemStack destination;

    public VillagerTrade(Profession profession, Level level, ItemStack source, ItemStack source2,
            ItemStack destination) {
        this.profession = profession;
        this.level = level;
        this.source = source;
        this.source2 = source2;
        this.destination = destination;

    }

    public VillagerTrade(Profession profession, Level level, ItemStack source, ItemStack destination) {
        this.profession = profession;
        this.level = level;
        this.source = source;
        this.source2 = null;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "{" +
            " profession='" + getProfession() + "'" +
            ", level='" + getLevel() + "'" +
            ", source='" + getSource() + "'" +
            ", source2='" + getSource2() + "'" +
            ", destination='" + getDestination() + "'" +
            "}";
    }
}
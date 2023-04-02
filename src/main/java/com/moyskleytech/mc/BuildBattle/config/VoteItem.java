package com.moyskleytech.mc.BuildBattle.config;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.LanguagePlaceholder;
import com.moyskleytech.obsidian.material.ObsidianItemTemplate;

import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;

@NoArgsConstructor
public class VoteItem {
    public ObsidianItemTemplate item;
    public VoteItem(ObsidianItemTemplate template)
    {
        item = template;
    }
    public VoteItemBuilder forPlayer(Player p) {
        VoteItemBuilder builder = new VoteItemBuilder();
        builder.template=item;
        builder.p = p;
        return builder;
    }

    public static class VoteItemBuilder {
        ObsidianItemTemplate template;
        Player p;

        public ItemStack build() {
            ItemStack item = template.build();
            List<Component> oldLore = item.lore();
            if(oldLore!=null)
                item.lore(oldLore.stream().map(lore->LanguagePlaceholder.of(lore).with(p).component()).toList());
            return item;
        }
    }
}

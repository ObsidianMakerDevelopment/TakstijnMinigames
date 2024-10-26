package com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.plugin.utility;

//https://github.com/RienBijl/Scoreboard-revision/blob/master/src/main/java/rien/bijl/Scoreboard/r/Plugin/Utility/ScoreboardStrings.java
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.LanguagePlaceholder;
import com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.plugin.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardStrings {

    private static final Pattern pattern = Pattern.compile("\\{#[a-fA-F0-9]{6}}");
    private static final Pattern placeholderPattern = Pattern.compile("&#[a-fA-F0-9]{6}");

    public static String make(Player player, String content) {
        return colors(placeholders(player, content));
    }

    public static String removeLastCharacter(String str) {
        String result = null;
        if ((str != null) && (str.length() > 0)) {
            result = str.substring(0, str.length() - 1);
        }
        return result;
    }

    public static List<LanguagePlaceholder> makeColoredStringList(List<LanguagePlaceholder> list) {
        return list;

        // List<LanguagePlaceholder> newList = new ArrayList<>();

        // for (LanguagePlaceholder str: list) {
        // newList.add(colors(str));
        // }

        // return newList;
    }

    public static String colors(String content) {
        Matcher match = pattern.matcher(content);
        while (match.find()) {
            String color = content.substring(match.start(), match.end());
            content = content.replace(color, net.md_5.bungee.api.ChatColor.of(color.replaceAll("\\{|}", "")) + "");
            match = pattern.matcher(content);
        }
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', content);
    }

    public static String placeholderColors(String content) {
        if (content.contains("&#")) {
            Matcher match = placeholderPattern.matcher(content);
            while (match.find()) {
                String color = content.substring(match.start(), match.end());
                content = content.replace(color, net.md_5.bungee.api.ChatColor.of(color.replaceAll("&", "")) + "");
                match = placeholderPattern.matcher(content);
            }
            return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', content);
        }
        return content;
    }

    public static String placeholders(Player player, String content) {
        if (Session.getSession().enabled_dependencies.contains(Session.getSession().dependencies[0])
                && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") &&
                PlaceholderAPI.containsPlaceholders(content)) {
            return placeholderColors(PlaceholderAPI.setPlaceholders(player, content));
        }

        return content;
    }

}
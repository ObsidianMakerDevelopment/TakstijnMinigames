package com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.board;

//https://github.com/RienBijl/Scoreboard-revision/blob/master/src/main/java/rien/bijl/Scoreboard/r/Board/ConfigBoard.java
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.LanguagePlaceholder;
import com.moyskleytech.mc.BuildBattle.scoreboard.Scoreboard;
import com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.board.animations.Row;
import com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.board.implementations.WrapperBoard;
import com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.plugin.ConfigControl;
import com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.plugin.utility.ScoreboardStrings;
import com.moyskleytech.mc.BuildBattle.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

public class ConfigBoard {

    public String board;
    private String objectiveName = "";
    private Row title;
    private ArrayList<Row> rows = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private HashMap<Player, WrapperBoard> playerToBoard = new HashMap<>();
    private boolean enabled;

    public ConfigBoard(String board) {
        this.board = board;
        enabled = true;
        this.initTitle();
        this.initRows();
    }

    public void setObjective(String objectiveName) {
        this.objectiveName = objectiveName;
    }

    public void setTitle(List<LanguagePlaceholder> animation, long interval) {
        this.title = new Row(ScoreboardStrings.makeColoredStringList(animation), (int) interval);
    }

    private void initTitle() {
        this.title = new Row(ScoreboardStrings.makeColoredStringList(List.of()), 0);
    }

    private void initRows() {
        /*
         * for (int i = 1; i < 200; i++) {
         * ConfigurationSection section =
         * ConfigControl.get().gc("settings").getConfigurationSection(this.board +
         * ".rows." + i);
         * if (section != null) {
         * Row row = new
         * Row(ScoreboardStrings.makeColoredStringList(section.getStringList("lines")),
         * section.getInt("interval"));
         * rows.add(row);
         * }
         * }
         */
    }

    public void setLines(List<LanguagePlaceholder> lines) {
        for (int i = 0; i < lines.size() && i < rows.size(); i++) {
            rows.set(i, new Row(ScoreboardStrings.makeColoredStringList(List.of(lines.get(i))), 0));
        }
        while (rows.size() < lines.size()) {
            rows.add(new Row(ScoreboardStrings.makeColoredStringList(List.of(lines.get(rows.size()))), 0));
        }
        while (rows.size() > lines.size()) {
            rows.remove(rows.size() - 1);
        }
    }

    public void hookPlayer(Player player) {
        players.add(player);

        try {
            WrapperBoard wrapperBoard = new WrapperBoard("SCOREBOARD_DRIVER_V1");
            wrapperBoard.setObjective(objectiveName);
            wrapperBoard.setLineCount(rows.size());
            wrapperBoard.setPlayer(player);
            playerToBoard.put(player, wrapperBoard);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void unhookPlayer(Player player) {
        playerToBoard.remove(player);
        players.remove(player);
        player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
    }

    private String[] chrs = new String[] { ChatColor.COLOR_CHAR + "0", ChatColor.COLOR_CHAR + "1",
            ChatColor.COLOR_CHAR + "2", ChatColor.COLOR_CHAR + "3", ChatColor.COLOR_CHAR + "4",
            ChatColor.COLOR_CHAR + "5", ChatColor.COLOR_CHAR + "6", ChatColor.COLOR_CHAR + "7",
            ChatColor.COLOR_CHAR + "8", ChatColor.COLOR_CHAR + "9", ChatColor.COLOR_CHAR + "a",
            ChatColor.COLOR_CHAR + "b", ChatColor.COLOR_CHAR + "c", ChatColor.COLOR_CHAR + "d",
            ChatColor.COLOR_CHAR + "e", ChatColor.COLOR_CHAR + "f" };

    public void run() {
        if (!this.enabled)
            return;

        this.title.update();

        for (Row row : rows) {
            row.update();
        }

        for (Player player : playerToBoard.keySet()) {
            WrapperBoard wrapperBoard = playerToBoard.get(player);
            wrapperBoard.setTitle(this.title.getLine());
            wrapperBoard.setLineCount(rows.size());

            int count = 0;
            for (Row row : new ArrayList<>(rows)) {
                wrapperBoard.setLine(count, chrs[count]+ChatColor.COLOR_CHAR+"r"+row.getLine());
                count++;
            }
        }
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public @Nullable WrapperBoard of(Player p) {
        return playerToBoard.get(p);
    }
}
package com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.board.animations;

//https://github.com/RienBijl/Scoreboard-revision/blob/master/src/main/java/rien/bijl/Scoreboard/r/Board/Animations/Row.java
import java.util.List;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.LanguagePlaceholder;

public class Row {

    private List<LanguagePlaceholder> lines;
    private int interval;
    private int count = 0;
    private int current = 1;
    private boolean is_static = false;

    private LanguagePlaceholder line;

    public Row(List<LanguagePlaceholder> lines, int interval) {
        this.lines = lines;
        this.interval = interval;

        if (lines.size() < 1) {
            line = LanguagePlaceholder.of("");
        } else {
            this.line = lines.get(0);
        }
    }

    public String getLine() {
        return this.line.string();
    }

    public void update() {
        if (is_static) {
            return;
        }

        if (count >= interval) {
            count = 0;
            current++;

            if (current >= lines.size()) {
                current = 0;
            }

            line = lines.get(current);

        } else {
            count++;
        }

    }

}
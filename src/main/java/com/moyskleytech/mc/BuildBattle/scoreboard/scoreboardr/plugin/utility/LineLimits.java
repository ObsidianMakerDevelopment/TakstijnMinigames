package com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.plugin.utility;

//https://github.com/RienBijl/Scoreboard-revision/blob/master/src/main/java/rien/bijl/Scoreboard/r/Plugin/Utility/LineLimits.java
public class LineLimits {

    private static int lineLimit = 0;

    public static int getLineLimit() {
        if (lineLimit != 0) {
            return lineLimit;
        }

        lineLimit = 64;

        return lineLimit;
    }

}
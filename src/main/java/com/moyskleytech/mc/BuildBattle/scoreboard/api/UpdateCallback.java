// 
// Decompiled by Procyon v0.5.36
// 

package com.moyskleytech.mc.BuildBattle.scoreboard.api;

import com.moyskleytech.mc.BuildBattle.scoreboard.Scoreboard;

@FunctionalInterface
public interface UpdateCallback
{
    boolean onCallback(final Scoreboard p0);
}

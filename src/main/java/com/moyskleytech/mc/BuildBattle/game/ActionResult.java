package com.moyskleytech.mc.BuildBattle.game;

import lombok.Getter;

@Getter
public class ActionResult {

    public static final String MAP_NOT_EXISTING = "error.non_existing_map";

    private String errorKey = null;
    private boolean isSuccess = true;

    public ActionResult(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public ActionResult(String errorKey) {
        this.isSuccess = false;
        this.errorKey = errorKey;
    }

    public static ActionResult success() {
        return new ActionResult(true);
    }

    public static ActionResult failure(String mapNotExisting) {
        return new ActionResult(mapNotExisting);
    }

}

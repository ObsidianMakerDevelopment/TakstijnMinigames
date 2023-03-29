package com.moyskleytech.mc.BuildBattle;

public class Test<T> {
    private static Test instance;

    public static Test<?> getInstance(){
        return instance;
    }

    public Test() {
        instance=this;
    }
    

}

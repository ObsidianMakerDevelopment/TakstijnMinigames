package com.moyskleytech.mc.BuildBattle.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static DateUtils instance=null;
    public static DateUtils getInstance() {
        if(instance==null)
            instance=new DateUtils();
        return instance;
    }

    private final SimpleDateFormat format;

    public DateUtils() {
        format = new SimpleDateFormat(("MM/dd/yy"));
    }

    public static SimpleDateFormat getSimpleDateFormat() {
        return getInstance().format;
    }

    public static String getFormattedDate() {
        return getSimpleDateFormat().format(new Date());
    }
}

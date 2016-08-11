package com.intellectualsites.web.core;

import com.intellectualsites.web.object.LogWrapper;

public class DefaultLogWrapper implements LogWrapper {

    @Override
    public void log(String prefix, String prefix1, String timeStamp, String message, String thread) {
        System.out.printf("[%s][%s][%s][%s] %s%s", prefix, prefix1, thread, timeStamp, message, System.lineSeparator());
    }

    @Override
    public void log(String s) {
        System.out.println(s);
    }

}

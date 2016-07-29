package com.intellectualsites.web.thread;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ThreadManager {

    public static void createThread(ThreadTask task) {
        try {
            new Thread() {
                @Override
                public void run() {
                    for (;;) {
                        task.run();
                    }
                }
            }.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}

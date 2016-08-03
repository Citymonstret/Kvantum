package com.intellectualsites.web.thread;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ThreadManager {

    public static void createThread(@NonNull final ThreadTask task) {
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

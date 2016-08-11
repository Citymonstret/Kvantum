package com.plotsquared.iserver.thread;

import com.plotsquared.iserver.util.Assert;

public final class ThreadManager {

    public static void createThread(final ThreadTask task) {
        Assert.notNull(task);

        try {
            new Thread() {
                @Override
                public void run() {
                    for (; ; ) {
                        task.run();
                    }
                }
            }.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}

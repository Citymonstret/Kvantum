package com.plotsquared.iserver.gui;

import com.plotsquared.iserver.core.DefaultLogWrapper;
import com.plotsquared.iserver.core.IntellectualServerMain;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.util.Bootstrap;

import java.io.File;

@Bootstrap
public class GuiMain extends Thread {

    private GuiMain() {
        super.setName("GUI-Thread");
    }

    public static void main(final String[] args) {
        final File file = new File(".");

        new GuiMain().start();

        IntellectualServerMain.startServer(true, file, new DefaultLogWrapper() {

            @Override
            public void log(String prefix, String prefix1, String timeStamp, String message, String thread) {
                super.log(prefix, prefix1, timeStamp, message, thread);
                Console.getConsole().log(prefix, prefix1, timeStamp, message, thread);
            }

            @Override
            public void log(String s) {
                super.log(s);
                Console.getConsole().log(s);
            }
        });
    }

    @Override
    public void run() {
        try {
            sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Server.getInstance().log("GUI Opening...");
        new Frame();
    }

}

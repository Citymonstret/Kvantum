//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2016 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualsites.web.core;

import java.net.Socket;

public class WorkerThread extends Thread {

    private static int idAlloaction = 0;

    private final int id;
    private final Worker task;
    private volatile Socket current;
    private final Server server;

    public WorkerThread(Worker task, Server server) {
        super("Worker Thread: " + ++idAlloaction);
        this.id = idAlloaction;
        this.task = task;
        this.server = server;

    }

    @Override
    public synchronized void start() {
        super.start();
        server.log("Started thread: " + id);
    }

    @Override
    final public void run() {
        Socket current;
        for (;;) {
            String s = ("Checking queue");
            if (!server.queue.isEmpty()) {
                current = server.queue.poll();
                task.run(current, server);
            } else {

            }
        }
    }

}

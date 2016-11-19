/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.gui;

import com.plotsquared.iserver.core.DefaultLogWrapper;
import com.plotsquared.iserver.core.IntellectualServerMain;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.util.Bootstrap;
import com.plotsquared.iserver.util.RequestManager;

import java.io.File;

@Bootstrap
public class GuiMain extends Thread
{

    private GuiMain()
    {
        super.setName( "GUI-Thread" );
    }

    public static void main(final String[] args, final File file)
    {
        new GuiMain().start();

        IntellectualServerMain.startServer( true, file, new DefaultLogWrapper()
        {

            @Override
            public void log(String prefix, String prefix1, String timeStamp, String message, String thread)
            {
                super.log( prefix, prefix1, timeStamp, message, thread );
                Console.getConsole().log( prefix, prefix1, timeStamp, message, thread );
            }

            @Override
            public void log(String s)
            {
                super.log( s );
                Console.getConsole().log( s );
            }
        }, new RequestManager() );
    }

    @Override
    public void run()
    {
        try
        {
            sleep( 2500 );
        } catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
        Server.getInstance().log( "GUI Opening..." );
        new Frame();
    }

}

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

import com.plotsquared.iserver.core.Server;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class Frame extends JFrame
{

    Frame()
    {
        super( "IntellectualServer" );

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        this.setVisible( true );
        this.setSize( screenSize );

        final Dimension consoleDimension = new Dimension( screenSize );
        consoleDimension.setSize( consoleDimension.getWidth() * 0.7, consoleDimension.getHeight() );

        Console.getConsole().setSize( consoleDimension );
        this.getContentPane().add( Console.getConsole(), FlowLayout.LEFT );

        addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                Frame.this.dispose();
                Server.getInstance().stopServer();
            }
        } );
    }

}

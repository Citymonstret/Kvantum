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

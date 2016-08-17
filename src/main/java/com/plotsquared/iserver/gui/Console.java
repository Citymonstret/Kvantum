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

import com.plotsquared.iserver.object.LogWrapper;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;

public class Console extends JTextPane implements LogWrapper
{

    private static Console console = new Console();
    private final Style mainStyle;
    private final Style bracket;
    private final Style bold;

    private Console()
    {
        this.setEditable( false );
        this.mainStyle = this.addStyle( "main", null );
        this.mainStyle.addAttribute( StyleConstants.FontFamily, "Lucida Console" );
        this.bracket = this.addStyle( "bracket", mainStyle );
        this.bracket.addAttribute( StyleConstants.Foreground, Color.DARK_GRAY );
        this.bold = this.addStyle( "bold", mainStyle );
        this.bold.addAttribute( StyleConstants.Bold, true );
    }

    public static Console getConsole()
    {
        return console;
    }

    @Override
    public void log(String prefix, String prefix1, String timeStamp, String message, String thread)
    {
        StyledDocument document = getStyledDocument();
        try
        {
            document.insertString( document.getLength(), "[", bracket );
            document.insertString( document.getLength(), prefix, bold );
            document.insertString( document.getLength(), "]", bracket );
            document.insertString( document.getLength(), "[", bracket );
            document.insertString( document.getLength(), prefix1, bold );
            document.insertString( document.getLength(), "]", bracket );
            document.insertString( document.getLength(), "[", bracket );
            document.insertString( document.getLength(), thread, bold );
            document.insertString( document.getLength(), "]", bracket );
            document.insertString( document.getLength(), "[", bracket );
            document.insertString( document.getLength(), timeStamp, bold );
            document.insertString( document.getLength(), "] ", bracket );
            document.insertString( document.getLength(), message, mainStyle );
            document.insertString( document.getLength(), System.lineSeparator(), null );
        } catch ( BadLocationException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void log(String s)
    {

    }

}

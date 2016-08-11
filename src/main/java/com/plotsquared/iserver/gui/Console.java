package com.plotsquared.iserver.gui;

import com.plotsquared.iserver.object.LogWrapper;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;

public class Console extends JTextPane implements LogWrapper {

    private static Console console = new Console();
    private final Style mainStyle;
    private final Style bracket;
    private final Style bold;
    private Console() {
        this.setEditable(false);
        this.mainStyle = this.addStyle("main", null);
        this.mainStyle.addAttribute(StyleConstants.FontFamily, "Lucida Console");
        this.bracket = this.addStyle("bracket", mainStyle);
        this.bracket.addAttribute(StyleConstants.Foreground, Color.DARK_GRAY);
        this.bold = this.addStyle("bold", mainStyle);
        this.bold.addAttribute(StyleConstants.Bold, true);
    }

    public static Console getConsole() {
        return console;
    }

    @Override
    public void log(String prefix, String prefix1, String timeStamp, String message, String thread) {
        StyledDocument document = getStyledDocument();
        try {
            document.insertString(document.getLength(), "[", bracket);
            document.insertString(document.getLength(), prefix, bold);
            document.insertString(document.getLength(), "]", bracket);
            document.insertString(document.getLength(), "[", bracket);
            document.insertString(document.getLength(), prefix1, bold);
            document.insertString(document.getLength(), "]", bracket);
            document.insertString(document.getLength(), "[", bracket);
            document.insertString(document.getLength(), thread, bold);
            document.insertString(document.getLength(), "]", bracket);
            document.insertString(document.getLength(), "[", bracket);
            document.insertString(document.getLength(), timeStamp, bold);
            document.insertString(document.getLength(), "] ", bracket);
            document.insertString(document.getLength(), message, mainStyle);
            document.insertString(document.getLength(), System.lineSeparator(), null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(String s) {

    }

}

package com.intellectualsites.web.commands;

/**
 * The command super class
 *
 * @author Citymonstret
 */
public abstract class Command {

    /**
     * Handle the command
     *
     * @param args Command arguments
     */
    public abstract void handle(String[] args);

}

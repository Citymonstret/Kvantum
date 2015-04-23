package com.intellectualsites.plugin.test;

import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * Created 2015-04-23 for IntellectualServer
 *
 * @author Citymonstret
 */
public class TestPlugin extends Plugin {

    public TestPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start(){
        System.out.println("Starten");
    }
}

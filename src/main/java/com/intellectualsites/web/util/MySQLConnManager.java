package com.intellectualsites.web.util;

import com.intellectualsites.web.config.YamlConfiguration;
import com.intellectualsites.web.core.Server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * A MySQL Connection utility
 *
 * @author peelsh
 * @author Citymonstret
 */
public class MySQLConnManager {
    private String host;
    private int port;
    private String db;
    private String user;
    private String pass;

    private Connection conn;

    public MySQLConnManager() {
        try {
            YamlConfiguration config = new YamlConfiguration("mysql", new File(new File(Server.getInstance().coreFolder, "config"), "mysql.yml"));
            config.loadFile();
            this.host = config.get("mysql.host", "127.0.0.1");
            this.pass = config.get("mysql.pass", "password");
            this.user = config.get("mysql.user", "root");
            this.port = config.get("mysql.port", 3306);
            this.db = config.get("mysql.db", "database");
            config.saveFile();
        } catch (final Exception e) {
            throw new MySQLInitiationException("Could not load mysql.yml", e);
        }
        log("MySQL manager is created!");
    }

    public Connection getConnection() {
        return conn;
    }

    public void init() {
        String connUrl = "jdbc:mysql://"+host+":"+port+"/"+db;
        try {
            conn = DriverManager.getConnection(connUrl, user, pass);
            log("Connection established.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            // log("MySQL threw error: ", ex.getMessage() + ex.getErrorCode());
        }
    }

    public void log(String message, final Object... args) {
        for (final Object a : args) {
            message = message.replaceFirst("%s", a.toString());
        }
        System.out.printf("[%s][%s] %s\n", Server.PREFIX + "-MySQL", TimeUtil.getTimeStamp(), message);
    }

    private class MySQLInitiationException extends RuntimeException {

        public MySQLInitiationException(final String issue, final Exception cause) {
            super(issue, cause);
        }

    }
}

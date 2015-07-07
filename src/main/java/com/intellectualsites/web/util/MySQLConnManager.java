package com.intellectualsites.web.util;

import com.intellectualsites.web.core.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Liam on 07/07/2015.
 */
public class MySQLConnManager {
    private String host;
    private Integer port;
    private String db;
    private String user;
    private String pass;

    private Connection conn;

    public MySQLConnManager(String host, Integer port, String db, String user, String pass) {
        this.host = host;
        this.port = port;
        this.db = db;
        this.user = user;
        this.pass = pass;
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
            log("MySQL threw error: ", ex.getMessage() + ex.getErrorCode());
        }
    }

    public void log(String message, final Object... args) {
        for (final Object a : args) {
            message = message.replaceFirst("%s", a.toString());
        }
        System.out.printf("[%s][%s] %s\n", Server.PREFIX + "-MySQL", TimeUtil.getTimeStamp(), message);
    }
}

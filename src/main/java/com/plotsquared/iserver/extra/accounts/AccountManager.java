//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
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

package com.plotsquared.iserver.extra.accounts;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Session;
import com.plotsquared.iserver.object.syntax.ProviderFactory;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.SQLiteManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AccountManager implements ProviderFactory<Account> {

    private final List<Account> accountList;
    private final ConcurrentHashMap<Session, Account> sessionAccountMap;
    private final SQLiteManager databaseManager;
    private int id = 0;

    public AccountManager(final SQLiteManager databaseManager) {
        this.accountList = Collections.synchronizedList(new ArrayList<Account>());
        this.sessionAccountMap = new ConcurrentHashMap<>();
        this.databaseManager = databaseManager;
    }

    public boolean load() {
        try {
            databaseManager.executeUpdate("CREATE TABLE IF NOT EXISTS account(id INTEGER PRIMARY KEY, name VARCHAR(32), password VARCHAR(256))");
            PreparedStatement loadAccounts = databaseManager.prepareStatement("SELECT * FROM account");
            ResultSet results = loadAccounts.executeQuery();
            while (results.next()) {
                this.id++;
                int id = results.getInt("id");
                String username = results.getString("name");
                String password = results.getString("password");
                Account account = new Account(id, username, password.getBytes());
                registerAccount(account);
            }
            results.close();
            loadAccounts.close();

            Server.getInstance().log("Loaded " + accountList.size() + " accounts from the database");

            if (accountList.isEmpty()) {
                createAccount(new Account(getNextId(), "test", "test".getBytes()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int getNextId() {
        return id++;
    }

    public void createAccount(final Account account) throws SQLException {
        PreparedStatement statement = databaseManager.prepareStatement("INSERT INTO account(name, password) VALUES(?, ?)");
        statement.setString(1, account.getUsername());
        statement.setString(2, new String(account.getPassword()));
        statement.executeUpdate();
        statement.close();
        registerAccount(account);
    }

    public synchronized Account getAccount(Session session) {
        if (!sessionAccountMap.containsKey(session)) {
            return null;
        }
        return sessionAccountMap.get(session);
    }

    public Account getAccount(final Object[] objects) {
        // 0 = id
        // 1 = username
        // 2 = email
        // ...
        Assert.equals(objects.length >= 2, true);
        if (objects[0] != null) {
            return getAccountById((Integer) objects[0]);
        }
        if (objects[1] != null) {
            return getAccountByUsername(objects[1].toString());
        }
        return null;
    }

    protected Account getAccountById(int id) {
        for (Account account : accountList) {
            if (account.getID() == id) {
                return account;
            }
        }
        return null;
    }

    protected Account getAccountByUsername(String username) {
        for (Account account : accountList) {
            if (account.getUsername().equals(username)) {
                return account;
            }
        }
        return null;
    }

    public void unbindAccount(Session session) {
        if (sessionAccountMap.containsKey(session)) {
            sessionAccountMap.remove(session);
        }
    }

    public synchronized void bindAccount(Session session, Account account) {
        sessionAccountMap.put(session, account);
    }

    public void registerAccount(Account account) {
        accountList.add(account);
    }

    public Session getSession(Account account) {
        if (!sessionAccountMap.containsValue(account)) {
            return null;
        }
        for (Session session : sessionAccountMap.keySet()) {
            if (sessionAccountMap.get(session).equals(account)) {
                return session;
            }
        }
        return null;
    }

    @Override
    public Account get(Request r) {
        return getAccount(r.getSession());
    }

    @Override
    public String providerName() {
        return "account";
    }
}

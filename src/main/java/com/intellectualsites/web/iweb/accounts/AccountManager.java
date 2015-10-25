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

package com.intellectualsites.web.iweb.accounts;

import com.intellectualsites.web.object.Session;
import com.intellectualsites.web.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountManager {

    private final List<Account> accountList;
    private final Map<Session, Account> sessionAccountMap;
    private int id = 0;

    public AccountManager() {
        this.accountList = Collections.synchronizedList(new ArrayList<Account>());
        this.sessionAccountMap = new ConcurrentHashMap<>();

        this.accountList.add(new Account(getNextId(), "test", "test".getBytes()));
    }

    public int getNextId() {
        return id++;
    }

    public Account getAccount(Session session) {
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

    public void bindAccount(Session session, Account account) {
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
}

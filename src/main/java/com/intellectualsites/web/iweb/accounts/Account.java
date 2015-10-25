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

import java.util.UUID;

public class Account {

    private int id;
    private UUID uuid;
    private String username;
    private byte[] password;

    public Account(int id, String username, byte[] password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.uuid = UUID.randomUUID();
    }

    public boolean passwordMatches(final byte[] password) {
        if (password.length != this.password.length) {
            return false;
        }

        for (int i = 0; i < password.length; i++) {
            if (password[i] != this.password[i]) {
                    return false;
            }
        }

        return true;

    }

    @Override
    public String toString() {
        return this.username;
    }

    public int getID() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Account) && ((Account) o).getUUID().equals(getUUID());
    }
}

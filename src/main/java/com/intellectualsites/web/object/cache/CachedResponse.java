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

package com.intellectualsites.web.object.cache;

import com.intellectualsites.web.object.Header;
import com.intellectualsites.web.object.HeaderProvider;
import com.intellectualsites.web.object.Response;
import lombok.NonNull;

public class CachedResponse implements HeaderProvider {

    public final boolean isText;
    public final byte[] bodyBytes; //, headerBytes;
    public final Header header;

    public CachedResponse(@NonNull final Response parent) {
        this.isText = parent.isText();
        this.header = parent.getHeader();
        // this.headerBytes = parent.getHeader().getBytes();
        if (parent.isText()) {
            this.bodyBytes = parent.getContent().getBytes();
        } else {
            this.bodyBytes = parent.getBytes();
        }
    }

    @Override
    public Header getHeader() {
        return header;
    }

}

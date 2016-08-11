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

package com.plotsquared.iserver.views.errors;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.views.View;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Error extends View {

    private final int code;
    private final String desc;

    Error(final int code, final String desc) {
        super("/", "error");
        this.code = code;
        this.desc = desc;
    }

    @Override
    public boolean passes(final Request request) {
        return true;
    }

    @Override
    public Response generate(final Request r) {
        Response response = new Response(this);
        response.setContent(String.format("<p><b>Error:</b> %d %s</p>", this.code, this.desc));
        return response;
    }

}

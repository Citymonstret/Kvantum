//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2016 IntellectualSites                                                                  /
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

package com.intellectualsites.web.core;

import com.intellectualsites.web.config.Message;
import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.object.cache.CachedResponse;
import com.intellectualsites.web.object.syntax.IgnoreSyntax;
import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.syntax.Syntax;
import com.intellectualsites.web.views.View;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Worker {

    public void run(Socket remote, Server server) {
        // Do we want to output a load of useless information?
        if (server.verbose) {
            server.log(Message.CONNECTION_ACCEPTED, remote.getInetAddress());
        }

        // The request object
        Request r;
        // The output stream, that we will write to
        BufferedOutputStream out;
        // The input information
        BufferedReader input;

        {
            StringBuilder rRaw = new StringBuilder();
            try {
                // Create a new reader
                input = new BufferedReader(new InputStreamReader(remote.getInputStream()), server.bufferIn);
                // And writer
                out = new BufferedOutputStream(remote.getOutputStream(), server.bufferOut);

                // Read the request
                String str;
                while ((str = input.readLine()) != null && !str.equals("")) {
                    rRaw.append(str).append("|");
                }
                r = new Request(rRaw.toString(), remote);

                // Fetch the post request, if it exists
                if (r.getQuery().getMethod() == Method.POST) {
                    StringBuilder pR = new StringBuilder();
                    int cl = Integer.parseInt(r.getHeader("Content-Length").substring(1));
                    for (int i = 0; i < cl; i++) {
                        pR.append((char) input.read());
                    }
                    r.setPostRequest(new PostRequest(pR.toString()));
                }
            } catch (final Exception e) {
                e.printStackTrace();
                return;
            }
        }

        // Do we want to output a log?
        if (!server.silent) {
            server.log(r.buildLog());
        }

        // Match a view
        View view = server.viewManager.match(r);

        boolean isText;
        String content = "";
        byte[] bytes = null;

        final HeaderProvider headerProvider;

        // Session management stuff
        Session session = server.sessionManager.getSession(r, out);
        if (session != null) {
            r.setSession(session);
        } else {
            r.setSession(server.sessionManager.createSession(r, out));
        }

        if (server.enableCaching && view instanceof CacheApplicable && ((CacheApplicable) view).isApplicable(r)) {
            if (server.cacheManager.hasCache(view)) {
                CachedResponse response = server.cacheManager.getCache(view);
                if ((isText = response.isText)) {
                    content = new String(response.bodyBytes);
                } else {
                    bytes = response.bodyBytes;
                }
                headerProvider = response;
            } else {
                Response response = view.generate(r);
                headerProvider = response;
                server.cacheManager.setCache(view, response);
                if ((isText = response.isText())) {
                    content = response.getContent();
                } else {
                    bytes = response.getBytes();
                }
            }
        } else {
            Response response = view.generate(r);
            headerProvider = response;
            if ((isText = response.isText())) {
                content = response.getContent();
            } else {
                bytes = response.getBytes();
            }
        }

        for (Map.Entry<String, String> postponedCookie : r.postponedCookies.entrySet()) {
            headerProvider.getHeader().setCookie(postponedCookie.getKey(), postponedCookie.getValue());
        }

        if (isText) {
            // Make sure to not use Crush when
            // told not to
            if (!(view instanceof IgnoreSyntax)) {
                // Provider factories are fun, and so is the
                // global map. But we also need the view
                // specific ones!
                Map<String, ProviderFactory> factories = new HashMap<>();
                for (final ProviderFactory factory : server.providers) {
                    factories.put(factory.providerName().toLowerCase(), factory);
                }
                // Now make use of the view specific ProviderFactory
                ProviderFactory z = view.getFactory(r);
                if (z != null) {
                    factories.put(z.providerName().toLowerCase(), z);
                }
                // This is how the crush engine works.
                // Quite simple, yet powerful!
                for (Syntax syntax : server.syntaxes) {
                    if (syntax.matches(content)) {
                        content = syntax.handle(content, r, factories);
                    }
                }
            }
            // Now, finally, let's get the bytes.
            bytes = content.getBytes();
        }

        headerProvider.getHeader().apply(out);

        try {
            out.write(bytes);
            out.flush();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            input.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        if (!server.silent) {
            server.log("Request was served by '%s', with the type '%s'. The total lenght of the content was '%s'",
                    view.getName(), isText ? "text" : "bytes", bytes.length
            );
        }
    }
}
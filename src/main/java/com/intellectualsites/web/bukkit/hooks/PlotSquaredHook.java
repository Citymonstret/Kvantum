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

package com.intellectualsites.web.bukkit.hooks;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualsites.web.bukkit.IntellectualServerPlugin;
import com.intellectualsites.web.bukkit.plotsquared.*;
import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.logging.LogProvider;
import com.intellectualsites.web.object.Header;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.FileUtils;

import com.intellectualsites.web.views.LessView;
import com.intellectualsites.web.views.decl.ViewDeclaration;
import com.intellectualsites.web.views.decl.ViewMatcher;
import com.intellectualsites.web.views.staticviews.StaticViewManager;
import org.apache.commons.io.IOUtils;
import org.lesscss.LessCompiler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SuppressWarnings({"unused"})
public class PlotSquaredHook extends Hook implements LogProvider, ViewDeclaration {

    private File styleSheet, script, logo, plotTemplate, userTemplate, searchTemplate, entryTemplate, uploadTemplate;

    @Override
    public void load(Server server) {
        log("Loading the plotsquared hook!");

        File file1 = new File(IntellectualServerPlugin.getPlugin(IntellectualServerPlugin.class).getDataFolder(), "plotsquared");
        if  (!file1.exists() && !file1.mkdirs()) {
            log("Couldn't create the main plotsquared folder ('%s')", file1.getAbsolutePath());
            return;
        }
        {
            File file = new File(file1, "/assets/stylesheet.less");
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    log("Failed to create the plotsquared folder");
                }
            }
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        log("Failed to create stylesheet.less");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            styleSheet = file;
        }
        {
            File file = new File(file1, "plot.html");
            if (!file.exists()) {
                boolean c = true;
                try {
                    if (!file.createNewFile()) {
                        c = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    c = false;
                }
                if (!c) {
                    log("Couldn't create plot template file...");
                    return;
                }
            }
            this.plotTemplate = file;
        }
        {
            File file = new File(file1, "user.html");
            if (!file.exists()) {
                boolean c = true;
                try {
                    if (!file.createNewFile()) {
                        c = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    c = false;
                }
                if (!c) {
                    log("Couldn't create user template file...");
                    return;
                }
            }
            this.userTemplate = file;
        }
        {
            File file = new File(file1, "search.html");
            if (!file.exists()) {
                boolean c = true;
                try {
                    if (!file.createNewFile()) {
                        c = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    c = false;
                }
                if (!c) {
                    log("Couldn't create search template file...");
                    return;
                }
            }

            this.searchTemplate = file;
        }
        {
            File file = new File(file1, "upload.html");
            if (!file.exists()) {
                boolean c = true;
                try {
                    if (!file.createNewFile()) {
                        c = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    c = false;
                }
                if (!c) {
                    log("Couldn't create upload template file...");
                    return;
                }
            }

            this.uploadTemplate = file;
        }
        {
            File file = new File(file1, "search_entry.html");
            if (!file.exists()) {
                boolean c = true;
                try {
                    if (!file.createNewFile()) {
                        c = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    c = false;
                }
                if (!c) {
                    log("Couldn't create search entry template file...");
                    return;
                }
            }
            this.entryTemplate = file;
        }
        {
            File file = new File(file1, "/assets/script.js");
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    log("Failed to create the plotsquared folder");
                }
            }
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        log("Failed to create script.js");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            script = file;
        }
        {
            File logo = new File(file1, "/assets/logo.png");
            if (!logo.exists()) {
                log("There is no logo.png, we'll skip this request for now!");
                this.logo = null;
            } else {
                this.logo = logo;
            }
        }
        server.getViewManager().clear();
        server.getViewManager().add(new MainView(file1));
        server.getViewManager().add(new GetSchematic());
        server.getViewManager().add(new PlotInfo(plotTemplate));
        server.getViewManager().add(new UserInfo(userTemplate));
        server.getViewManager().add(new SearchView(searchTemplate, entryTemplate));
        server.getViewManager().add(new UploadView(uploadTemplate));
        try {
            StaticViewManager.generate(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MainCommand.subCommands.add(new PlotCommandWeb());
    }

    @ViewMatcher(filter = "(\\/assets\\/)(logo)(.png)?", name="plotlogo", cache = true)
    public Response getLogo(final Request in) {
        if (this.logo == null) {
            Response response = new Response(null);
            response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML);
            response.setContent("<h1>The logo file wasn't found >_<");
            return response;
        }
        byte[] bytes = new byte[0];
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(logo), (int)(logo.length() / 512));
            bytes = IOUtils.toByteArray(stream);
            stream.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        Response response = new Response(null);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, "image/png; charset=utf-8");
        response.setBytes(bytes);
        return response;
    }

    @ViewMatcher(filter = "(\\/assets\\/)(script)(.js)?", name = "plotscript", cache = true)
    public Response getScript(final Request in) {
        Response response = new Response(null);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_JAVASCRIPT);
        response.setContent(FileUtils.getDocument(script, 1024 * 1024 * 16));
        return response;
    }

    @ViewMatcher(filter = "(\\/assets\\/)(stylesheet)(.less|.css)?", name = "plotstylesheet", cache = true)
    public Response getStylesheet(final Request in) {
        Response response = new Response(null);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_CSS);
        if (LessView.compiler == null) {
            LessView.compiler = new LessCompiler();
        }
        try {
            response.setContent(LessView.compiler.compile(FileUtils.getDocument(styleSheet, 1024 * 1024 * 16)));
        } catch(final Exception e) {
            response.setContent("ERROR: " + e.getMessage());
        }
        return response;
    }

    @Override
    public String getLogIdentifier() {
        return "PSHook";
    }

    private void log(final String s, final Object ... o) {
        Server.getInstance().log(this, s, o);
    }

}

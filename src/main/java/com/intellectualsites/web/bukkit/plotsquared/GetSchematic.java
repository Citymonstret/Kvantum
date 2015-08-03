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

package com.intellectualsites.web.bukkit.plotsquared;

import com.intellectualcrafters.plot.config.Settings;
import com.intellectualsites.web.object.Header;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.views.View;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created 2015-07-13 for IntellectualServer
 *
 * @author Citymonstret
 */
public class GetSchematic extends View {

    public GetSchematic() {
        super("(\\/schematic\\/)([A-Z0-9a-z;,-]*)(.schematic)?", "schematic");
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String fileName = matcher.group(2) + ".schematic";
        File file = new File(Settings.SCHEMATIC_SAVE_PATH, fileName);
        request.addMeta("ps_schematic", file);
        return file.exists();
    }

    private byte[] generateZipContent(File file) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        BufferedInputStream bif = new BufferedInputStream(new FileInputStream(file), getBuffer());
        ZipEntry entry = new ZipEntry(file.getName());
        zipOutputStream.putNextEntry(entry);
        IOUtils.copy(bif, zipOutputStream);
        bif.close();
        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public Response generate(final Request r) {
        Response response = new Response(this);
        try {
            byte[] bytes = generateZipContent((File) r.getMeta("ps_schematic"));
            response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_OCTET_STREAM);
            response.getHeader().set(Header.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"schematic.zip\"");
            response.getHeader().set(Header.HEADER_CONTENT_TRANSFER_ENCODING, "binary");
            response.getHeader().set(Header.HEADER_CONTENT_LENGTH, "" + bytes.length);
            response.getHeader().set(Header.HEADER_REFRESH, "15; url=/");
            response.setBytes(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML);
            response.setContent("<h1>ERROR DOWNLOADING FILE</h1>");
        }
        return response;
    }
}

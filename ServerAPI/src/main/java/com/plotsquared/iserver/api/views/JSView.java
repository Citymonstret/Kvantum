/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.views;

import com.plotsquared.iserver.api.cache.CacheApplicable;
import com.plotsquared.iserver.api.request.Request;
import com.plotsquared.iserver.api.util.FileExtension;

import java.util.Collections;
import java.util.Map;

/**
 * Created 2015-04-22 for IntellectualServer
 *
 * @author Citymonstret
 */
public class JSView extends StaticFileView implements CacheApplicable
{

    public JSView(String filter, Map<String, Object> options)
    {
        super( filter, options, "javascript", Collections.singletonList( FileExtension.JAVASCRIPT ) );
        super.relatedFolderPath = "/assets/js";
        super.fileName = "{file}.js";
    }

    @Override
    public boolean isApplicable(Request r)
    {
        return true;
    }
}

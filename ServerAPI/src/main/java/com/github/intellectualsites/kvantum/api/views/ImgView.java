/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.views;

import com.github.intellectualsites.kvantum.api.cache.CacheApplicable;
import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.util.FileExtension;
import com.github.intellectualsites.kvantum.api.util.IgnoreSyntax;

import java.util.Map;

/**
 * Created 2015-04-21 for Kvantum
 *
 * @author Citymonstret
 */
public class ImgView extends StaticFileView implements CacheApplicable, IgnoreSyntax
{

    public ImgView(String filter, Map<String, Object> options)
    {
        super( filter, options, "img", FileExtension.IMAGE );
        super.relatedFolderPath = "/assets/img";
    }

    @Override
    public boolean isApplicable(Request r)
    {
        return false;
    }
}

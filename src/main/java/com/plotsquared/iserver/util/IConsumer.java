/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
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
package com.plotsquared.iserver.util;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IConsumer<T> extends Consumer<T>
{

    default void foreach(final Predicate<T> predicate, final Collection<T> collection)
    {
        Assert.notNull( predicate, collection );

        collection.stream().filter( predicate ).forEach( this );
    }

    default void foreach(final Collection<T> collection)
    {
        Assert.notNull( collection );

        collection.forEach( this );
    }

}

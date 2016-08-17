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
package com.plotsquared.iserver.events;

/**
 * This is how we decide what event listeners
 * that gets to act in what order. This is useful
 * as it can be used to create a hierarchy for
 * internal listeners and alike
 *
 * @author Citymonstret
 */
public enum EventPriority
{

    /**
     * Low Priority
     */
    LOW,

    /**
     * Medium Priority
     */
    MEDIUM,

    /**
     * High Priority
     */
    HIGH
}

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

package com.plotsquared.iserver.crush.syntax;

/**
 * The variable provider class - Can get quite confusing.
 * <p>
 * This generates a variable based on a key, and is accessed like
 * * <pre>
 * {@code
 * {{PROVIDER.NAME}}
 * }
 * </pre>
 * whereas the name would be the variable key
 *
 * @author Citymonstret
 */
public interface VariableProvider
{

    /**
     * Does the provider contain this variable?
     *
     * @param variable Variable Key
     * @return True if the variable exists
     */
    boolean contains(final String variable);

    /**
     * Get the variable
     *
     * @param variable Variable Key
     * @return The object (or null)
     * @see #contains(String) - Use this to check if it exists
     */
    Object get(final String variable);

}

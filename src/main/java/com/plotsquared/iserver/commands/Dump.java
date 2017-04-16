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
package com.plotsquared.iserver.commands;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.CommandInstance;

/**
 * Created 12/24/2015 for IntellectualServer
 *
 * @author Citymonstret
 */
@CommandDeclaration(
        command = "dump",
        description = "Dump information from the router"
)
public class Dump extends Command
{

    @Override
    public boolean onCommand(CommandInstance instance)
    {
        com.plotsquared.iserver.core.ServerImplementation.getImplementation().getRouter().dump( com.plotsquared.iserver.core.ServerImplementation.getImplementation() );
        return true;
    }

}

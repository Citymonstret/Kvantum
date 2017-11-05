/*
 * IntellectualServer is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.iserver.implementation.commands;

import com.github.intellectualsites.iserver.api.config.Message;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandInstance;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.pagination.PaginatedCommand;
import com.intellectualsites.commands.pagination.PaginationFactory;
import com.intellectualsites.commands.parser.impl.IntegerParser;

public class Help extends PaginatedCommand<Command>
{

    public Help(final CommandManager parent)
    {
        super( Command.class, parent::getCommands, 3, "help", "/help [page]",
                "Show a list of commands", "", new String[]{ "h" }, Object.class );
        withArgument( "page", new IntegerParser(), "The page" );
    }

    @Override
    public boolean handleTooBigPage(CommandInstance commandInstance, int i, int i1)
    {
        commandInstance.getCaller().message( "The entered page number is too large (" + ( i + 1 ) + ">" + i1 + ")" );
        return true;
    }

    @Override
    public boolean onCommand(PaginatedCommandInstance<Command> paginatedCommandInstance)
    {
        final PaginationFactory.Page<Command> commandPage = paginatedCommandInstance.getPage();
        Message.CMD_HELP_HEADER.log( commandPage.getPageNum() + 1, this.getPaginationFactory().getPages().size() );
        for ( final Command command : commandPage.getItems() )
        {
            Message.CMD_HELP_ITEM.log( command.getCommand(), command.getUsage(), command.getDescription() );
        }
        if ( commandPage.getPageNum() + 1 < this.getPaginationFactory().getPages().size() )
        {
            Message.CMD_HELP_FOOTER.log( commandPage.getPageNum() + 2 );
        }
        return true;
    }
}

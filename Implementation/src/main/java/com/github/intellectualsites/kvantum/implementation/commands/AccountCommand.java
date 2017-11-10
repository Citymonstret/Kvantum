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
package com.github.intellectualsites.kvantum.implementation.commands;

import com.github.intellectualsites.kvantum.api.account.IAccount;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.util.ApplicationStructure;
import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.StringUtil;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.CommandInstance;
import com.intellectualsites.commands.parser.impl.StringParser;

import java.util.Optional;

@CommandDeclaration(
        command = "account",
        usage = "/account [subcommand]",
        description = "Manage accounts"
)
public class AccountCommand extends Command
{

    private final ApplicationStructure structure;

    public AccountCommand(final ApplicationStructure applicationStructure)
    {
        Assert.notNull( applicationStructure );

        this.createCommand( new DumpData() );
        this.createCommand( new TestPass() );
        this.createCommand( new CreateAccount() );
        this.createCommand( new SetData() );
        this.createCommand( new DeleteData() );

        this.structure = applicationStructure;
    }

    public boolean onCommand(CommandInstance instance )
    {
        if ( instance.getArguments().length < 1 )
        {
            send( "Available Subcommands: create, testpass, dumpdata, setdata, deletedata" );
        } else
        {
            super.handle( instance.getCaller(), instance.getArguments() );
        }
        return true;
    }

    @CommandDeclaration(
            command = "deletedata"
    )
    public class DeleteData extends Command
    {

        DeleteData()
        {
            this.withArgument( "username", new StringParser(), "Username!" );
            this.withArgument( "key", new StringParser(), "Data key!" );
        }

        @Override
        public boolean onCommand(final CommandInstance instance)
        {
            String key = instance.getString( "key" );
            String username = instance.getValue( "username", String.class );
            Optional<IAccount> account = structure.getAccountManager().getAccount( username );
            if ( !account.isPresent() )
            {
                send( "There is no such account!" );
            } else
            {
                account.get().removeData( key );
                send( "Data for account " + account.get().getId() + ": " + StringUtil.join( account.get()
                        .getRawData(), ": ", ", " ) );
            }
            return true;
        }
    }

    @CommandDeclaration(
            command = "setdata"
    )
    public class SetData extends Command
    {

        SetData()
        {
            this.withArgument( "username", new StringParser(), "Username!" );
            this.withArgument( "key", new StringParser(), "Data key!" );
            this.withArgument( "value", new StringParser(), "Data value!" );
        }

        @Override
        public boolean onCommand(final CommandInstance instance)
        {
            String key = instance.getString( "key" );
            String value = instance.getString( "value" );
            String username = instance.getValue( "username", String.class );
            Optional<IAccount> account = structure.getAccountManager().getAccount( username );
            if ( !account.isPresent() )
            {
                send( "There is no such account!" );
            } else
            {
                account.get().setData( key, value );
                send( "Data for account " + account.get().getId() + ": " + StringUtil.join( account.get()
                        .getRawData(), ": ", ", " ) );
            }
            return true;
        }
    }

    @CommandDeclaration(
            command = "dumpdata"
    )
    public class DumpData extends Command
    {

        DumpData()
        {
            this.withArgument( "username", new StringParser(), "Username!" );
        }

        @Override
        public boolean onCommand(final CommandInstance instance)
        {
            if ( instance.getArguments().length < 1 )
            {
                send( "Syntax: account dumpdata [username]" );
            } else
            {
                String username = instance.getValue( "username", String.class );
                Optional<IAccount> account = structure.getAccountManager().getAccount( username );
                if ( !account.isPresent() )
                {
                    send( "There is no such account!" );
                } else
                {
                    send( "Data for account " + account.get().getId() + ": " + StringUtil.join( account.get()
                            .getRawData(), ": ", ", " ) );
                }
            }
            return true;
        }
    }

    @CommandDeclaration(
            command = "create"
    )
    public class CreateAccount extends Command
    {

        CreateAccount()
        {
            this.withArgument( "username", new StringParser(), "Username!" );
            this.withArgument( "password", new StringParser(), "Password!" );
        }

        @Override
        public boolean onCommand(final CommandInstance instance)
        {
            String username = instance.getValue( "username", String.class );
            String password = instance.getValue( "password", String.class );
            if ( structure.getAccountManager().getAccount( username ).isPresent() )
            {
                send( "There is already an account with that username!" );
            } else
            {
                try
                {
                    send( "Account created (Username: " + username + ")" );
                    structure.getAccountManager().createAccount( username, password );
                } catch ( final Exception e )
                {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }

    @CommandDeclaration(
            command = "testpass"
    )
    public class TestPass extends Command
    {

        TestPass()
        {
            this.withArgument( "username", new StringParser(), "Username!" );
            this.withArgument( "password", new StringParser(), "Password!" );
        }

        @Override
        public boolean onCommand(final CommandInstance instance)
        {
            String username = instance.getValue( "username", String.class );
            String password = instance.getValue( "password", String.class );

            Optional<IAccount> accountOptional = structure.getAccountManager().getAccount( username );
            if ( !accountOptional.isPresent() )
            {
                send( "There is no such account!" );
            } else
            {
                send( "Matches: " + accountOptional.get().passwordMatches( password ) );
            }
            return true;
        }
    }

    private void send(final String s)
    {
        ServerImplementation.getImplementation().log( s );
    }
}

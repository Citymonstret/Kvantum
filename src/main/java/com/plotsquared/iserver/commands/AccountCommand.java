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
package com.plotsquared.iserver.commands;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.CommandInstance;
import com.plotsquared.iserver.api.account.Account;
import com.plotsquared.iserver.api.core.ServerImplementation;
import com.plotsquared.iserver.api.util.ApplicationStructure;
import com.plotsquared.iserver.api.util.Assert;
import com.plotsquared.iserver.api.util.StringUtil;

import java.util.Optional;

/**
 * TODO: Remake this!
 */
@CommandDeclaration(
        command = "account"
)
public class AccountCommand extends Command
{

    private final ApplicationStructure structure;

    public AccountCommand(final ApplicationStructure applicationStructure)
    {
        Assert.notNull( applicationStructure );

        this.structure = applicationStructure;
    }


    public boolean onCommand(CommandInstance instance )
    {
        if ( instance.getArguments().length < 1 )
        {
            send( "Available Subcommands: create, testpass, dumpdata" );
        } else
        {
            switch ( instance.getArguments()[ 0 ].toLowerCase() )
            {
                case "dumpdata":
                {
                    if ( instance.getArguments().length < 2 )
                    {
                        send( "Syntax: account dumpdata [username]" );
                    } else
                    {
                        String username = instance.getArguments()[ 1 ];
                        Optional<Account> account = structure.getAccountManager().getAccount( username );
                        if ( !account.isPresent() )
                        {
                            send( "There is no such account!" );
                        } else
                        {
                            send( "Data for account " + account.get().getId() + ": " + StringUtil.join( account.get()
                                    .getRawData(), ": ", ", " ) );
                        }
                    }
                }
                break;
                case "testpass":
                {
                    if ( instance.getArguments().length < 3 )
                    {
                        send( "Syntax: account testpass [username] [password]" );
                    } else
                    {
                        String username = instance.getArguments()[ 1 ];
                        String password = instance.getArguments()[ 2 ];

                        Optional<Account> accountOptional = structure.getAccountManager().getAccount( username );
                        if ( !accountOptional.isPresent() )
                        {
                            send( "There is no such account!" );
                        } else
                        {
                            send( "Matches: " + accountOptional.get().passwordMatches( password ) );
                        }
                    }
                }
                break;
                case "create":
                {
                    if ( instance.getArguments().length < 3 )
                    {
                        send( "Syntax: account create [username] [password]" );
                    } else
                    {
                        String username = instance.getArguments()[ 1 ];
                        String password = instance.getArguments()[ 2 ];

                        if ( structure.getAccountManager().getAccount( username ) != null )
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
                    }
                }
                break;
                default:
                    send( "Unknown subcommand: " + instance.getArguments()[ 0 ].toLowerCase() );
                    break;
            }
        }
        return true;
    }

    private void send(final String s)
    {
        ServerImplementation.getImplementation().log( s );
    }
}

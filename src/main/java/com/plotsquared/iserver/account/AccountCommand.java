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
package com.plotsquared.iserver.account;

import com.plotsquared.iserver.commands.Command;
import com.plotsquared.iserver.core.ServerImplementation;
import com.plotsquared.iserver.extra.ApplicationStructure;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.StringUtil;

import java.util.Optional;

public class AccountCommand extends Command
{

    private final ApplicationStructure structure;

    public AccountCommand(final ApplicationStructure applicationStructure)
    {
        Assert.notNull( applicationStructure );

        this.structure = applicationStructure;
    }

    @Override
    public void handle(String[] args)
    {
        if ( args.length < 1 )
        {
            send( "Available Subcommands: create, testpass, dumpdata" );
        } else
        {
            switch ( args[ 0 ].toLowerCase() )
            {
                case "dumpdata":
                {
                    if ( args.length < 2 )
                    {
                        send( "Syntax: account dumpdata [username]" );
                    } else
                    {
                        String username = args[ 1 ];
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
                    if ( args.length < 3 )
                    {
                        send( "Syntax: account testpass [username] [password]" );
                    } else
                    {
                        String username = args[ 1 ];
                        String password = args[ 2 ];

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
                    if ( args.length < 3 )
                    {
                        send( "Syntax: account create [username] [password]" );
                    } else
                    {
                        String username = args[ 1 ];
                        String password = args[ 2 ];

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
                    send( "Unknown subcommand: " + args[ 0 ].toLowerCase() );
                    break;
            }
        }
    }

    private void send(final String s)
    {
        ServerImplementation.getImplementation().log( s );
    }
}

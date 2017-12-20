/*
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.implementation.commands;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.CommandInstance;
import com.intellectualsites.commands.parser.impl.StringParser;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.verification.AccountVerifier;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.util.ApplicationStructure;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.MapUtil;
import xyz.kvantum.server.api.verification.Rule;
import xyz.kvantum.server.implementation.Account;

import java.util.Collection;
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
                send( "Data for account " + account.get().getId() + ": " + MapUtil.join( account.get()
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
                send( "Data for account " + account.get().getId() + ": " + MapUtil.join( account.get()
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
                    send( "Data for account " + account.get().getId() + ": " + MapUtil.join( account.get()
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
                final IAccount temporaryAccount = new Account( -1, username, password );
                final AccountVerifier accountVerifier = AccountVerifier.getGlobalAccountVerifier();
                final Collection<Rule<IAccount>> brokenRules = accountVerifier.verifyAccount( temporaryAccount );
                if ( !brokenRules.isEmpty() )
                {
                    send( "Error when creating account: " );
                    brokenRules.forEach( rule -> send( "- " + rule.getRuleDescription() ) );
                    return true;
                }
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

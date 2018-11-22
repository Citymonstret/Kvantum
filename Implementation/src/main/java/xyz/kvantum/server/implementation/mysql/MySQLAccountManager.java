/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
package xyz.kvantum.server.implementation.mysql;

import com.google.common.collect.ImmutableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.implementation.Account;
import xyz.kvantum.server.implementation.MySQLApplicationStructure;

@RequiredArgsConstructor public class MySQLAccountManager implements IAccountManager
{

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType") private static final Optional<IAccount> EMPTY_OPTIONAL = Optional
			.empty();
	@Getter private final MySQLApplicationStructure applicationStructure;

	private static String getNewSalt()
	{
		return BCrypt.gensalt();
	}

	private static String hashPassword(@NonNull final String password, @NonNull final String salt)
	{
		return BCrypt.hashpw( password, salt );
	}

	@Override public void setup() throws Exception
	{
		this.applicationStructure.getDatabaseManager().executeUpdate(
				"CREATE TABLE IF NOT EXISTS `account` (" + "  `id` int(11) NOT NULL AUTO_INCREMENT,"
						+ "  `username` varchar(64) DEFAULT NULL," + "  `password` varchar(255) DEFAULT NULL,"
						+ "  PRIMARY KEY (`id`)," + "  UNIQUE KEY `accounts_UN` (`username`) )" );
		this.applicationStructure.getDatabaseManager().executeUpdate(
				"CREATE TABLE IF NOT EXISTS `account_data` (" + " `id` int(11) NOT NULL AUTO_INCREMENT,"
						+ " `account_id` int(11)," + " `key` VARCHAR(255)," + " `value` VARCHAR(255),"
						+ " PRIMARY KEY (`id`)," + " UNIQUE KEY `account_data_UN` (`account_id`, `key`) )" );
		this.checkAdmin();
	}

	@Override public Optional<IAccount> createAccount(@NonNull final String username, @NonNull final String password)
	{
		return this.createAccount( new Account( -1, username, password ) );
	}

	@Override public Optional<IAccount> createAccount(@NonNull final IAccount temporary)
	{
		final String username = temporary.getUsername();
		final String password = temporary.getSuppliedPassword();

		Assert.notEmpty( username );
		Assert.notEmpty( password );

		Optional<IAccount> ret = EMPTY_OPTIONAL;
		if ( getAccount( username ).isPresent() )
		{
			return ret;
		}
		try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
		{
			try ( final PreparedStatement statement = connection
					.prepareStatement( "INSERT INTO account(`username`, " + "`password`) VALUES(?, ?)" ) )
			{
				statement.setString( 1, username );
				statement.setString( 2, hashPassword( password, getNewSalt() ) );
				statement.executeUpdate();
			} catch ( final Exception e )
			{
				e.printStackTrace();
			} finally
			{
				ret = getAccount( username );
			}
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
		return ret;
	}

	@Override public Optional<IAccount> getAccount(@NonNull final String username)
	{
		Assert.notEmpty( username );

		Optional<Integer> accountId = ServerImplementation.getImplementation().getCacheManager()
				.getCachedId( username );
		Optional<IAccount> ret = EMPTY_OPTIONAL;
		if ( accountId.isPresent() )
		{
			ret = ServerImplementation.getImplementation().getCacheManager().getCachedAccount( accountId.get() );
		}
		if ( ret.isPresent() )
		{
			return ret;
		}
		try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
		{
			try ( final PreparedStatement statement = connection
					.prepareStatement( "SELECT * FROM `account` WHERE `username` = ?" ) )
			{
				statement.setString( 1, username );
				try ( final ResultSet resultSet = statement.executeQuery() )
				{
					if ( resultSet.next() )
					{
						ret = Optional.of( getAccount( resultSet ) );
					}
				}
			} catch ( final Exception e )
			{
				e.printStackTrace();
			}
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
		ret.ifPresent(
				account -> ServerImplementation.getImplementation().getCacheManager().setCachedAccount( account ) );
		ret.ifPresent( account -> account.setManager( this ) );
		return ret;
	}

	@Override public Optional<IAccount> getAccount(final int accountId)
	{
		Optional<IAccount> ret = ServerImplementation.getImplementation().getCacheManager()
				.getCachedAccount( accountId );
		if ( ret.isPresent() )
		{
			return ret;
		}
		try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
		{
			try ( final PreparedStatement statement = connection
					.prepareStatement( "SELECT * FROM `account` WHERE `id` = ?" ) )
			{
				statement.setInt( 1, accountId );
				try ( final ResultSet resultSet = statement.executeQuery() )
				{
					if ( resultSet.next() )
					{
						ret = Optional.of( getAccount( resultSet ) );
					}
				}
			} catch ( final Exception e )
			{
				e.printStackTrace();
			}
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
		ret.ifPresent(
				account -> ServerImplementation.getImplementation().getCacheManager().setCachedAccount( account ) );
		ret.ifPresent( account -> account.setManager( this ) );
		return ret;
	}

	private IAccount getAccount(@NonNull final ResultSet resultSet) throws Exception
	{
		final int id = resultSet.getInt( "id" );
		final String username = resultSet.getString( "username" );
		final String password = resultSet.getString( "password" );
		final IAccount account = new Account( id, username, password );
		account.setManager( this );
		return account;
	}

	@Override public void loadData(@NonNull final IAccount account)
	{
		try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
		{
			try ( final PreparedStatement statement = connection
					.prepareStatement( "SELECT * FROM account_data WHERE account_id = ?" ) )
			{
				statement.setInt( 1, account.getId() );
				final ResultSet set = statement.executeQuery();
				while ( set.next() )
				{
					account.internalMetaUpdate( set.getString( "key" ), set.getString( "value" ) );
				}
				set.close();
			} catch ( final Exception e )
			{
				e.printStackTrace();
			}
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}

	@Override public void deleteAccount(@NonNull final IAccount account)
	{
		try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
		{
			try ( final PreparedStatement statement = connection
					.prepareStatement( "DELETE FROM `account` WHERE `id` = ?" ) )
			{
				statement.setInt( 1, account.getId() );
				statement.executeUpdate();
			} catch ( final SQLException e )
			{
				e.printStackTrace();
			}
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
		ServerImplementation.getImplementation().getCacheManager().deleteAccount( account );
	}

	@Override public void removeData(@NonNull final IAccount account, @NonNull final String key)
	{
		try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
		{
			try ( final PreparedStatement statement = connection
					.prepareStatement( "DELETE FROM account_data WHERE account_id = ? AND `key` = ?" ) )
			{
				statement.setInt( 1, account.getId() );
				statement.setString( 2, key );
				statement.executeUpdate();
			} catch ( final Exception e )
			{
				e.printStackTrace();
			}
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}

	@Override public void setData(@NonNull final IAccount account, @NonNull final String key,
			@NonNull final String value)
	{
		try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
		{
			try ( final PreparedStatement statement = connection
					.prepareStatement( "INSERT IGNORE INTO account_data(account_id, `key`, `value`) VALUES(?, ?, ?)" ) )
			{
				statement.setInt( 1, account.getId() );
				statement.setString( 2, key );
				statement.setString( 3, value );
				statement.executeUpdate();
			} catch ( final Exception e )
			{
				e.printStackTrace();
			}
			try ( final PreparedStatement statement = connection
					.prepareStatement( "UPDATE account_data SET `value` = ? " + "WHERE account_id = ? AND `key` = ?" ) )
			{
				statement.setInt( 2, account.getId() );
				statement.setString( 3, key );
				statement.setString( 1, value );
				statement.executeUpdate();
			} catch ( final Exception e )
			{
				e.printStackTrace();
			}
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}

	@Override public ImmutableList<? extends IAccount> findAll()
	{
		final ImmutableList.Builder<IAccount> builder = ImmutableList.builder();
		try ( final Connection connection = applicationStructure.getDatabaseManager().getConnection() )
		{
			try ( final PreparedStatement statement = connection.prepareStatement( "SELECT * FROM `account`" ) )
			{
				try ( final ResultSet resultSet = statement.executeQuery() )
				{
					while ( resultSet.next() )
					{
						builder.add( getAccount( resultSet ) );
					}
				}
			} catch ( final Exception e )
			{
				e.printStackTrace();
			}
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
		return builder.build();
	}
}

package com.github.intellectualsites.iserver.implementation.mongo;

import com.github.intellectualsites.iserver.api.account.IAccount;
import com.github.intellectualsites.iserver.api.account.IAccountManager;
import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.implementation.Account;
import com.github.intellectualsites.iserver.implementation.MongoApplicationStructure;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

@RequiredArgsConstructor
final public class MongoAccountManager implements IAccountManager
{

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<IAccount> EMPTY_OPTIONAL = Optional.empty();
    @Getter
    private final MongoApplicationStructure applicationStructure;

    private DBCollection counters;

    private static String getNewSalt()
    {
        return BCrypt.gensalt();
    }

    private static String hashPassword(final String password, final String salt)
    {
        return BCrypt.hashpw( password, salt );
    }

    @Override
    public void setup() throws Exception
    {
        DB database = applicationStructure.getMongoClient().getDB( CoreConfig.MongoDB.dbMorphia );
        this.counters = database.getCollection( "counters" );
        if ( !this.counters.find( new BasicDBObject( "_id", "userId" ) ).hasNext() )
        {
            this.counters.insert( new BasicDBObject( "_id", "userId" ).append( "seq", 0 ) );
        }

        this.checkAdmin();
    }

    private int getNextId()
    {
        return (int) counters.findAndModify( new BasicDBObject( "_id", "userId" ), new BasicDBObject( "$inc",
                new BasicDBObject( "seq", 1 ) ) ).get( "seq" );
    }

    @Override
    public Optional<IAccount> createAccount(final String username, final String password)
    {
        Assert.notEmpty( username );
        Assert.notEmpty( password );

        if ( getAccount( username ).isPresent() )
        {
            return EMPTY_OPTIONAL;
        }

        final String hashedPassword = hashPassword( password, getNewSalt() );
        final Account account = new Account( getNextId(), username, hashedPassword );
        account.setManager( this );
        this.applicationStructure.getMorphiaDatastore().save( account );

        return Optional.of( account );
    }

    @Override
    public Optional<IAccount> getAccount(final String username)
    {
        Assert.notEmpty( username );

        Optional<Integer> accountId = ServerImplementation.getImplementation().getCacheManager().getCachedId( username );
        Optional<IAccount> ret = EMPTY_OPTIONAL;
        if ( accountId.isPresent() )
        {
            ret = ServerImplementation.getImplementation().getCacheManager().getCachedAccount( accountId.get() );
        }
        if ( ret.isPresent() )
        {
            return ret;
        }
        ret = Optional.ofNullable( applicationStructure.getMorphiaDatastore().createQuery( Account.class )
                .field( "username" ).equal( username ).get() );
        ret.ifPresent( account -> ServerImplementation.getImplementation().getCacheManager().setCachedAccount( account ) );
        ret.ifPresent( account -> account.setManager( this ) );
        return ret;
    }

    @Override
    public Optional<IAccount> getAccount(final int accountId)
    {
        Optional<IAccount> ret = ServerImplementation.getImplementation().getCacheManager().getCachedAccount(
                accountId );
        if ( ret.isPresent() )
        {
            return ret;
        }

        ret = Optional.ofNullable( applicationStructure.getMorphiaDatastore().createQuery( Account.class )
                .field( "userId" ).equal( accountId ).get() );
        ret.ifPresent( account -> ServerImplementation.getImplementation().getCacheManager().setCachedAccount( account ) );
        ret.ifPresent( account -> account.setManager( this ) );
        return ret;
    }

    @Override
    public void setData(final IAccount account, final String key, final String value)
    {
        applicationStructure.getMorphiaDatastore().update(
                applicationStructure.getMorphiaDatastore().createQuery( Account.class )
                        .field( "username" ).equal( account.getUsername() ),
                applicationStructure.getMorphiaDatastore().createUpdateOperations( Account.class )
                        .set( "data." + key, value )
        );
    }

    @Override
    public void removeData(final IAccount account, final String key)
    {
        applicationStructure.getMorphiaDatastore().update(
                applicationStructure.getMorphiaDatastore().createQuery( Account.class )
                        .field( "username" ).equal( account.getUsername() ),
                applicationStructure.getMorphiaDatastore().createUpdateOperations( Account.class )
                        .removeFirst( "data." + key )
        );
    }

    @Override
    public void loadData(final IAccount account)
    {
        // Done automatically
    }
}

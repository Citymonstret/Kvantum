package com.github.intellectualsites.iserver.implementation.mongo;

import com.github.intellectualsites.iserver.api.account.Account;
import com.github.intellectualsites.iserver.api.account.IAccountManager;
import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.api.util.MongoApplicationStructure;
import com.mongodb.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

@RequiredArgsConstructor
public class MongoAccountManager implements IAccountManager
{

    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_DATA = "data";

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Account> EMPTY_OPTIONAL = Optional.empty();
    @Getter
    private final MongoApplicationStructure applicationStructure;
    private DBCollection collection;
    private DBCollection counters;

    private static String getNewSalt()
    {
        return BCrypt.gensalt();
    }

    private static String hashPassword(final String password, final String salt)
    {
        return BCrypt.hashpw( password, salt );
    }

    private Account dboToAccount(final DBObject object)
    {
        final int userID = (int) object.get( FIELD_USER_ID );
        final String username = object.get( FIELD_USERNAME ).toString();
        final String password = object.get( FIELD_PASSWORD ).toString();

        return new Account( userID, username, password, this );
    }

    @Override
    public void setup() throws Exception
    {
        DB database = applicationStructure.getMongoClient().getDB( CoreConfig.MongoDB.dbAccounts );
        this.collection = database.getCollection( CoreConfig.MongoDB.collectionAccounts );
        this.counters = database.getCollection( "counters" );

        if ( !this.counters.find( new BasicDBObject( "_id", "userId" ) ).hasNext() )
        {
            this.counters.insert( new BasicDBObject( "_id", "userId" ).append( "seq", 0 ) );
        }
    }

    private int getNextId()
    {
        return (int) counters.findAndModify( new BasicDBObject( "_id", "userId" ), new BasicDBObject( "$inc",
                new BasicDBObject( "seq", 1 ) ) ).get( "seq" );
    }

    @Override
    public Optional<Account> createAccount(final String username, final String password)
    {
        Assert.notEmpty( username );
        Assert.notEmpty( password );

        if ( getAccount( username ).isPresent() )
        {
            return EMPTY_OPTIONAL;
        }

        final String salt = getNewSalt();
        final DBObject account = new BasicDBObject()
                .append( FIELD_USER_ID, getNextId() )
                .append( FIELD_USERNAME, username )
                .append( FIELD_PASSWORD, hashPassword( password, salt ) )
                .append( FIELD_DATA, new BasicDBObject( "created", true ) );
        collection.insert( account );

        return getAccount( username );
    }

    @Override
    public Optional<Account> getAccount(final String username)
    {
        Assert.notEmpty( username );

        Optional<Integer> accountId = ServerImplementation.getImplementation().getCacheManager().getCachedId( username );
        Optional<Account> ret = EMPTY_OPTIONAL;
        if ( accountId.isPresent() )
        {
            ret = ServerImplementation.getImplementation().getCacheManager().getCachedAccount( accountId.get() );
        }
        if ( ret.isPresent() )
        {
            return ret;
        }

        final DBObject query = new BasicDBObject( FIELD_USERNAME, username );
        final DBCursor cursor = collection.find( query );

        if ( cursor.hasNext() )
        {
            final Account account = dboToAccount( cursor.one() );
            ret = Optional.of( account );
        }

        ret.ifPresent( account -> ServerImplementation.getImplementation().getCacheManager().setCachedAccount( account ) );
        return ret;
    }

    @Override
    public Optional<Account> getAccount(final int accountId)
    {
        Optional<Account> ret = ServerImplementation.getImplementation().getCacheManager().getCachedAccount( accountId );
        if ( ret.isPresent() )
        {
            return ret;
        }

        final DBObject query = new BasicDBObject( FIELD_USER_ID, accountId );
        final DBCursor cursor = collection.find( query );

        if ( cursor.hasNext() )
        {
            final Account account = dboToAccount( cursor.one() );
            ret = Optional.of( account );
        }

        ret.ifPresent( account -> ServerImplementation.getImplementation().getCacheManager().setCachedAccount( account ) );
        return ret;
    }

    @Override
    public void setData(final Account account, final String key, final String value)
    {
        collection.update( new BasicDBObject( FIELD_USER_ID, account.getId() ),
                new BasicDBObject( "$set", new BasicDBObject( FIELD_DATA + "." + key, value ) ) );
    }

    @Override
    public void removeData(final Account account, final String key)
    {
        collection.update( new BasicDBObject( FIELD_USER_ID, account.getId() ),
                new BasicDBObject( "$unset", new BasicDBObject( FIELD_DATA + "." + key, 1 ) ) );
    }

    @Override
    public void loadData(final Account account)
    {
        final DBCursor cursor = collection.find( new BasicDBObject(
                        new BasicDBObject( FIELD_USER_ID, account.getId() ) ),
                new BasicDBObject( "data", 1 ) );
        if ( cursor.hasNext() )
        {
            final DBObject container = cursor.next();
            final DBObject data = (BasicDBObject) container.get( "data" );
            for ( final String key : data.keySet() )
            {
                account.internalMetaUpdate( key, data.get( key ).toString() );
            }
        }
    }
}

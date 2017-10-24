package com.github.intellectualsites.iserver.implementation.mongo;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.session.ISessionDatabase;
import com.github.intellectualsites.iserver.api.util.MongoApplicationStructure;
import com.mongodb.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MongoSessionDatabase implements ISessionDatabase
{

    private static final String FIELD_SESSION_ID = "sessionId";
    private static final String FIELD_LAST_ACTIVE = "lastActive";

    @Getter
    private final MongoApplicationStructure applicationStructure;
    private DBCollection collection;

    @Override
    public void setup() throws Exception
    {
        DB database = applicationStructure.getMongoClient().getDB( CoreConfig.MongoDB.dbSessions );
        this.collection = database.getCollection( CoreConfig.MongoDB.collectionSessions );
    }

    public long containsSession(final String sessionID)
    {
        long ret = -1;

        final DBObject object = new BasicDBObject( FIELD_SESSION_ID, sessionID );
        final DBCursor cursor = collection.find( object );

        if ( cursor.hasNext() )
        {
            ret = (long) cursor.next().get( FIELD_LAST_ACTIVE );
        }

        return ret;
    }

    @Override
    public void storeSession(final String session)
    {
        if ( containsSession( session ) != -1 )
        {
            updateSession( session );
        } else
        {
            final DBObject object = new BasicDBObject()
                    .append( FIELD_SESSION_ID, session )
                    .append( FIELD_LAST_ACTIVE, System.currentTimeMillis() );
            collection.insert( object );
        }
    }

    @Override
    public void updateSession(final String session)
    {
        collection.update( new BasicDBObject( FIELD_SESSION_ID, session ), new BasicDBObject( "$set",
                new BasicDBObject( FIELD_LAST_ACTIVE, System.currentTimeMillis() ) ) );
    }

    @Override
    public void deleteSession(final String session)
    {
        collection.remove( new BasicDBObject( FIELD_SESSION_ID, session ) );
    }
}

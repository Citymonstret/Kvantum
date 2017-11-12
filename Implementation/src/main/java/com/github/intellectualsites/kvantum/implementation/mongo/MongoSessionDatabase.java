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
package com.github.intellectualsites.kvantum.implementation.mongo;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.session.ISession;
import com.github.intellectualsites.kvantum.api.session.ISessionDatabase;
import com.github.intellectualsites.kvantum.api.session.SessionLoad;
import com.github.intellectualsites.kvantum.implementation.MongoApplicationStructure;
import com.mongodb.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final public class MongoSessionDatabase implements ISessionDatabase
{

    private static final String FIELD_SESSION_ID = "sessionId";
    private static final String FIELD_LAST_ACTIVE = "lastActive";
    private static final String FIELD_SESSION_KEY = "sessionKey";

    @Getter
    private final MongoApplicationStructure applicationStructure;
    private DBCollection collection;

    @Override
    public void setup() throws Exception
    {
        DB database = applicationStructure.getMongoClient().getDB( CoreConfig.MongoDB.dbSessions );
        this.collection = database.getCollection( CoreConfig.MongoDB.collectionSessions );
    }

    @Override
    public SessionLoad getSessionLoad(final String sessionID)
    {
        final DBObject object = new BasicDBObject( FIELD_SESSION_ID, sessionID );
        final DBCursor cursor = collection.find( object );

        if ( cursor.hasNext() )
        {
            final DBObject session = cursor.next();

            return new SessionLoad( (int) session.get( FIELD_SESSION_ID ),
                    session.get( FIELD_SESSION_KEY ).toString(), (long) session.get( FIELD_LAST_ACTIVE ) );
        }

        return null;
    }

    @Override
    public void storeSession(final ISession session)
    {
        if ( getSessionLoad( session.get( "id" ).toString() ) != null )
        {
            updateSession( session.get( "id" ).toString() );
        } else
        {
            final DBObject object = new BasicDBObject()
                    .append( FIELD_SESSION_ID, session.get( "id" ).toString() )
                    .append( FIELD_LAST_ACTIVE, System.currentTimeMillis() )
                    .append( FIELD_SESSION_KEY, session.getSessionKey() );
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

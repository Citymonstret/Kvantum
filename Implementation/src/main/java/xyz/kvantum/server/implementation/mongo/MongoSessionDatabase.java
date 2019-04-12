/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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
package xyz.kvantum.server.implementation.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.session.ISessionDatabase;
import xyz.kvantum.server.api.session.SessionLoad;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.implementation.MongoApplicationStructure;

@RequiredArgsConstructor final public class MongoSessionDatabase implements ISessionDatabase {

    private static final String FIELD_SESSION_ID = "sessionId";
    private static final String FIELD_LAST_ACTIVE = "lastActive";
    private static final String FIELD_SESSION_KEY = "sessionKey";

    @Getter private final MongoApplicationStructure applicationStructure;
    private DBCollection collection;

    @Override public void setup() throws Exception {
        DB database = applicationStructure.getMongoClient().getDB(CoreConfig.MongoDB.dbSessions);
        this.collection = database.getCollection(CoreConfig.MongoDB.collectionSessions);
    }

    @Override public SessionLoad getSessionLoad(final AsciiString sessionID) {
        final DBObject object = new BasicDBObject(FIELD_SESSION_ID, sessionID);
        final DBCursor cursor = collection.find(object);

        if (cursor.hasNext()) {
            final DBObject session = cursor.next();

            return new SessionLoad(session.get(FIELD_SESSION_KEY).toString(),
                (long) session.get(FIELD_LAST_ACTIVE));
        }

        return null; // Nullable
    }

    @Override public void storeSession(final ISession session) {
        if (getSessionLoad((AsciiString) session.get("id")) != null) {
            updateSession((AsciiString) session.get("id"));
        } else {
            final DBObject object =
                new BasicDBObject().append(FIELD_SESSION_ID, session.get("id").toString())
                    .append(FIELD_LAST_ACTIVE, System.currentTimeMillis())
                    .append(FIELD_SESSION_KEY, session.getSessionKey());
            collection.insert(object);
        }
    }

    @Override public void updateSession(final AsciiString session) {
        collection.update(new BasicDBObject(FIELD_SESSION_ID, session), new BasicDBObject("$set",
            new BasicDBObject(FIELD_LAST_ACTIVE, System.currentTimeMillis())));
    }

    @Override public void deleteSession(final AsciiString session) {
        collection.remove(new BasicDBObject(FIELD_SESSION_ID, session));
    }
}

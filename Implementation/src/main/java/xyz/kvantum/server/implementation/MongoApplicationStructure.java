/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.implementation;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.Getter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.LoggerFactory;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.util.ApplicationStructure;

public abstract class MongoApplicationStructure extends ApplicationStructure
{

    @Getter
    private final MongoClient mongoClient;
    @Getter
    private final Morphia morphia;
    @Getter
    private final Datastore morphiaDatastore;

    MongoApplicationStructure(final String applicationName)
    {
        super( applicationName );

        // Turn off the really annoying MongoDB spam :/
        {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger( "org.mongodb.driver" );
            rootLogger.setLevel( Level.OFF );
        }

        this.mongoClient = new MongoClient( new MongoClientURI( CoreConfig.MongoDB.uri ) );
        this.accountManager = createNewAccountManager();
        xyz.kvantum.server.api.logging.Logger.info( "Initialized MongoApplicationStructure: {}", this
                .applicationName );

        this.morphia = new Morphia();
        this.morphia.mapPackage( "com.github.intellectualsites.kvantum.implementation" );
        this.morphiaDatastore = morphia.createDatastore( this.mongoClient, CoreConfig.MongoDB.dbMorphia );
    }

}

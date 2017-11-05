/*
 * IntellectualServer is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.iserver.implementation;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.util.ApplicationStructure;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.Getter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.LoggerFactory;

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
        com.github.intellectualsites.iserver.api.logging.Logger.info( "Initialized MongoApplicationStructure: %s", this
                .applicationName );

        this.morphia = new Morphia();
        this.morphia.mapPackage( "com.github.intellectualsites.iserver.implementation" );
        this.morphiaDatastore = morphia.createDatastore( this.mongoClient, CoreConfig.MongoDB.dbMorphia );
    }

}

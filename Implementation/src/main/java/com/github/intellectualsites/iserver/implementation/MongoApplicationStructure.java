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

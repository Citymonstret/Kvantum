package com.github.intellectualsites.iserver.api.util;

import com.github.intellectualsites.iserver.api.exceptions.IntellectualServerException;
import com.github.intellectualsites.iserver.api.logging.Logger;
import lombok.Getter;

import java.io.IOException;
import java.sql.SQLException;

public abstract class SQLiteApplicationStructure extends ApplicationStructure
{

    @Getter
    private final SQLiteManager databaseManager;

    public SQLiteApplicationStructure(final String applicationName)
    {
        super( applicationName );
        try
        {
            this.databaseManager = new SQLiteManager( this.applicationName );
        } catch ( final IOException | SQLException e )
        {
            throw new IntellectualServerException( e );
        }
        this.accountManager = createNewAccountManager();
        Logger.info( "Initialized SQLiteApplicationStructure: %s", this.applicationName );
    }

}

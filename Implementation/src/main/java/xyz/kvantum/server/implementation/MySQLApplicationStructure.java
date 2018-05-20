package xyz.kvantum.server.implementation;

import lombok.Getter;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.ApplicationStructure;
import xyz.kvantum.server.implementation.mysql.MySQLManager;

public abstract class MySQLApplicationStructure extends ApplicationStructure
{

    @Getter
    private final MySQLManager databaseManager;

    public MySQLApplicationStructure(final String applicationName)
    {
        super( applicationName );
        this.databaseManager = new MySQLManager( this.applicationName );
        this.accountManager = createNewAccountManager();
        Logger.info( "Initialized MySQLApplicationStructure: {}", this.applicationName );
    }
}

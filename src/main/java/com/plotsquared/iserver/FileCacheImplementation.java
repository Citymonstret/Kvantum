package com.plotsquared.iserver;

import com.plotsquared.iserver.files.FileCacheManager;

import java.util.Optional;

public class FileCacheImplementation implements FileCacheManager
{

    @Override
    public Optional<String> readCachedFile(String string)
    {
        return Server.getInstance().getCacheManager().getCachedFile( string );
    }

    @Override
    public void writeCachedFile(String string, String content)
    {
        Server.getInstance().getCacheManager().setCachedFile( string, content );
    }
}

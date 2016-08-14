package com.plotsquared.iserver.util;

import java.util.Collection;

public final class CollectionUtil
{

    public static int clear(final Collection collection)
    {
        final int size = collection.size();
        collection.clear();
        return size;
    }

}

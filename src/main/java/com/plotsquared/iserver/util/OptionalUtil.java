package com.plotsquared.iserver.util;

import java.util.Optional;

public class OptionalUtil
{

    public static <T> T getOrNull(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") final Optional<T> optional)
    {
        if ( optional.isPresent() )
        {
            return optional.get();
        }
        return null;
    }

}

package com.plotsquared.iserver.util;

import java.util.HashMap;
import java.util.Map;

public final class MapUtil
{
    public static <I, O> Map<String, O> convertMap(final Map<String, I> input, final Converter<I, O>
            converter)
    {
        final Map<String, O> output = new HashMap<>();
        input.forEach( ( key, value ) -> output.put( key, converter.convert( value ) ) );
        return output;
    }

    public interface Converter<I, O>
    {

        O convert(I input);

    }

}

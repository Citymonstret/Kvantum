package xyz.kvantum.server.api.util;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class ColorUtil
{

    private static final Map<Character, Integer> coloredMapping = MapBuilder.<Character, Integer>newHashMap()
            .put( '0', 30 ).put( '9', 34 ).put( 'c', 31 ).put( '2', 32 )
            .put( 'e', 78 ).put( '5', 35 ).put( 'f', 37 ).put( 'r', 0 ).get();


    public static String getReplaced(String in)
    {
        for ( final Map.Entry<Character, Integer> entry : coloredMapping.entrySet() )
        {
            in = in.replace( "&" + entry.getKey(), "\u001B[" + entry.getValue() + ";1m" );
        }
        return in;
    }

}

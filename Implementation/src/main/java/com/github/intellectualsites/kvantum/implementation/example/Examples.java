package com.github.intellectualsites.kvantum.implementation.example;

import com.github.intellectualsites.kvantum.api.logging.Logger;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Examples
{

    private static Map<String, Example> exampleMap = new HashMap<>();

    static
    {
        exampleMap.put( "usersearch", new UserSearchExample() );
    }

    public static void loadExample(final String input)
    {
        if ( exampleMap.containsKey( input.toLowerCase() ) )
        {
            exampleMap.get( input.toLowerCase() ).initExample();
        } else
        {
            Logger.error( "Unknown example: \"%s\"", input );
        }
    }

}

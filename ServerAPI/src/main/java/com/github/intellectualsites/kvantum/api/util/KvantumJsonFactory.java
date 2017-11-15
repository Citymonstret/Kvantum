package com.github.intellectualsites.kvantum.api.util;

import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Utilities for common JSON procedures
 */
@UtilityClass
public class KvantumJsonFactory
{

    @Getter
    private static final JSONParser parser = new JSONParser();

    /**
     * Attempt to parse a string into a json object,
     * if it fails for some reason an empty JSON object
     * will be returned instead
     *
     * @param in String input
     * @return JSON object
     */
    public static JSONObject parseJSONObject(final String in)
    {
        try
        {
            final Object object = parser.parse( in );
            if ( object instanceof JSONObject )
            {
                return (JSONObject) object;
            }
        } catch ( final ParseException e )
        {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    /**
     * Get a {@link JsonPrimitive} instance for a given string. The
     * string will be empty ({@code ""}) if the input string is null
     *
     * @param in String
     * @return Parsed primitive
     */
    public static JsonPrimitive stringToPrimitive(final String in)
    {
        if ( in == null )
        {
            return new JsonPrimitive( "" );
        }
        return new JsonPrimitive( in );
    }

}

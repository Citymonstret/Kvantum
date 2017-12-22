/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.api.util;

import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xyz.kvantum.server.api.pojo.KvantumPojo;

import java.util.Collection;
import java.util.Map;

/**
 * Utilities for common JSON procedures
 */
@UtilityClass
public class KvantumJsonFactory
{

    @Getter
    private static final JSONParser PARSER = new JSONParser();
    private static final JSONObject EMPTY_OBJECT = new JSONObject();

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
        if ( null == in || in.isEmpty() )
        {
            return EMPTY_OBJECT;
        }
        try
        {
            final Object object = PARSER.parse( in );
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

    public <T> JSONObject toJSONObject(final Map<String, T> map)
    {
        return new JSONObject( MapUtil.convertMap( map, Object::toString ) );
    }

    public JSONArray toJsonArray(final Collection<?> collection)
    {
        final JSONArray array = new JSONArray();
        for ( final Object o : collection )
        {
            if ( o instanceof KvantumPojo )
            {
                array.add( ( (KvantumPojo) o ).toJson() );
            } else
            {
                array.add( o );
            }
        }
        return array;
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

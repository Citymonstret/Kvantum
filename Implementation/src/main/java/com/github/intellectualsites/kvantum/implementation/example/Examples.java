/*
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

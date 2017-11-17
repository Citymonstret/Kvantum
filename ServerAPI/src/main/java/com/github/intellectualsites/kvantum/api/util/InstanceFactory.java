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
package com.github.intellectualsites.kvantum.api.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utility class for dealing with singleton instances
 */
@SuppressWarnings( "ALL" )
@UtilityClass
public class InstanceFactory
{

    public static <T> void setupInstance(final T t, final String fieldName)
    {
        try
        {
            Field field = t.getClass().getDeclaredField( fieldName );
            if ( !field.isAccessible() )
            {
                field.setAccessible( true );
            }
            field.set( null, t );

            Assert.equals( field.get( null ).equals( t ), true );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    public static <T> void setupInstance(final T t)
    {
        setupInstance( t, "instance" );
    }

    public static <T> void setupInstanceAutomagic(final T t)
    {
        for ( final Field field : t.getClass().getDeclaredFields() )
        {
            if ( Modifier.isStatic( field.getModifiers() ) && field.getType().equals( t.getClass() ) )
            {
                try
                {
                    field.setAccessible( true );
                    if ( field.get( null ) != null )
                    {
                        continue;
                    }
                    field.set( null, t );
                } catch ( IllegalAccessException e )
                {
                    e.printStackTrace();
                }
            }
        }
    }

}

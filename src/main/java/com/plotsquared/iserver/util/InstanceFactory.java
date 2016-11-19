package com.plotsquared.iserver.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class InstanceFactory
{

    public static <T> void setupInstance(T t, String fieldName)
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

    public static <T> void setupInstance(T t)
    {
        setupInstance( t, "instance" );
    }

    public static <T> void setupInstanceAutomagic(T t)
    {
        for ( final Field field : t.getClass().getDeclaredFields() )
        {
            if ( Modifier.isStatic( field.getModifiers() ) )
            {
                if ( field.getType().equals( t.getClass() ) )
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

}

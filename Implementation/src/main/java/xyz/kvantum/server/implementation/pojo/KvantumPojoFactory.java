package xyz.kvantum.server.implementation.pojo;

import com.google.common.collect.ImmutableMap;
import com.hervian.lambda.LambdaFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

/**
 * Class responsible for constructing {@link KvantumPojo} instances
 *
 * @param <Object> POJO class
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("WeakerAccess")
public final class KvantumPojoFactory<Object>
{

    private final Map<String, PojoGetter<Object>> getters;

    /**
     * Construct a new {@link KvantumPojoFactory} for a given POJO class
     *
     * @param pojoClass Class containing Object
     * @param <Object>  Class Type
     * @return Constructed Factory
     */
    public static <Object> KvantumPojoFactory<Object> forClass(final Class<Object> pojoClass)
    {
        final ImmutableMap.Builder<String, PojoGetter<Object>> getterBuilder = ImmutableMap.builder();
        for ( final Method method : pojoClass.getDeclaredMethods() )
        {
            final String prefix;
            if ( method.getName().startsWith( "is" ) )
            {
                prefix = "is";
            } else if ( method.getName().startsWith( "get" ) )
            {
                if ( method.getName().equals( "get" ) )
                {
                    // "get" is often used to get underlying maps
                    continue;
                }
                prefix = "get";
            } else
            {
                continue;
            }

            //
            // Will turn "getName" into "name"
            //
            String name = method.getName().replaceFirst( prefix, "" );
            String firstChar = new String( new char[]{ name.charAt( 0 ) } );
            name = firstChar.toLowerCase( Locale.US ) + name.substring( 1 );

            try
            {
                getterBuilder.put( name, new PojoGetter<>( LambdaFactory.create( method ), method.getReturnType() ) );
            } catch ( final Throwable throwable )
            {
                throwable.printStackTrace();
            }
        }
        return new KvantumPojoFactory<>( getterBuilder.build() );
    }

    /**
     * Get a {@link KvantumPojo} instance for the given POJO instance
     *
     * @param instance POJO Instance
     * @return Instance
     */
    public KvantumPojo of(final Object instance)
    {
        final ImmutableMap.Builder<String, java.lang.Object> fieldValues = ImmutableMap.builder();
        for ( final Map.Entry<String, PojoGetter<Object>> getterEntry : getters.entrySet() )
        {
            fieldValues.put( getterEntry.getKey(), getterEntry.getValue().get( instance ) );
        }
        return new KvantumPojo( fieldValues.build() );
    }

}

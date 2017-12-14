package xyz.kvantum.server.implementation.pojo;

import com.hervian.lambda.Lambda;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class PojoGetter<Pojo>
{

    private final Lambda lambda;
    private final Class<?> returnType;

    public Object get(final Pojo instance)
    {
        if ( returnType.isPrimitive() )
        {
            if ( returnType.equals( int.class ) )
            {
                return lambda.invoke_for_int( instance );
            } else if ( returnType.equals( long.class ) )
            {
                return lambda.invoke_for_long( instance );
            } else if ( returnType.equals( float.class ) )
            {
                return lambda.invoke_for_float( instance );
            } else if ( returnType.equals( boolean.class ) )
            {
                return lambda.invoke_for_boolean( instance );
            } else if ( returnType.equals( double.class ) )
            {
                return lambda.invoke_for_double( instance );
            } else if ( returnType.equals( byte.class ) )
            {
                return lambda.invoke_for_byte( instance );
            } else if ( returnType.equals( char.class ) )
            {
                return lambda.invoke_for_char( instance );
            }
        }
        return lambda.invoke_for_Object( instance );
    }
}

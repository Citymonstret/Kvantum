package xyz.kvantum.server.api.pojo;

import com.hervian.lambda.Lambda;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class PojoSetter<Pojo>
{

    private final Lambda lambda;
    @Getter(AccessLevel.PACKAGE)
    private final Class<?> parameterType;

    public void set(@NonNull final Pojo instance, @NonNull final Object object)
    {
        if ( parameterType.isPrimitive() )
        {
            if ( parameterType.equals( int.class ) )
            {
                lambda.invoke_for_void( instance, (int) object );
            } else if ( parameterType.equals( long.class ) )
            {
                lambda.invoke_for_void( instance, (long) object );
            } else if ( parameterType.equals( float.class ) )
            {
                lambda.invoke_for_void( instance, (float) object );
            } else if ( parameterType.equals( boolean.class ) )
            {
                lambda.invoke_for_void( instance, (boolean) object );
            } else if ( parameterType.equals( double.class ) )
            {
                lambda.invoke_for_void( instance, (double) object );
            } else if ( parameterType.equals( byte.class ) )
            {
                lambda.invoke_for_void( instance, (byte) object );
            } else if ( parameterType.equals( char.class ) )
            {
                lambda.invoke_for_void( instance, (char) object );
            }
        } else
        {
            lambda.invoke_for_void( instance, object );
        }
    }

}

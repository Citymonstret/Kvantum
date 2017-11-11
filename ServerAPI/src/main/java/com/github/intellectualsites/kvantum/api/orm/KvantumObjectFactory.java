package com.github.intellectualsites.kvantum.api.orm;

import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumConstructor;
import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumField;
import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumInsert;
import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumObject;
import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.util.ParameterScope;
import com.github.intellectualsites.kvantum.api.views.rest.RequestRequirements;
import com.intellectualsites.commands.parser.Parser;
import com.intellectualsites.commands.parser.ParserResult;
import com.intellectualsites.commands.parser.impl.BooleanParser;
import com.intellectualsites.commands.parser.impl.IntegerParser;
import com.intellectualsites.commands.parser.impl.StringParser;
import lombok.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

import static com.github.intellectualsites.kvantum.api.util.ParameterScope.GET;

/**
 * Framework for binding objects to GET/POST requests
 * <p>
 * Example:
 * <pre>{@code
 * @code @KvantumObject
 *  public class RequestedUser
 *  {
 *      @code @Getter
 *      @code @KvantumField ( default = "admin" )
 *       private String username:
 *
 *      @code @Getter
 *      @code @KvantumField
 *       private int userId = -1;
 *
 *      @code @KvantumConstructor
 *       public RequestedUser( @KvantumInsert( "userId" ) int userId,
 *          @code @KvantumInsert( "username" ) String username
 *       {
 *          ...
 *       }
 *
 *       // OR
 *
 *      @code @KvantumConstructor
 *       public RequestedUser() {} // Here fields will be set directly!
 *
 *  }
 *
 * // THEN
 *
 * KvantumObjectFactory<RequestedUsser> factory = KvantumObjectFactory.from( RequestedUser.class );
 * // skip parser result checking; DON'T ACTUALLY TO THAT...
 * RequestedUser user = factory.build( ParameterScope.GET ).parseRequest( request ).getParsedObject();
 * }</pre>
 * </p>
 */
@RequiredArgsConstructor
final public class KvantumObjectFactory<T>
{

    private final InternalKvantumConstructor<T> constructor;
    private final Map<String, InternalKvantumField> fields;

    /**
     * Generate a new factory from a given class. The class must have a @KvantumObject annotation,
     * and all fields used in the generation of the object must have @KvantumField annotations. There
     * must be at least one field in the class with a @KvantumField annotation. If the class does not
     * have a @KvantumConstructor annotated constructor, then a default no-args constructor must be available. If the
     * annotated @KvantumConstructor has no parameters, then the fields will be updated directly instead.
     * <p>
     * This method is very
     * expensive, so the generated factory should be reused. Do not use this method in performance-critical
     * operations, instead generate it beforehand and store it somewhere.
     * </p>
     * @param clazz Class from which the factory will be generated
     * @param <T> Type
     * @return Generated factory
     * @throws IllegalArgumentException If the class does not fit the specifications of a Kvantum object
     */
    @SuppressWarnings( "ALL" )
    public static <T> KvantumObjectFactory<T> from(final Class<T> clazz) throws IllegalArgumentException
    {
        if ( clazz.getAnnotation( KvantumObject.class ) == null )
        {
            throw new IllegalArgumentException(
                    String.format( "Class [%s] does not have an @KvantumObject annotation!", clazz.getName() ) );
        }
        final Map<String, InternalKvantumField> clazzFields = new LinkedHashMap<>();
        KvantumField kvantumField;
        for ( final Field field : clazz.getDeclaredFields() )
        {
            if ( ( kvantumField = field.getAnnotation( KvantumField.class ) ) != null )
            {
                //
                // Check if we're actually able to parse values for the field
                //
                final Optional<Parser<?>> parser = getParser( field );
                if ( !parser.isPresent() )
                {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Failed to parse @KvantumField [%s]: " +
                                    "KvantumFields may not be of type [%s]",
                                    field.getName(), field.getType().getName() ) );
                }
                //
                // Prepare the field and make it usable
                //
                field.setAccessible( true );
                //
                // Get the preferred field name
                //
                final String kvantumName;
                if ( kvantumField.kvantumName().isEmpty() )
                {
                    kvantumName = field.getName();
                } else
                {
                    kvantumName = kvantumField.kvantumName();
                }
                //
                // Retrieve the default value
                //
                final Object defaultValue;
                if ( kvantumField.defaultValue().equals( "null" ) )
                {
                    // In this case, we're just not gonna set the value when we get to it, and then
                    // we'll let the implementation deal with any potential issues arising from this
                    defaultValue = null;
                } else
                {
                    final ParserResult<?> parserResult = parser.get().parse( kvantumField.defaultValue() );
                    if ( parserResult.isParsed() )
                    {
                        defaultValue = parserResult.getResult();
                    } else
                    {
                        throw new IllegalArgumentException(
                                String.format(
                                        "Failed to parse @KvantumField [%s]: %s",
                                        field.getName(), parserResult.getError() ) );
                    }
                }
                clazzFields.put( kvantumName, new InternalKvantumField( kvantumName, defaultValue, field,
                        kvantumField, parser.get() ) );
            }
        }
        if ( clazzFields.isEmpty() )
        {
            throw new IllegalArgumentException(
                    String.format( "Class [%s] does not have any @KvantumField annotations!", clazz.getName() ) );
        }
        //
        // Now we have to find a suitable constructor
        //
        final Set<String> kvantumConstructorParameters = new LinkedHashSet<>();
        Constructor<T> kvantumConstructor = null;
        for ( final Constructor<?> constructor : clazz.getDeclaredConstructors() )
        {
            if ( constructor.getAnnotation( KvantumConstructor.class ) != null )
            {
                KvantumInsert insert;
                for ( final Parameter parameter : constructor.getParameters() )
                {
                    if ( ( insert = parameter.getAnnotation( KvantumInsert.class ) ) == null )
                    {
                        throw new IllegalArgumentException(
                                String.format( "Class [%s] does not have an appropriate constructor (missing " +
                                        "@KvantumField for parameter (%s)", clazz.getName(), parameter.getName() ) );
                    }
                    kvantumConstructorParameters.add( insert.value() );
                }
                constructor.setAccessible( true );
                kvantumConstructor = (Constructor<T>) constructor;
                break;
            }
        }
        if ( kvantumConstructor == null )
        {
            throw new IllegalArgumentException(
                    String.format( "Class [%s] does not have an appropriate constructor", clazz.getName() ) );
        }

        return new KvantumObjectFactory<>( new InternalKvantumConstructor<>( kvantumConstructor,
                kvantumConstructorParameters ), clazzFields );
    }

    public BuilderInstance build(final ParameterScope scope)
    {
        if ( scope == GET )
        {
            return new GetBuilderInstance();
        } else
        {
            return new PostBuilderInstance();
        }
    }

    @NoArgsConstructor( access = AccessLevel.PRIVATE )
    public abstract class BuilderInstance
    {

        protected abstract Map<String, String> getParameters(final Request request);

        /**
         * Parse an incoming request and attempt to construct the object
         * <p>
         * This factory will not check if the values are actually present beforehand, and instead generate
         * an error when parsing. If you want to confirm that the request contains all required parameters,
         * use the {@link RequestRequirements} framework beforehand
         * </p>
         * @param request Incoming request
         * @return Parsing result
         */
        public KvantumObjectParserResult<T> parseRequest(final Request request)
        {
            final Map<String, String> parameters = getParameters( request );
            final Map<String, Object> parsed = new HashMap<>();
            for ( final Map.Entry<String, InternalKvantumField> field : fields.entrySet() )
            {
                if ( parameters.containsKey( field.getKey() ) )
                {
                    final ParserResult<?> parserResult = field.getValue().parser
                            .parse( parameters.get( field.getKey() ) );
                    if ( !parserResult.isParsed() )
                    {
                        return new KvantumObjectParserResult<>( null, false, new KvantumObjectParserResult
                                .KvantumObjectParserCouldNotParse( field.getValue().getKvantumField(), parserResult ) );
                    }
                    parsed.put( field.getKey(), parserResult.getResult() );
                } else
                {
                    if ( field.getValue().getDefaultValue() == null )
                    {
                        if ( field.getValue().kvantumField.isRequired() )
                        {
                            return new KvantumObjectParserResult<>( null, false, new KvantumObjectParserResult
                                    .KvantumObjectParserMissingParameter( field.getValue().kvantumField ) );
                        }
                    } else
                    {
                        parsed.put( field.getKey(), field.getValue().getDefaultValue() );
                    }
                }
            }
            //
            // Determine the creation strategy
            //
            T instance;
            if ( constructor.getConstructorParameters().isEmpty() )
            {
                try
                {
                    instance = constructor.javaConstructor.newInstance();
                    for ( final Map.Entry<String, InternalKvantumField> field : fields.entrySet() )
                    {
                        final Object object = parsed.get( field.getKey() );
                        if ( object == null )
                        {
                            // We do not set null values, this is error prone and
                            // is due to misconfiguration
                            continue;
                        }
                        field.getValue().javaField.set( instance, parsed.get( field.getKey() ) );
                    }
                } catch ( final InstantiationException | IllegalAccessException | InvocationTargetException e )
                {
                    return new KvantumObjectParserResult<>( null, false, new KvantumObjectParserResult
                            .KvantumObjectParserInitializedFailed( e ) );
                }
            } else
            {
                final Object[] constructorParameters = new Object[ constructor.getConstructorParameters().size() ];
                int index = 0;
                for ( String s : constructor.getConstructorParameters() )
                {
                    constructorParameters[ index++ ] = parsed.get( s );
                }
                try
                {
                    instance = constructor.javaConstructor.newInstance( constructorParameters );
                } catch ( final InstantiationException | IllegalAccessException | InvocationTargetException e )
                {
                    return new KvantumObjectParserResult<>( null, false, new KvantumObjectParserResult
                            .KvantumObjectParserInitializedFailed( e ) );
                }
            }
            return new KvantumObjectParserResult<>( instance, true );
        }

    }

    @NoArgsConstructor( access = AccessLevel.PRIVATE )
    public final class GetBuilderInstance extends BuilderInstance
    {

        @Override
        protected Map<String, String> getParameters(final Request request)
        {
            return request.getQuery().getParameters();
        }
    }

    @NoArgsConstructor( access = AccessLevel.PRIVATE )
    public final class PostBuilderInstance extends BuilderInstance
    {

        @Override
        protected Map<String, String> getParameters(final Request request)
        {
            return request.getPostRequest().get();
        }
    }

    @Getter
    @AllArgsConstructor
    private static class InternalKvantumConstructor<T>
    {

        private final Constructor<T> javaConstructor;
        private final Set<String> constructorParameters;

    }

    @Getter
    @AllArgsConstructor
    private static class InternalKvantumField
    {

        private final String kvantumName;
        private final Object defaultValue;
        private final Field javaField;
        private final KvantumField kvantumField;
        private final Parser<?> parser;

    }

    private static Optional<Parser<?>> getParser(final Field field)
    {
        final Parser<?> parser;
        if ( field.getType().equals( Integer.class ) || field.getType().equals( int.class ) )
        {
            parser = new IntegerParser();
        } else if ( field.getType().equals( String.class ) )
        {
            parser = new StringParser();
        } else if ( field.getType().equals( Boolean.class ) || field.getType().equals( boolean.class ) )
        {
            parser = new BooleanParser();
        } else
        {
            parser = null;
        }
        return Optional.ofNullable( parser );
    }

}

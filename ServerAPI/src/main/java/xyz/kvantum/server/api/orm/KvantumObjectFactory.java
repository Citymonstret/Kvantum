/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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
package xyz.kvantum.server.api.orm;

import com.intellectualsites.commands.parser.Parser;
import com.intellectualsites.commands.parser.ParserResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import xyz.kvantum.server.api.orm.annotations.KvantumConstructor;
import xyz.kvantum.server.api.orm.annotations.KvantumField;
import xyz.kvantum.server.api.orm.annotations.KvantumInsert;
import xyz.kvantum.server.api.orm.annotations.KvantumObject;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.ParameterScope;
import xyz.kvantum.server.api.util.Parsers;
import xyz.kvantum.server.api.views.rest.RequestRequirements;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Framework for binding objects to GET/POST requests <p> Example:
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
@RequiredArgsConstructor public final class KvantumObjectFactory<T> {

    private static final Map<Class<?>, KvantumObjectFactory<?>> factoryCache =
        new ConcurrentHashMap<>();
    private static final Validator validator = new Validator();

    private final KvantumObject kvantumObjectDeclaration;
    private final InternalKvantumConstructor<T> constructor;
    private final Map<String, InternalKvantumField> fields;

    /**
     * Generate a new factory from a given class. The class must have a @KvantumObject annotation, and all fields used
     * in the generation of the object must have @KvantumField annotations. There must be at least one field in the
     * class with a @KvantumField annotation. If the class does not have a @KvantumConstructor annotated constructor,
     * then a default no-args constructor must be available. If the annotated @KvantumConstructor has no parameters,
     * then the fields will be updated directly instead. <p> This method will store the generated factory in a
     * concurrent cache, and will attempt to fetch the factory from the cache when the method is called. </p>
     *
     * @param clazz Class from which the factory will be generated
     * @param <T>   Type
     * @return Generated factory
     * @throws IllegalArgumentException If the class does not fit the specifications of a Kvantum object
     */
    @SuppressWarnings("ALL") public static <T> KvantumObjectFactory<T> from(final Class<T> clazz)
        throws IllegalArgumentException {
        final KvantumObject kvantumObject;
        if ((kvantumObject = clazz.getAnnotation(KvantumObject.class)) == null) {
            throw new IllegalArgumentException(String
                .format("Class [%s] does not have an @KvantumObject annotation!", clazz.getName()));
        }
        if (factoryCache.containsKey(clazz)) {
            return (KvantumObjectFactory<T>) factoryCache.get(clazz);
        }
        final Map<String, InternalKvantumField> clazzFields = new LinkedHashMap<>();
        KvantumField kvantumField;
        for (final Field field : clazz.getDeclaredFields()) {
            if ((kvantumField = field.getAnnotation(KvantumField.class)) != null) {
                //
                // Check if we're actually able to parse values for the field
                //
                final Optional<Parser<?>> parser = Parsers.getPrimitiveParser(field);
                if (!parser.isPresent()) {
                    throw new IllegalArgumentException(String.format(
                        "Failed to parse @KvantumField [{}]: "
                            + "KvantumFields may not be of type [{}]", field.getName(),
                        field.getType().getName()));
                }
                //
                // Prepare the field and make it usable
                //
                field.setAccessible(true);
                //
                // Get the preferred field name
                //
                final String kvantumName;
                if (kvantumField.kvantumName().isEmpty()) {
                    kvantumName = field.getName();
                } else {
                    kvantumName = kvantumField.kvantumName();
                }
                //
                // Retrieve the default value
                //
                final Object defaultValue;
                if (kvantumField.defaultValue().equals("null")) {
                    // In this case, we're just not gonna set the value when we get to it, and then
                    // we'll let the implementation deal with any potential issues arising from this
                    defaultValue = null;
                } else {
                    final ParserResult<?> parserResult =
                        parser.get().parse(kvantumField.defaultValue());
                    if (parserResult.isParsed()) {
                        defaultValue = parserResult.getResult();
                    } else {
                        throw new IllegalArgumentException(String
                            .format("Failed to parse @KvantumField [{}]: {}", field.getName(),
                                parserResult.getError()));
                    }
                }
                clazzFields.put(kvantumName,
                    new InternalKvantumField(kvantumName, defaultValue, field, kvantumField,
                        parser.get()));
            }
        }
        if (clazzFields.isEmpty()) {
            throw new IllegalArgumentException(String
                .format("Class [%s] does not have any @KvantumField annotations!",
                    clazz.getName()));
        }
        //
        // Now we have to find a suitable constructor
        //
        final Set<InternalKvantumConstructorParameter> kvantumConstructorParameters =
            new LinkedHashSet<>();
        Constructor<T> kvantumConstructor = null;
        for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getAnnotation(KvantumConstructor.class) != null) {
                KvantumInsert insert;
                for (final Parameter parameter : constructor.getParameters()) {
                    if ((insert = parameter.getAnnotation(KvantumInsert.class)) == null) {
                        throw new IllegalArgumentException(String.format(
                            "Class [%s] does not have an appropriate constructor (missing "
                                + "@KvantumField for parameter (%s)", clazz.getName(),
                            parameter.getName()));
                    }

                    final String fieldName = insert.value();

                    if (!clazzFields.containsKey(fieldName)) {
                        throw new IllegalArgumentException(String.format(
                            "Parameter '%s' in class '%s' is referring to kvantum field '%s',"
                                + " but no such kvantum field is declared", parameter.getName(),
                            clazz.getName(), fieldName));
                    }

                    final InternalKvantumField field = clazzFields.get(fieldName);
                    final Object defaultValue;
                    if ("null".equals(insert.defaultValue())) {
                        defaultValue = null;
                    } else {
                        final Parser<?> parser = field.getParser();
                        final ParserResult<?> parserResult = parser.parse(insert.defaultValue());
                        if (!parserResult.isParsed()) {
                            throw new IllegalArgumentException(String.format(
                                "Parameter '%s' in class '%s' is referring to kvantum field '%s',"
                                    + " but default value cannot be parsed: %s",
                                parameter.getName(), clazz.getName(), fieldName,
                                parserResult.getError()));
                        }
                        defaultValue = parserResult.getResult();
                    }

                    final InternalKvantumConstructorParameter kvantumConstructorParameter =
                        new InternalKvantumConstructorParameter(fieldName, field, defaultValue);
                    kvantumConstructorParameters.add(kvantumConstructorParameter);
                }
                constructor.setAccessible(true);
                kvantumConstructor = (Constructor<T>) constructor;
                break;
            }
        }
        if (kvantumConstructor == null) {
            throw new IllegalArgumentException(String
                .format("Class [%s] does not have an appropriate constructor", clazz.getName()));
        }

        final val factory = new KvantumObjectFactory<>(kvantumObject,
            new InternalKvantumConstructor<>(kvantumConstructor, kvantumConstructorParameters),
            clazzFields);
        factoryCache.put(clazz, factory);
        return factory;
    }

    public BuilderInstance build(final ParameterScope scope) {
        if (scope == ParameterScope.GET) {
            return new GetBuilderInstance();
        } else {
            return new PostBuilderInstance();
        }
    }

    @Getter @AllArgsConstructor private static class InternalKvantumConstructor<T> {

        private final Constructor<T> javaConstructor;
        private final Set<InternalKvantumConstructorParameter> constructorParameters;

    }


    @EqualsAndHashCode(of = "kvantumName") @Getter @AllArgsConstructor
    private static class InternalKvantumField {

        private final String kvantumName;
        private final Object defaultValue;
        private final Field javaField;
        private final KvantumField kvantumField;
        private final Parser<?> parser;

    }


    @EqualsAndHashCode(of = "parameterName") @Getter @AllArgsConstructor
    private static class InternalKvantumConstructorParameter {

        private final String parameterName;
        private final InternalKvantumField internalKvantumField;
        private final Object defaultValue;

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE) public final class GetBuilderInstance
        extends BuilderInstance {

        @Override protected Map<String, String> getParameters(final AbstractRequest request) {
            return request.getQuery().getParameters();
        }
    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE) public final class PostBuilderInstance
        extends BuilderInstance {

        @Override protected Map<String, String> getParameters(final AbstractRequest request) {
            return request.getPostRequest().get();
        }
    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE) public abstract class BuilderInstance {

        protected abstract Map<String, String> getParameters(final AbstractRequest request);

        /**
         * Parse an incoming request and attempt to construct the object <p> This factory will not check if the values
         * are actually present beforehand, and instead generate an error when parsing. If you want to confirm that the
         * request contains all required parameters, use the {@link RequestRequirements} framework beforehand </p>
         *
         * @param request Incoming request
         * @return Parsing result
         */
        public KvantumObjectParserResult<T> parseRequest(final AbstractRequest request) {
            final Map<String, String> parameters = getParameters(request);
            final Map<String, Object> parsed = new HashMap<>();
            for (final Map.Entry<String, InternalKvantumField> field : fields.entrySet()) {
                if (parameters.containsKey(field.getKey())) {
                    final ParserResult<?> parserResult =
                        field.getValue().parser.parse(parameters.get(field.getKey()));
                    if (!parserResult.isParsed()) {
                        return new KvantumObjectParserResult<>(null, false,
                            new KvantumObjectParserResult.KvantumObjectParserCouldNotParse(
                                field.getValue().getKvantumField(), parserResult));
                    }
                    parsed.put(field.getKey(), parserResult.getResult());
                } else {
                    if (field.getValue().getDefaultValue() == null) {
                        if (field.getValue().kvantumField.isRequired()) {
                            return new KvantumObjectParserResult<>(null, false,
                                new KvantumObjectParserResult.KvantumObjectParserMissingParameter(
                                    field.getValue().kvantumField));
                        }
                    } else {
                        parsed.put(field.getKey(), field.getValue().getDefaultValue());
                    }
                }
            }
            //
            // Determine the creation strategy
            //
            T instance;
            if (constructor.getConstructorParameters().isEmpty()) {
                try {
                    instance = constructor.javaConstructor.newInstance();
                    for (final Map.Entry<String, InternalKvantumField> field : fields.entrySet()) {
                        final Object object = parsed.get(field.getKey());
                        if (object == null) {
                            // We do not set null values, this is error prone and
                            // is due to misconfiguration
                            continue;
                        }
                        field.getValue().javaField.set(instance, parsed.get(field.getKey()));
                    }
                } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    return new KvantumObjectParserResult<>(null, false,
                        new KvantumObjectParserResult.KvantumObjectParserInitializedFailed(e));
                }
            } else {
                final Object[] constructorParameters =
                    new Object[constructor.getConstructorParameters().size()];
                int index = 0;
                for (InternalKvantumConstructorParameter parameter : constructor
                    .getConstructorParameters()) {
                    Object value;
                    if (parsed.containsKey(parameter.getParameterName())
                        && (value = parsed.get(parameter.getParameterName())) != null) {
                        constructorParameters[index++] = value;
                    } else {
                        constructorParameters[index++] = parameter.getDefaultValue();
                    }
                }
                try {
                    instance = constructor.javaConstructor.newInstance(constructorParameters);
                } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    return new KvantumObjectParserResult<>(null, false,
                        new KvantumObjectParserResult.KvantumObjectParserInitializedFailed(e));
                }
            }

            if (kvantumObjectDeclaration.checkValidity()) {
                final List<ConstraintViolation> violations = validator.validate(instance);
                if (!violations.isEmpty()) {
                    return new KvantumObjectParserResult<>(null, false,
                        new KvantumObjectParserResult.KvantumObjectParserValidationFailed(
                            violations));
                }
            }

            return new KvantumObjectParserResult<>(instance, true);
        }

    }

}

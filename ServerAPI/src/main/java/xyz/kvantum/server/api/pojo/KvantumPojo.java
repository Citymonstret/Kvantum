/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.api.pojo;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import xyz.kvantum.server.api.util.VariableProvider;

import java.util.Collection;
import java.util.Map;

/**
 * Representation of a POJO instance constructed
 * by a {@link KvantumPojoFactory} instance
 * <p>
 * Delegates {@link #toString()}, {@link #equals(Object)} and {@link #hashCode()}
 * to the POJO instance
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings({ "unused", "WeakerAccess" })
public final class KvantumPojo<Pojo> implements VariableProvider, JSONAware
{

    @NonNull
    private final KvantumPojoFactory<Pojo> factory;
    @NonNull
    private final Pojo instance;
    @NonNull
    private final Map<String, PojoGetter<Pojo>> fieldValues;
    @NonNull
    private final Map<String, PojoSetter<Pojo>> fieldSetters;

    @Override
    public boolean contains(final String variable)
    {
        return this.containsGetter( variable );
    }

    /**
     * Get a specified value. Will throw exceptions
     * if no such key is stored.
     *
     * @param key Key
     * @return Object
     * @see #containsGetter(String) To check if a key is stored
     */
    @SneakyThrows(NoSuchMethodException.class)
    public Object get(@NonNull final String key)
    {
        if ( !this.containsGetter( key ) )
        {
            throw new NoSuchMethodException( "No such getter: " + key );
        }
        return fieldValues.get( key ).get( this.instance );
    }

    /**
     * Get the names of all setters in the POJO
     *
     * @return Collection of field names
     */
    public Collection<String> getSetterNames()
    {
        return this.fieldSetters.keySet();
    }

    /**
     * Get the names of all getters in the POJO
     *
     * @return Collection of field names
     */
    public Collection<String> getGetterNames()
    {
        return this.fieldValues.keySet();
    }

    /**
     * Check if this instance contains a given key
     *
     * @param key Key to check for
     * @return True if the key exists
     */
    public boolean containsGetter(@NonNull final String key)
    {
        return fieldValues.containsKey( key );
    }

    /**
     * Check if this instance contains a given key
     *
     * @param key Key to check for
     * @return True if the key exists
     */
    public boolean containsSetter(@NonNull final String key)
    {
        return fieldSetters.containsKey( key );
    }

    /**
     * Get the values for all getters in the POJO
     *
     * @return Immutable map with all values
     */
    public Map<String, Object> getAll()
    {
        final ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();
        for ( final Map.Entry<String, PojoGetter<Pojo>> getterEntry : this.fieldValues.entrySet() )
        {
            mapBuilder.put( getterEntry.getKey(), getterEntry.getValue().get( instance ) );
        }
        return mapBuilder.build();
    }

    /**
     * Construct a {@link JSONObject} from this instance
     *
     * @return JSON object
     */
    public JSONObject toJson()
    {
        return this.factory.getJsonFactory().toJson( this );
    }

    /**
     * Update a field by accessing a setter in the POJO instance. Following the name
     * pattern "setField"
     *
     * @param field Field name
     * @param value Object value
     */
    @SneakyThrows({ IllegalArgumentException.class, NoSuchMethodException.class })
    public void set(@NonNull final String field,
                    @NonNull final Object value)
    {
        if ( this.fieldSetters.containsKey( field ) )
        {
            final PojoSetter<Pojo> setter = this.fieldSetters.get( field );
            if ( !value.getClass().isAssignableFrom( setter.getParameterType() ) )
            {
                throw new IllegalArgumentException( String.format( "Provided '%s', expected '%s'",
                        value.getClass().getSimpleName(), setter.getParameterType().getSimpleName() ) );
            }
            setter.set( instance, value );
        } else
        {
            throw new NoSuchMethodException( "No such method: " + field );
        }
    }

    /**
     * Get the POJO object represented by this class
     *
     * @return POJO instance
     */
    public Pojo getPojo()
    {
        return this.instance;
    }

    @Override
    public String toString()
    {
        return this.instance.toString();
    }

    @Override
    public int hashCode()
    {
        return this.instance.hashCode();
    }

    @Override
    public boolean equals(final Object object)
    {
        if ( object == null )
        {
            return false;
        } else if ( object instanceof KvantumPojo )
        {
            return this.instance.equals( ( (KvantumPojo) object ).getPojo() );
        } else
        {
            return this.instance.equals( object );
        }
    }

    /**
     * Get the immutable implementation of this instance
     *
     * @return Immutable version
     */
    public ImmutableKvantumPojo<Pojo> toImmutable()
    {
        return new ImmutableKvantumPojo<>( this.instance, this.fieldValues );
    }

    @Override
    public String toJSONString()
    {
        return this.toJson().toString();
    }
}

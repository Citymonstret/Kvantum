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
package xyz.kvantum.server.api.repository;

import lombok.NonNull;
import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;

import java.util.Map;

@SuppressWarnings("unused")
public final class FieldComparator<Q, V> extends Matcher<Q, V>
{

    private final KvantumPojo<Q> queryPojo;
    private final boolean ignoreUnassigned;
    private final boolean returnFirstMatch;

    private KvantumPojoFactory<V> valueFactory;

    /**
     * Initialize the comparator using a query object
     *
     * @param queryObject      Query object
     * @param ignoreUnassigned If null values should be ignored
     * @param returnFirstMatch If any matching fields should
     *                         count as a match
     */
    public FieldComparator(@NonNull final Q queryObject,
                           final boolean ignoreUnassigned,
                           final boolean returnFirstMatch)
    {
        super( queryObject );
        this.ignoreUnassigned = ignoreUnassigned;
        this.returnFirstMatch = returnFirstMatch;
        //
        // Create the factory class
        //
        final KvantumPojoFactory<Q> queryFactory = KvantumPojoFactory.forClass( getClass( queryObject ) );
        this.queryPojo = queryFactory.of( queryObject );
    }

    @SuppressWarnings("all")
    private static <T> Class<T> getClass(final T instance)
    {
        return (Class<T>) instance.getClass();
    }

    @Override
    protected boolean matches(@NonNull final Q query, @NonNull final V value)
    {
        if ( valueFactory == null )
        {
            valueFactory = KvantumPojoFactory.forClass( getClass( value ) );
        }
        final KvantumPojo<V> valuePojo = valueFactory.of( value );
        for ( final Map.Entry<String, Object> entry : valuePojo.getAll().entrySet() )
        {
            final Object entryValue = entry.getValue();
            final Object queryValue = queryPojo.get( entry.getKey() );
            if ( ignoreUnassigned && ( entryValue == null || queryValue == null ) )
            {
                continue;
            }
            if ( returnFirstMatch )
            {
                if ( entryValue.equals( queryValue ) )
                {
                    return true;
                }
            } else if ( !entryValue.equals( queryValue ) )
            {
                return false;
            }
        }
        return !returnFirstMatch;
    }
}

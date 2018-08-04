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

import com.github.rutledgepaulv.qbuilders.visitors.PredicateVisitor;
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import java.util.function.Predicate;

/**
 * Matcher that matches RSQL queries to objects.
 * Build using {@link RSQLMatcherFactory}
 */
@SuppressWarnings("WeakerAccess")
public final class RSQLMatcher<V> extends Matcher<String, V>
{

    private static final QueryConversionPipeline pipeline = QueryConversionPipeline.defaultPipeline();

    private Predicate<V> predicate;

    RSQLMatcher(@NonNull final String queryObject)
    {
        super( queryObject );
    }

    @Override
    protected boolean matches(@Nullable final String queryObject,
                              @NonNull final V value)
    {
        if ( this.predicate == null )
        {
            this.predicate = pipeline.apply( queryObject, getClass( value ) ).query( new PredicateVisitor<>() );
        }
        return this.predicate.test( value );
    }

    @SuppressWarnings("ALL")
    private Class<V> getClass(@NonNull final V instance)
    {
        return (Class<V>) instance.getClass();
    }

}

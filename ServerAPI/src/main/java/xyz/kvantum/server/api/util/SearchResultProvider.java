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
package xyz.kvantum.server.api.util;

import java.util.Collection;

/**
 * An interface for objects that perform a query and returns a collection of results
 */
@FunctionalInterface
public interface SearchResultProvider<QueryType, ObjectType>
{

    /**
     * Get all results matching the given query
     *
     * @param query Query
     * @return Collection containing all the matching results
     */
    Collection<? extends ObjectType> getResults(QueryType query);

}

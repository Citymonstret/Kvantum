/*
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
package xyz.kvantum.server.api.verification.rules;

import xyz.kvantum.server.api.verification.Rule;

public final class NotNull<T> implements Rule<T>
{

    @Override
    public String getRuleDescription()
    {
        return "Supplied object cannot be null";
    }

    @Override
    public boolean test(final Object o)
    {
        return o != null;
    }

}

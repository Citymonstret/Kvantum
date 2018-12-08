/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
package xyz.kvantum.server.api.verification;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Singular;
import xyz.kvantum.server.api.verification.rules.NotNull;

import java.util.Collection;
import java.util.Collections;

public class Verifier<T> {

    private final Collection<Rule<T>> rules;

    private boolean nullable = false;

    @Builder private Verifier(@Singular("withRule") final Collection<Rule<T>> rules,
        final boolean nullable) {
        this.rules = ImmutableList.copyOf(rules);
        this.nullable = nullable;
    }

    /**
     * Verify an object and get a list of all the broken verification rules
     *
     * @param object Object to verify
     * @return Collection of broken rules, empty if the supplied object is valid
     */
    public Collection<Rule<T>> verify(final T object) {
        if (!this.nullable && object == null) {
            return Collections.singletonList(new NotNull<T>());
        }
        final ImmutableCollection.Builder<Rule<T>> ruleBuilder = ImmutableList.builder();
        for (final Rule<T> rule : this.rules) {
            if (rule.test(object)) {
                continue;
            }
            ruleBuilder.add(rule);
        }
        return ruleBuilder.build();
    }

}

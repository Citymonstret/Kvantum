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
package xyz.kvantum.server.api.matching;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import xyz.kvantum.server.api.exceptions.KvantumException;
import xyz.kvantum.server.api.util.VariableHolder;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor(access = AccessLevel.PRIVATE) public final class FilePattern {

    private static final Pattern PATTERN_VARIABLE =
        Pattern.compile("\\$\\{(?<variable>[A-Za-z0-9]*)}");

    private final String pattern;
    private final Map<String, String> variableMap;

    @Nonnull public static FilePattern compile(@NonNull final String in) {
        final Matcher matcher = PATTERN_VARIABLE.matcher(in);
        final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        while (matcher.find()) {
            mapBuilder.put(matcher.group(), matcher.group("variable"));
        }
        return new FilePattern(in, mapBuilder.build());
    }

    @Nonnull public FileMatcher matcher(final VariableHolder holder) {
        return new FileMatcher(holder);
    }

    @SuppressWarnings("WeakerAccess") final public static class FilePatternException
        extends KvantumException {

        private FilePatternException(final String message) {
            super("Failed to handle file pattern: " + message);
        }
    }


    public class FileMatcher {

        private final Map<String, String> variableMapping;
        private String compiledName = null;

        private FileMatcher(@Nonnull @NonNull final VariableHolder variableHolder) {
            final ImmutableMap.Builder<String, String> variableMappingBuilder =
                ImmutableMap.builder();
            final Map<String, String> requestVariables = variableHolder.getVariables();
            for (final Map.Entry<String, String> entry : variableMap.entrySet()) {
                if (!requestVariables.containsKey(entry.getValue())) {
                    break;
                }
                variableMappingBuilder
                    .put(entry.getValue(), requestVariables.get(entry.getValue()));
            }
            this.variableMapping = variableMappingBuilder.build();
        }

        public boolean matches() {
            return this.variableMapping.size() == variableMap.size();
        }

        public String getFileName() {
            if (this.compiledName == null) {
                if (!this.matches()) {
                    throw new FilePatternException(
                        "Trying to use #getFileName when matches = false");
                }
                String rawName = pattern;
                for (final Map.Entry<String, String> variable : variableMap.entrySet()) {
                    rawName = rawName
                        .replace(variable.getKey(), this.variableMapping.get(variable.getValue()));
                }
                this.compiledName = rawName;
            }
            return this.compiledName;
        }
    }

}

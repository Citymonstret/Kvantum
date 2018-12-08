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
package xyz.kvantum.crush.syntax;

import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Macro extends Syntax {

    private static final Pattern MACRO_USAGE =
        Pattern.compile("#(?<name>[A-Za-z0-9]+) \\((?<params>[\"A-Za-z0-9\\s]*)\\)#");

    private static final Pattern MACRO_USAGE_PARAM = Pattern.compile("\"(?<param>[^\".]*)\"");

    public Macro() {
        super(Pattern.compile("\\{#macro (?<name>[A-Za-z0-9]+)(?<params>[A-Za-z0-9\\s]*)}"
            + "(?<body>[A-Za-z0-9<>\"'-_\\/\\\\ }{}\\n\\s]*)" + "\\{/macro}"));
    }

    @Override public String process(String in, final Matcher matcher, final AbstractRequest r,
        final Map<String, ProviderFactory<? extends VariableProvider>> factories) {
        final Map<String, DefinedMacro> macroMap = new HashMap<>();

        while (matcher.find()) {
            final String macroName = matcher.group("name");
            final String params = matcher.group("params");
            final String body = matcher.group("body");
            final List<String> parameterList = new LinkedList<>();
            final StringTokenizer paramTokenizer = new StringTokenizer(params);
            while (paramTokenizer.hasMoreTokens()) {
                parameterList.add(paramTokenizer.nextToken());
            }
            macroMap.put(macroName, new DefinedMacro(body, parameterList));
            in = in.replace(matcher.group(), "");
        }

        final Matcher usageMatcher = MACRO_USAGE.matcher(in);
        while (usageMatcher.find()) {
            final String macroName = usageMatcher.group("name");
            if (!macroMap.containsKey(macroName)) {
                Logger.warn("Crush template requesting invalid macro: {}", macroName);
                continue;
            }
            final DefinedMacro definedMacro = macroMap.get(macroName);
            final String params = usageMatcher.group("params");
            final Matcher paramUsage = MACRO_USAGE_PARAM.matcher(params);
            int index = 0;
            String replacementString = definedMacro.body;
            while (paramUsage.find()) {
                final String value = paramUsage.group("param");
                if (index < definedMacro.parameters.size()) {
                    replacementString = replacementString
                        .replace("{{" + definedMacro.parameters.get(index++) + "}}", value);
                }
            }
            while (index++ < definedMacro.parameters.size()) {
                replacementString = replacementString
                    .replace("{{" + definedMacro.parameters.get(index++) + "}}", "");
            }
            in = in.replace(usageMatcher.group(), replacementString);
        }
        return in;
    }

    private static class DefinedMacro {

        private final String body;
        private final List<String> parameters;

        private DefinedMacro(final String body, final List<String> parameters) {
            this.body = body;
            this.parameters = parameters;
        }

    }

}

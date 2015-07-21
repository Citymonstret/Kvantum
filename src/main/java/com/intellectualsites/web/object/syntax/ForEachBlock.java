//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualsites.web.object.syntax;

import com.intellectualsites.web.object.Request;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForEachBlock extends Syntax {

    public ForEachBlock() {
        super(Pattern.compile("\\{#foreach ([A-Za-z0-9]*).([A-Za-z0-9]*) -> ([A-Za-z0-9]*)\\}([A-Za-z0-9<>\"'-_\\/\\\\ }{}\\n\\s]*)\\{\\/foreach\\}"));
    }

    public String process(String content, Matcher matcher, Request r, Map<String, ProviderFactory> factories) {
        while (matcher.find()) {
            String provider = matcher.group(1);
            String variable = matcher.group(2);
            String variableName = matcher.group(3);
            String forContent = matcher.group(4);

            if (factories.containsKey(provider.toLowerCase())) {
                VariableProvider p = factories.get(provider.toLowerCase()).get(r);
                if (p != null) {
                    if (!p.contains(variable)) {
                        content = content.replace(matcher.group(), "");
                    } else {
                        Object o = p.get(variable);

                        StringBuilder totalContent = new StringBuilder();
                        if (o instanceof Object[]) {
                            for (Object oo : (Object[]) o) {
                                totalContent.append(forContent.replace("{{" + variableName + "}}", oo.toString()));
                            }
                        } else if (o instanceof Collection) {
                            for (Object oo : (Collection) o) {
                                totalContent.append(forContent.replace("{{" + variableName + "}}", oo.toString()));
                            }
                        }
                        content = content.replace(matcher.group(), totalContent.toString());
                    }
                } else {
                    content = content.replace(matcher.group(), "");
                }
            }  else {
                content = content.replace(matcher.group(), "");
            }
        }
        return content;
    }
}

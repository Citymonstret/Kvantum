package com.github.intellectualsites.kvantum.crush.syntax;

import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.util.ProviderFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Macro extends Syntax
{

    private static final Pattern MACRO_USAGE = Pattern.compile(
            "#(?<name>[A-Za-z0-9]+)([\\S\\s]*)\\((?<params>[\"A-Za-z0-9\\s]*)\\)([\\S\\s]*)#"
    );

    private static final Pattern MACRO_USAGE_PARAM = Pattern.compile(
            "\"(?<param>[^\".]*)\""
    );

    public Macro()
    {
        super( Pattern.compile(
                "\\{#macro (?<name>[A-Za-z0-9]+)(?<params>[A-Za-z0-9\\s]*)}" +
                        "(?<body>[A-Za-z0-9<>\"'-_\\/\\\\ }{}\\n\\s]*)" +
                        "\\{/macro}"
        ) );
    }

    @Override
    public String process(String in, final Matcher matcher, final Request r,
                          final Map<String, ProviderFactory> factories)
    {
        final Map<String, DefinedMacro> macroMap = new HashMap<>();

        while ( matcher.find() )
        {
            final String macroName = matcher.group( "name" );
            final String params = matcher.group( "params" );
            final String body = matcher.group( "body" );
            final List<String> parameterList = new LinkedList<>();
            final StringTokenizer paramTokenizer = new StringTokenizer( params );
            while ( paramTokenizer.hasMoreTokens() )
            {
                parameterList.add( paramTokenizer.nextToken() );
            }
            macroMap.put( macroName, new DefinedMacro( body, parameterList ) );
            in = in.replace( matcher.group(), "" );
        }

        final Matcher usageMatcher = MACRO_USAGE.matcher( in );
        while ( usageMatcher.find() )
        {
            final String macroName = usageMatcher.group( "name" );
            if ( !macroMap.containsKey( macroName ) )
            {
                Logger.warn( "Crush template requesting invalid macro: %s", macroName );
                continue;
            }
            final DefinedMacro definedMacro = macroMap.get( macroName );
            final String params = usageMatcher.group( "params" );
            final Matcher paramUsage = MACRO_USAGE_PARAM.matcher( params );
            int index = 0;
            String replacementString = definedMacro.body;
            while ( paramUsage.find() )
            {
                final String value = paramUsage.group( "param" );
                if ( index < definedMacro.parameters.size() )
                {
                    replacementString = replacementString
                            .replace( "{{" + definedMacro.parameters.get( index++ ) + "}}", value );
                }
            }
            while ( index++ < definedMacro.parameters.size() )
            {
                replacementString = replacementString
                        .replace( "{{" + definedMacro.parameters.get( index++ ) + "}}", "" );
            }
            in = in.replace( usageMatcher.group(), replacementString );
        }
        return in;
    }

    private static class DefinedMacro
    {

        private final String body;
        private final List<String> parameters;

        private DefinedMacro(final String body, final List<String> parameters)
        {
            this.body = body;
            this.parameters = parameters;
        }

    }

}

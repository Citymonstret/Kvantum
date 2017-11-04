package com.github.intellectualsites.iserver.api.matching;

import com.github.intellectualsites.iserver.api.exceptions.IntellectualServerException;
import com.github.intellectualsites.iserver.api.util.VariableHolder;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final public class FilePattern
{

    private static final Pattern PATTERN_VARIABLE = Pattern.compile( "\\$\\{(?<variable>[A-Za-z0-9]*)}" );

    private final String pattern;
    private final Map<String, String> variableMap;

    public static FilePattern compile(final String in)
    {
        final Matcher matcher = PATTERN_VARIABLE.matcher( in );
        final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        while ( matcher.find() )
        {
            mapBuilder.put( matcher.group(), matcher.group( "variable" ) );
        }
        return new FilePattern( in, mapBuilder.build() );
    }

    public FileMatcher matcher(final VariableHolder holder)
    {
        return new FileMatcher( holder );
    }

    @SuppressWarnings("WeakerAccess")
    public class FileMatcher
    {

        private final Map<String, String> variableMapping;
        private String compiledName = null;

        private FileMatcher(final VariableHolder variableHolder)
        {
            final ImmutableMap.Builder<String, String> variableMappingBuilder = ImmutableMap.builder();
            final Map<String, String> requestVariables = variableHolder.getVariables();
            for ( final Map.Entry<String, String> entry : variableMap.entrySet() )
            {
                if ( !requestVariables.containsKey( entry.getValue() ) )
                {
                    break;
                }
                variableMappingBuilder.put( entry.getValue(), requestVariables.get( entry.getValue() ) );
            }
            this.variableMapping = variableMappingBuilder.build();
        }

        public boolean matches()
        {
            return this.variableMapping.size() == variableMap.size();
        }

        public String getFileName()
        {
            if ( this.compiledName == null )
            {
                if ( !this.matches() )
                {
                    throw new FilePatternException( "Trying to use #getFileName when matches = false" );
                }
                String rawName = pattern;
                for ( final Map.Entry<String, String> variable : variableMap.entrySet() )
                {
                    System.out.println( "Variable name:" + variable.getKey() );
                    System.out.println( "Variable value:" + variable.getValue() );

                    rawName = rawName.replace( variable.getKey(), this.variableMapping.get( variable.getValue() ) );
                }
                this.compiledName = rawName;
            }
            return this.compiledName;
        }
    }

    @SuppressWarnings("WeakerAccess")
    final public class FilePatternException extends IntellectualServerException
    {

        private FilePatternException(final String message)
        {
            super( "Failed to handle file pattern: " + message );
        }
    }

}

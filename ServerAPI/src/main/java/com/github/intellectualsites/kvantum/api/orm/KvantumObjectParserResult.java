package com.github.intellectualsites.kvantum.api.orm;

import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumField;
import com.intellectualsites.commands.parser.ParserResult;
import lombok.*;

@Getter
@AllArgsConstructor( access = AccessLevel.PACKAGE )
@RequiredArgsConstructor( access = AccessLevel.PACKAGE )
final public class KvantumObjectParserResult<T>
{
    private final T parsedObject;
    private final boolean success;
    private KvantumObjectParserError error;

    @AllArgsConstructor( access = AccessLevel.PACKAGE )
    public static class KvantumObjectParserCouldNotParse extends KvantumObjectParserError
    {
        @Getter
        private final KvantumField missingField;

        @Getter
        private final ParserResult parserResult;

        @Override
        public String getCause()
        {
            return String.format( "Could not parse field: \"%s\". Reason: %s", missingField.kvantumName(),
                    parserResult.getError() );
        }
    }

    @AllArgsConstructor( access = AccessLevel.PACKAGE )
    public static class KvantumObjectParserInitializedFailed extends KvantumObjectParserError
    {

        @Getter
        private final Throwable exception;

        @Override
        public String getCause()
        {
            return "Failed to create new instance: \"" + exception.getMessage() + "\"";
        }

    }

    @AllArgsConstructor( access = AccessLevel.PACKAGE )
    public static class KvantumObjectParserMissingParameter extends KvantumObjectParserError
    {
        @Getter
        private final KvantumField missingField;

        @Override
        public String getCause()
        {
            return "Missing field: \"" + missingField.kvantumName() + "\"";
        }
    }

    @NoArgsConstructor( access = AccessLevel.PRIVATE )
    public static abstract class KvantumObjectParserError
    {

        public abstract String getCause();

    }

}

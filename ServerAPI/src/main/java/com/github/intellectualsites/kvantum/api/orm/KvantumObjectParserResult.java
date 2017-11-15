/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.orm;

import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumField;
import com.github.intellectualsites.kvantum.api.util.CollectionUtil;
import com.intellectualsites.commands.parser.ParserResult;
import lombok.*;
import net.sf.oval.ConstraintViolation;

import java.util.List;

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

    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class KvantumObjectParserValidationFailed extends KvantumObjectParserError
    {

        private final List<ConstraintViolation> violations;

        @Override
        public String getCause()
        {
            return "Violations failed: " + CollectionUtil.join( violations, ", " );
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

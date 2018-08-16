/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.api.orm;

import com.intellectualsites.commands.parser.ParserResult;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sf.oval.ConstraintViolation;
import xyz.kvantum.server.api.orm.annotations.KvantumField;
import xyz.kvantum.server.api.util.CollectionUtil;

@Getter @AllArgsConstructor(access = AccessLevel.PACKAGE) @RequiredArgsConstructor(access = AccessLevel.PACKAGE) final public class KvantumObjectParserResult<T>
{

	private final T parsedObject;
	private final boolean success;
	private KvantumObjectParserError error;

	@AllArgsConstructor(access = AccessLevel.PACKAGE) public static class KvantumObjectParserCouldNotParse
			extends KvantumObjectParserError
	{

		@Getter private final KvantumField missingField;

		@Getter private final ParserResult parserResult;

		@Override public String getCause()
		{
			return String.format( "Could not parse field: \"%s\". Reason: %s", missingField.kvantumName(),
					parserResult.getError() );
		}
	}

	@AllArgsConstructor(access = AccessLevel.PACKAGE) public static class KvantumObjectParserInitializedFailed
			extends KvantumObjectParserError
	{

		@Getter private final Throwable exception;

		@Override public String getCause()
		{
			return "Failed to create new instance: \"" + exception.getMessage() + "\"";
		}

	}

	@AllArgsConstructor(access = AccessLevel.PACKAGE) public static class KvantumObjectParserValidationFailed
			extends KvantumObjectParserError
	{

		@Getter private final List<ConstraintViolation> violations;

		@Override public String getCause()
		{
			return "Violations failed: " + CollectionUtil.join( violations, ", " );
		}
	}

	@AllArgsConstructor(access = AccessLevel.PACKAGE) public static class KvantumObjectParserMissingParameter
			extends KvantumObjectParserError
	{

		@Getter private final KvantumField missingField;

		@Override public String getCause()
		{
			return "Missing field: \"" + missingField.kvantumName() + "\"";
		}
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE) public static abstract class KvantumObjectParserError
	{

		public abstract String getCause();

	}

}

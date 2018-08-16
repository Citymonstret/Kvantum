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
package xyz.kvantum.server.api.util;

import java.util.Map;

/**
 * The variable provider class - Can get quite confusing. <p> This generates a variable based on a key, and is accessed
 * like
 * <pre>
 * {@code
 * {{PROVIDER.NAME}}
 * }
 * </pre>
 * whereas the name would be the variable key
 */
public interface VariableProvider
{

	/**
	 * Does the provider contain this variable?
	 *
	 * @param variable Variable Key
	 * @return True if the variable exists
	 */
	boolean contains(String variable);

	/**
	 * Get the variable
	 *
	 * @param variable Variable Key
	 * @return The object (or null)
	 * @see #contains(String) Use this to check if it exists
	 */
	Object get(String variable);

	/**
	 * Get all variables stored in the provider
	 *
	 * @return map containing all the stored variables
	 */
	Map<String, Object> getAll();

}

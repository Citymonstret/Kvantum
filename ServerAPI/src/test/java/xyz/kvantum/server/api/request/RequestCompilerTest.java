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
package xyz.kvantum.server.api.request;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.kvantum.server.api.request.RequestCompiler.HeaderPair;

class RequestCompilerTest
{

	@Test void compileHeader()
	{
		final Optional<HeaderPair> headerPairOptional = RequestCompiler.compileHeader( "Random-Header: value" );
		Assertions.assertNotNull( headerPairOptional );
		Assertions.assertTrue( headerPairOptional.isPresent() );
		final HeaderPair headerPair = headerPairOptional.get();
		Assertions.assertEquals( "random-header", headerPair.getKey().toString() );
		Assertions.assertEquals( "value", headerPair.getValue().toString() );
	}
}

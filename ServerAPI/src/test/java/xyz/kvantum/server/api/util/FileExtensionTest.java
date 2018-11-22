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
package xyz.kvantum.server.api.util;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FileExtensionTest
{

	@Test void getExtension()
	{
		final String string1 = ".txt";
		final String string2 = "JPEG";
		final Optional<FileExtension> ext1 = FileExtension.getExtension( string1 );
		final Optional<FileExtension> ext2 = FileExtension.getExtension( string2 );
		Assertions.assertTrue( ext1.isPresent() );
		Assertions.assertTrue( ext2.isPresent() );
		Assertions.assertEquals( FileExtension.TXT, ext1.get() );
		Assertions.assertEquals( FileExtension.JPEG, ext2.get() );
	}

}

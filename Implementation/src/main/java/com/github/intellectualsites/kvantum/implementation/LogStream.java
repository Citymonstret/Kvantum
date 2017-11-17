/*
 *
 *    Copyright (C) 2017 IntellectualSites
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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.io.AsyncBufferedOutputStream;
import com.github.intellectualsites.kvantum.api.util.TimeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

final class LogStream extends PrintStream
{

    LogStream(final File logFolder) throws FileNotFoundException
    {
        super( new AsyncBufferedOutputStream( new FileOutputStream(
                new File( logFolder, TimeUtil.getTimeStamp( TimeUtil.logFileFormat, new Date() ) + ".txt" ) ) ) );
    }

}

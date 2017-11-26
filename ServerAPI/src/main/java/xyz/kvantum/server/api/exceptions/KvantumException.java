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
package xyz.kvantum.server.api.exceptions;

public class KvantumException extends RuntimeException
{

    public KvantumException(final String message)
    {
        super( "Kvantum threw an exception: " + message );
    }

    public KvantumException(final String message, final Throwable cause)
    {
        super( "Kvantum threw an exception: " + message, cause );
    }

    public KvantumException(final Throwable cause)
    {
        super( cause );
    }

}
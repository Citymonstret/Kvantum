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
package xyz.kvantum.server.implementation;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Locale;
import xyz.kvantum.server.api.config.CoreConfig;

/**
 * This is primarily used to check if the system output stream is used inside of the logger, but might be extended to
 * handle more in the future
 */
@SuppressWarnings("ALL") final class ServerSecurityManager
{

	static class SecurePrintStream extends PrintStream
	{

		private PrintStream old;

		private SecurePrintStream(final OutputStream outputStream)
		{
			super( outputStream );
		}

		static SecurePrintStream construct(final PrintStream old) throws Exception
		{
			final Field field = FilterOutputStream.class.getDeclaredField( "out" );
			field.setAccessible( true );
			final OutputStream outputStream = ( OutputStream ) field.get( old );
			final SecurePrintStream stream = new SecurePrintStream( outputStream );
			stream.old = old;
			return stream;
		}

		@SuppressWarnings("unchecked") static <T extends Throwable> void sneakyThrow(final Throwable t) throws T
		{
			throw ( T ) t;
		}

		@Override public void println()
		{
			checkPermission();
			old.println();
		}

		@Override public void print(boolean b)
		{
			checkPermission();
			old.print( b );
		}

		@Override public void print(char c)
		{
			checkPermission();
			old.print( c );
		}

		@Override public void print(int i)
		{
			checkPermission();
			old.print( i );
		}

		@Override public void print(long l)
		{
			checkPermission();
			old.print( l );
		}

		@Override public void print(float f)
		{
			checkPermission();
			old.print( f );
		}

		@Override public void print(double d)
		{
			checkPermission();
			old.print( d );
		}

		@Override public void print(char[] s)
		{
			checkPermission();
			old.print( s );
		}

		@Override public void print(String s)
		{
			checkPermission();
			old.print( s );
		}

		@Override public void print(Object obj)
		{
			checkPermission();
			old.print( obj );
		}

		@Override public void println(boolean x)
		{
			checkPermission();
			old.println( x );
		}

		@Override public void println(char x)
		{
			checkPermission();
			old.println( x );
		}

		@Override public void println(int x)
		{
			checkPermission();
			old.println( x );
		}

		@Override public void println(long x)
		{
			checkPermission();
			old.println( x );
		}

		@Override public void println(float x)
		{
			checkPermission();
			old.println( x );
		}

		@Override public void println(double x)
		{
			checkPermission();
			old.println( x );
		}

		@Override public void println(char[] x)
		{
			checkPermission();
			old.println( x );
		}

		@Override public void println(String x)
		{
			checkPermission();
			old.println( x );
		}

		@Override public void println(Object x)
		{
			checkPermission();
			old.println( x );
		}

		@Override public PrintStream printf(String format, Object... args)
		{
			checkPermission();
			return old.printf( format, args );
		}

		@Override public PrintStream printf(Locale l, String format, Object... args)
		{
			checkPermission();
			return old.printf( l, format, args );
		}

		void checkPermission()
		{
			final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
			for ( final StackTraceElement element : stackTraceElements )
			{
				if ( element.getClassName().contains( "LogWrapper" ) )
				{
					return;
				}
			}
			if ( CoreConfig.debug )
			{
				for ( final StackTraceElement element : stackTraceElements )
				{
					old.println( element );
				}
			}
			sneakyThrow( new IllegalAccessException( "Non-Logger tried to use System.out!" ) );
		}
	}
}

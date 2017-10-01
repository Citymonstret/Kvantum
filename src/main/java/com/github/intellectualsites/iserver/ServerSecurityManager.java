/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
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
package com.github.intellectualsites.iserver;

import com.github.intellectualsites.iserver.api.config.CoreConfig;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Locale;

/**
 * This is primarily used to check if the system output stream is used
 * inside of the logger, but might be extended to handle more in the
 * future
 */
final class ServerSecurityManager
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
            final OutputStream outputStream = (OutputStream) field.get( old );
            final SecurePrintStream stream = new SecurePrintStream( outputStream );
            stream.old = old;
            return stream;
        }

        @SuppressWarnings("unchecked")
        static <T extends Throwable> void sneakyThrow(final Throwable t) throws T
        {
            throw (T) t;
        }

        @Override
        public void println()
        {
            checkPermission();
            old.println();
        }

        @Override
        public void print(boolean b)
        {
            checkPermission();
            old.print( b );
        }

        @Override
        public void print(char c)
        {
            checkPermission();
            old.print( c );
        }

        @Override
        public void print(int i)
        {
            checkPermission();
            old.print( i );
        }

        @Override
        public void print(long l)
        {
            checkPermission();
            old.print( l );
        }

        @Override
        public void print(float f)
        {
            checkPermission();
            old.print( f );
        }

        @Override
        public void print(double d)
        {
            checkPermission();
            old.print( d );
        }

        @Override
        public void print(char[] s)
        {
            checkPermission();
            old.print( s );
        }

        @Override
        public void print(String s)
        {
            checkPermission();
            old.print( s );
        }

        @Override
        public void print(Object obj)
        {
            checkPermission();
            old.print( obj );
        }

        @Override
        public void println(boolean x)
        {
            checkPermission();
            old.println( x );
        }

        @Override
        public void println(char x)
        {
            checkPermission();
            old.println( x );
        }

        @Override
        public void println(int x)
        {
            checkPermission();
            old.println( x );
        }

        @Override
        public void println(long x)
        {
            checkPermission();
            old.println( x );
        }

        @Override
        public void println(float x)
        {
            checkPermission();
            old.println( x );
        }

        @Override
        public void println(double x)
        {
            checkPermission();
            old.println( x );
        }

        @Override
        public void println(char[] x)
        {
            checkPermission();
            old.println( x );
        }

        @Override
        public void println(String x)
        {
            checkPermission();
            old.println( x );
        }

        @Override
        public void println(Object x)
        {
            checkPermission();
            old.println( x );
        }

        @Override
        public PrintStream printf(String format, Object... args)
        {
            checkPermission();
            return old.printf( format, args );
        }

        @Override
        public PrintStream printf(Locale l, String format, Object... args)
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

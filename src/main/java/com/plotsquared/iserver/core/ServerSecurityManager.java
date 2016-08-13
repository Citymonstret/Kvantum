package com.plotsquared.iserver.core;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Locale;

public class ServerSecurityManager
{

    static class SecurePrintStream extends PrintStream
    {
        
        private PrintStream old;
        
        static SecurePrintStream construct(final PrintStream old) throws Exception
        {
            final Field field = FilterOutputStream.class.getDeclaredField( "out" );
            field.setAccessible( true );
            final OutputStream outputStream = (OutputStream) field.get( old );
            final SecurePrintStream stream = new SecurePrintStream( outputStream );
            stream.old = old;
            return stream;
        }
        
        private SecurePrintStream(final OutputStream outputStream)
        {
            super( outputStream );
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

        void checkPermission() {
            final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            for ( final StackTraceElement element : stackTraceElements )
            {
                if ( element.getClassName().contains( "LogWrapper" ) ) {
                    return;
                }
            }
            sneakyThrow( new IllegalAccessException( "Non-Logger tried to use System.out!" ) );
        }

        @SuppressWarnings("unchecked")
        static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
            throw (T) t;
        }
    }
}

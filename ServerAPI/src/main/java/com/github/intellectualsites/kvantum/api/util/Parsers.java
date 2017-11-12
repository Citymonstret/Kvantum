package com.github.intellectualsites.kvantum.api.util;

import com.intellectualsites.commands.parser.Parser;
import com.intellectualsites.commands.parser.ParserResult;
import com.intellectualsites.commands.parser.impl.BooleanParser;
import com.intellectualsites.commands.parser.impl.IntegerParser;
import com.intellectualsites.commands.parser.impl.StringParser;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({ "WeakerAccess", "unused" })
@UtilityClass
public class Parsers
{

    private static final Map<Type, Parser<?>> primitiveParserMap = MapBuilder.<Type, Parser<?>>newHashMap()
            .put( int.class, new IntegerParser() )
            .put( String.class, new StringParser() )
            .put( byte.class, new ByteParser() )
            .put( char.class, new CharParser() )
            .put( short.class, new ShortParser() )
            .put( long.class, new LongParser() )
            .put( float.class, new FloatParser() )
            .put( double.class, new DoubleParser() )
            .put( boolean.class, new BooleanParser() ).get();

    public static Optional<Parser<?>> getPrimitiveParser(final Field field)
    {
        return Optional.ofNullable( primitiveParserMap.getOrDefault( field.getGenericType(), null ) );
    }

    public static final class ByteParser extends Parser<Byte>
    {

        private byte min;
        private byte max;
        private boolean hasRange;

        public ByteParser()
        {
            super( "byte", 0b0001 );
        }

        public ByteParser(final byte min, final byte max)
        {
            this();
            this.min = min;
            this.max = max;
            this.hasRange = false;
        }

        @Override
        public ParserResult<Byte> parse(final String in)
        {
            Byte value = null;

            try
            {
                value = Byte.parseByte( in );
            } catch ( final Exception ignored )
            {
            }

            if ( this.hasRange && value != null )
            {
                byte b = value;
                b = (byte) Math.min( b, this.max );
                b = (byte) Math.max( b, this.min );
                if ( b != value )
                {
                    return new ParserResult<>( value + " is not within range (" + this.min + " -> " + this.max + ")" );
                }
            }

            return value != null ? new ParserResult<>( value ) : new ParserResult<>( in + " is not a byte" );
        }
    }

    public static final class CharParser extends Parser<Character>
    {

        private char min;
        private char max;
        private boolean hasRange = false;

        public CharParser()
        {
            super( "char", 'b' );
        }

        public CharParser(final char min, final char max)
        {
            this();
            this.min = min;
            this.max = max;
            this.hasRange = true;
        }

        @Override
        public ParserResult<Character> parse(final String in)
        {
            Character value = null;

            try
            {
                value = in.charAt( 0 );
            } catch ( final Exception ignored )
            {
            }

            if ( this.hasRange && value != null )
            {
                char b = value;
                b = (char) Math.min( b, this.max );
                b = (char) Math.max( b, this.min );
                if ( b != value )
                {
                    return new ParserResult<>( value + " is not within range (" + this.min + " -> " + this.max + ")" );
                }
            }

            return value != null ? new ParserResult<>( value ) : new ParserResult<>( in + " is not a character" );
        }
    }

    public static final class ShortParser extends Parser<Short>
    {

        private short min;
        private short max;
        private boolean hasRange;

        public ShortParser()
        {
            super( "short", 11 );
        }

        public ShortParser(final short min, final short max)
        {
            this();
            this.min = min;
            this.max = max;
            this.hasRange = true;
        }

        @Override
        public ParserResult<Short> parse(final String in)
        {
            Short value = null;

            try
            {
                value = Short.parseShort( in );
            } catch ( final Exception ignored )
            {
            }

            if ( this.hasRange && value != null )
            {
                short b = value;
                b = (byte) Math.min( b, this.max );
                b = (byte) Math.max( b, this.min );
                if ( b != value )
                {
                    return new ParserResult<>( value + " is not within range (" + this.min + " -> " + this.max + ")" );
                }
            }

            return value != null ? new ParserResult<>( value ) : new ParserResult<>( in + " is not a short" );
        }
    }

    public static final class LongParser extends Parser<Long>
    {

        private long min;
        private long max;
        private boolean hasRange;

        public LongParser()
        {
            super( "long", Long.MAX_VALUE );
        }

        public LongParser(final long min, final long max)
        {
            this();
            this.min = min;
            this.max = max;
            this.hasRange = true;
        }

        @Override
        public ParserResult<Long> parse(final String in)
        {
            Long value = null;

            try
            {
                value = Long.parseLong( in );
            } catch ( final Exception ignored )
            {
            }

            if ( this.hasRange && value != null )
            {
                long b = value;
                b = (byte) Math.min( b, this.max );
                b = (byte) Math.max( b, this.min );
                if ( b != value )
                {
                    return new ParserResult<>( value + " is not within range (" + this.min + " -> " + ( this.max ==
                            Long.MAX_VALUE ? "infinity" : this.max ) + ")" );
                }
            }

            return value != null ? new ParserResult<>( value ) : new ParserResult<>( in + " is not a long" );
        }
    }

    public static final class FloatParser extends Parser<Float>
    {

        private float min;
        private float max;
        private boolean hasRange;

        public FloatParser()
        {
            super( "float", Float.MAX_VALUE );
        }

        public FloatParser(final float min, final float max)
        {
            this();
            this.min = min;
            this.max = max;
            this.hasRange = true;
        }

        @Override
        public ParserResult<Float> parse(final String in)
        {
            Float value = null;

            try
            {
                value = Float.parseFloat( in );
            } catch ( final Exception ignored )
            {
            }

            if ( this.hasRange && value != null )
            {
                float b = value;
                b = (byte) Math.min( b, this.max );
                b = (byte) Math.max( b, this.min );
                if ( b != value )
                {
                    return new ParserResult<>( value + " is not within range (" + this.min + " -> " + ( this.max ==
                            Float.MAX_VALUE ? "infinity" : this.max ) + ")" );
                }
            }

            return value != null ? new ParserResult<>( value ) : new ParserResult<>( in + " is not a float" );
        }
    }

    public static final class DoubleParser extends Parser<Double>
    {

        private double min;
        private double max;
        private boolean hasRange;

        public DoubleParser()
        {
            super( "double", Double.MAX_VALUE );
        }

        public DoubleParser(final double min, final double max)
        {
            this();
            this.min = min;
            this.max = max;
            this.hasRange = true;
        }

        @Override
        public ParserResult<Double> parse(final String in)
        {
            Double value = null;

            try
            {
                value = Double.parseDouble( in );
            } catch ( final Exception ignored )
            {
            }

            if ( this.hasRange && value != null )
            {
                double b = value;
                b = (byte) Math.min( b, this.max );
                b = (byte) Math.max( b, this.min );
                if ( b != value )
                {
                    return new ParserResult<>( value + " is not within range (" + this.min + " -> " + ( this.max ==
                            Double.MAX_VALUE ? "infinity" : this.max ) + ")" );
                }
            }

            return value != null ? new ParserResult<>( value ) : new ParserResult<>( in + " is not a double" );
        }
    }

}

/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility object for dealing with US-ASCII encoded strings. The strings are immutable, and can thus safely be reused in
 * multiple contexts.
 */
@SuppressWarnings("unused") public final class AsciiString
    implements CharSequence, AsciiStringable, Comparable<CharSequence> {

    private static final char[] charactersUpper =
        new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] charactersLower =
        new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final CacheEntry[] cachedStrings = new CacheEntry[128];
    private static final Object cachedStringsLock = new Object();
    private static final Map<String, AsciiString> map = new HashMap<>();
    private static final AsciiString HEX_ZERO_PREFIXED = of("0x0");
    private static final AsciiString HEX_ZERO = of("0");
    public static final AsciiString empty = of("");

    private final byte[] value;

    private byte lowercase = -1;
    private byte uppercase = -1;
    private int hashCode;
    private String cache;

    private AsciiString(final String value) {
        this(value.getBytes(StandardCharsets.US_ASCII));
    }

    private AsciiString(final byte[] bytes) {
        this.value = bytes;
    }

    public static AsciiString randomUUIDAsciiString() {
        return of(UUID.randomUUID().toString(), false);
    }

    /**
     * Convert a positive integer to a String representation of the
     * hexadecimal number
     *
     * @param number Positive integer
     * @return Hex string
     */
    public static AsciiString integerToHexString(final int number) {
        if (number <= 0) {
            return HEX_ZERO_PREFIXED;
        }
        final int leadingZeros = Integer.numberOfLeadingZeros(number) >>> 2;
        final byte[] chars = new byte[10 - leadingZeros];
        chars[0] = '0';
        chars[1] = 'x';
        for (int i = leadingZeros; i < 8; i++) {
            chars[9 - i] = (byte) charactersUpper[(number >>> ((i - leadingZeros) << 2)) & 0xF];
        }
        return of(chars);
    }

    /**
     * Convert a positive integer to a String representation of the
     * hexadecimal number, identical to the Strings produced by {@link Integer#toHexString(int)}
     *
     * @param number Positive integer
     * @return Hex string
     */
    public static AsciiString integerToHexStringWithoutPrefix(final int number) {
        if (number <= 0) {
            return HEX_ZERO;
        }
        synchronized (cachedStringsLock) {
            final CacheEntry cacheEntry = cachedStrings[number & 0x7F];
            if (cacheEntry != null && cacheEntry.tag == (number & 0x7FFFFF80)) {
                return cacheEntry.cachedString;
            }
        }
        final int leadingZeros = Integer.numberOfLeadingZeros(number) >>> 2;
        final byte[] chars = new byte[8 - leadingZeros];
        for (int i = leadingZeros; i < 8; i++) {
            chars[7 - i] = (byte) charactersLower[(number >>> ((i - leadingZeros) << 2)) & 0xF];
        }
        final AsciiString asciiString = of(chars);
        final CacheEntry cacheEntry = new CacheEntry(number & 0x7FFFFF80, asciiString);
        synchronized (cachedStringsLock) {
            cachedStrings[number & 0x7F] = cacheEntry;
        }
        return asciiString;
    }

    /**
     * Create a new (cached) ascii string or retrieve the object from the cache
     *
     * @param string String value
     * @return Created (or retrieved from cache) string
     */
    public static AsciiString of(final String string) {
        return of(string, true);
    }

    public static AsciiString of(final Number number) {
        return of(number.toString(), false);
    }

    public static AsciiString of(final String string, final boolean cache) {
        AsciiString asciiString = map.get(string);
        if (asciiString != null) {
            return asciiString;
        }
        asciiString = new AsciiString(string);
        if (cache) {
            map.put(string, asciiString);
        }
        return asciiString;
    }

    public static AsciiString of(final byte[] string) {
        return new AsciiString(string);
    }

    public boolean isEmpty() {
        return this.length() == 0;
    }

    private boolean isUppercase() {
        if (this.uppercase == -1) {
            for (final byte b : this.value) {
                if (b >= 97 && b <= 122) {
                    uppercase = 0;
                    return false;
                }
            }
            this.uppercase = 1;
        }
        return uppercase > 0;
    }

    private boolean isLowercase() {
        if (this.lowercase == -1) {
            for (final byte b : this.value) {
                if (b >= 65 && b <= 90) {
                    this.lowercase = 0;
                    return false;
                }
            }
            this.lowercase = 1;
        }
        return lowercase > 0;
    }

    /**
     * Get the byte array backing the string
     *
     * @return Bytes
     */
    public byte[] getValue() {
        return this.value;
    }

    @Override public int length() {
        return this.value.length;
    }

    @Override public char charAt(final int i) {
        return (char) this.value[i];
    }

    @Override public CharSequence subSequence(final int i, final int i1) {
        final byte[] bytes = new byte[i1 - i];
        System.arraycopy(this.value, i, bytes, 0, bytes.length);
        return of(bytes);
    }

    @Override public String toString() {
        return this.cache == null ?
            (this.cache = new String(this.value, StandardCharsets.US_ASCII)) :
            this.cache;
    }

    @Override public int hashCode() {
        if (this.hashCode == 0) {
            for (byte b : value) {
                this.hashCode = 31 * this.hashCode + b;
            }
        }
        return this.hashCode;
    }

    @Override public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof AsciiString) {
            final AsciiString other = (AsciiString) object;
            return compareBytes(other.value);
        } else if (object instanceof CharSequence) {
            final CharSequence charSequence = (CharSequence) object;
            if (charSequence.length() != this.length()) {
                return false;
            }
            for (int i = 0; i < length(); i++) {
                if (charAt(i) != charSequence.charAt(i)) {
                    return false;
                }
            }
            return true;
        } else if (object instanceof byte[]) {
            return compareBytes((byte[]) object);
        }
        return false;
    }

    private boolean compareBytes(final byte[] other) {
        if (this.value.length != other.length) {
            return false;
        }
        for (int i = 0; i < this.value.length; i++) {
            if (this.value[i] != other[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Delegate for {@link String#contains(CharSequence)}
     */
    public boolean contains(final CharSequence other) {
        return this.toString().contains(other);
    }

    public boolean equals(final AsciiString other) {
        return Arrays.equals(this.value, other.value);
    }

    @SuppressWarnings("ALL") public boolean equalsIgnoreCase(final CharSequence other) {
        if (this == other) {
            return true;
        } else if (isLowercase()) {
            return equals(other.toString().toLowerCase(Locale.ENGLISH));
        } else if (isUppercase()) {
            return equals(other.toString().toUpperCase(Locale.ENGLISH));
        } else {
            return toLowerCase().equals(other.toString().toLowerCase(Locale.ENGLISH));
        }
    }

    /**
     * Delegate for {@link String#endsWith(String)}
     */
    public boolean endsWith(final String string) {
        final char[] chars = string.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] != this.charAt(length() - chars.length + i)) {
                return false;
            }
        }
        return true;
    }

    public AsciiString toLowerCase() {
        if (this.isLowercase() || this.isEmpty()) {
            return this;
        }

        final byte[] lowercase = new byte[this.value.length];
        System.arraycopy(this.value, 0, lowercase, 0, this.value.length);

        for (int i = 0; i < this.value.length; i++) {
            byte character = this.value[i];
            if (character >= 65 && character <= 90) // uppercase A-Z
            {
                lowercase[i] = (byte) (character + 32);
            }
        }

        return of(lowercase);
    }

    public AsciiString toUpperCase() {
        if (this.isUppercase() || this.isEmpty()) {
            return this;
        }

        final byte[] uppercase = new byte[this.value.length];
        System.arraycopy(this.value, 0, uppercase, 0, this.value.length);

        for (int i = 0; i < this.value.length; i++) {
            byte character = this.value[i];
            if (character >= 97 && character <= 122) // lowercase a-z
            {
                uppercase[i] = (byte) (character - 32);
            }
        }

        return of(uppercase);
    }

    @Override public AsciiString toAsciiString() {
        return this;
    }

    /**
     * Get the integer value of the string. Does not verify whether the string contains non-integer characters or not.
     * Supports negative values.
     *
     * @return Parsed integer
     */
    public int toInteger() {
        return (int) toLong();
    }

    public boolean isInteger() {
        for (int i = 0; i < this.value.length; i++) {
            final byte b = this.value[i];

            if (b >= 48 && b <= 57) {
                continue;
            }

            if (i == 0 && b == 45) {
                continue;
            }

            return false;
        }
        return true;
    }

    /**
     * Get the long value of the string. Does not verify whether the string contains non-long characters or not.
     * Supports negative values.
     *
     * @return Parsed long
     */
    public long toLong() {
        int index = 0;
        long value = 0;
        boolean negative = this.value[0] == 45 /* - */;
        if (negative) {
            index = 1;
        }
        for (; index < this.value.length; index++) {
            value = (value * 10) + (this.value[index] - 48 /* 0 */);
        }
        if (negative) {
            return -value;
        }
        return value;
    }

    @Override public int compareTo(final CharSequence sequence) {
        if (this == sequence || this.equals(sequence)) {
            return 0;
        }
        return this.toString().compareTo(sequence.toString());
    }

    public List<AsciiString> split(final String delimiter) {
        return Arrays.stream(this.toString().split(delimiter))
            .map(string -> AsciiString.of(string, false)).collect(Collectors.toList());
    }

    public boolean startsWith(final AsciiString part) {
        final byte[] other = part.value;
        if (this.value.length < other.length) {
            return false;
        }
        for (int i = 0; i < other.length; i++) {
            if (this.value[i] != other[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean startsWith(final String part) {
        final char[] characters = part.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            if (characters[i] != this.value[i]) {
                return false;
            }
        }
        return true;
    }

    @RequiredArgsConstructor private static final class CacheEntry {

        private final int tag;
        private final AsciiString cachedString;

    }

}

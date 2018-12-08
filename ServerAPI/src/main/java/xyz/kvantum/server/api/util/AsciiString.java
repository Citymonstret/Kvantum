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

import com.google.common.base.Charsets;
import com.google.common.collect.HashBiMap;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility object for dealing with US-ASCII encoded strings. The strings are immutable, and can thus safely be reused in
 * multiple contexts.
 */
@SuppressWarnings("unused") public final class AsciiString
    implements CharSequence, AsciiStringable, Comparable<CharSequence> {

    private static final Map<String, AsciiString> map = HashBiMap.create();
    public static final AsciiString empty = of("");
    private final byte[] value;
    private final String string;
    private final boolean lowercase;
    private final boolean uppercase;
    private final int hashCode;

    private AsciiString(@NonNull final String value) {
        this(value, value.getBytes(Charsets.US_ASCII));
    }

    private AsciiString(@NonNull final byte[] value) {
        this(new String(value, StandardCharsets.US_ASCII), value);
    }

    private AsciiString(@NonNull final String string, @NonNull final byte[] bytes) {
        this.value = bytes;
        this.string = string;
        this.hashCode = this.string.hashCode();
        boolean lowercase = true;
        boolean uppercase = true;

        //
        // Pre-calculated values that determine
        // how the string behaves in case dependent
        // methods
        //
        for (final byte b : this.value) {
            if (b >= 97 && b <= 122) // lowercase a-z
            {
                uppercase = false;
            } else if (b >= 65 && b <= 90) // uppercase A-Z
            {
                lowercase = false;
            }
        }

        this.uppercase = uppercase;
        this.lowercase = lowercase;
    }

    public static AsciiString randomUUIDAsciiString() {
        return of(UUID.randomUUID().toString(), false);
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

    public static AsciiString of(@Nonnull @NonNull final Number number) {
        return of(number.toString(), false);
    }

    public static AsciiString of(@NonNull final String string, final boolean cache) {
        if (map.containsKey(string)) {
            return map.get(string);
        }
        final AsciiString asciiString = new AsciiString(string);
        if (cache) {
            map.put(string, asciiString);
        }
        return asciiString;
    }

    @Nonnull @Contract("_ -> new") public static AsciiString of(@NonNull final byte[] string) {
        return new AsciiString(string);
    }

    @Contract(pure = true) public boolean isEmpty() {
        return this.length() == 0;
    }

    /**
     * Get the byte array backing the string
     *
     * @return Bytes
     */
    @Contract(pure = true) public byte[] getValue() {
        return this.value;
    }

    @Contract(pure = true) @Override public int length() {
        return this.string.length();
    }

    @Contract(pure = true) @Override public char charAt(final int i) {
        return this.string.charAt(i);
    }

    @Nonnull @Contract(pure = true) @Override public CharSequence subSequence(final int i, final int i1) {
        return this.string.subSequence(i, i1);
    }

    @Contract(pure = true) @Override @SuppressWarnings("ALL") public String toString() {
        return this.string;
    }

    @Contract(pure = true) @Override public int hashCode() {
        return this.hashCode;
    }

    @Contract(value = "null -> false", pure = true) @Override public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof AsciiString) {
            final AsciiString other = (AsciiString) object;
            return compareBytes(other.value);
        } else if (object instanceof String) {
            return this.string.equals(object);
        } else if (object instanceof byte[]) {
            return compareBytes((byte[]) object);
        }
        return false;
    }

    @Contract(pure = true) private boolean compareBytes(@Nonnull @NonNull final byte[] other) {
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
    @Contract(pure = true) public boolean contains(@NonNull final CharSequence other) {
        return this.string.contains(other);
    }

    @SuppressWarnings("ALL") public boolean containsIgnoreCase(@NonNull final CharSequence other) {
        final String localString;
        final String otherString;
        if (lowercase) {
            localString = this.string;
            otherString = other.toString().toLowerCase(Locale.ENGLISH);
        } else if (uppercase) {
            localString = this.string;
            otherString = other.toString().toUpperCase(Locale.ENGLISH);
        } else {
            localString = this.string.toLowerCase(Locale.ENGLISH);
            otherString = other.toString().toLowerCase(Locale.ENGLISH);
        }
        return localString.contains(otherString);
    }

    @Contract(value = "null -> false", pure = true) public boolean equals(@NonNull final CharSequence other) {
        return this.string.equals(other.toString());
    }

    public boolean equals(@Nonnull @NonNull final AsciiString other) {
        return Arrays.equals(this.value, other.value);
    }

    @SuppressWarnings("ALL") public boolean equalsIgnoreCase(@NonNull final CharSequence other) {
        if (this == other) {
            return true;
        }
        final String localString;
        final String otherString;
        if (lowercase) {
            localString = this.string;
            otherString = other.toString().toLowerCase(Locale.ENGLISH);
        } else if (uppercase) {
            localString = this.string;
            otherString = other.toString().toUpperCase(Locale.ENGLISH);
        } else {
            localString = this.string.toLowerCase(Locale.ENGLISH);
            otherString = other.toString().toLowerCase(Locale.ENGLISH);
        }
        return localString.equals(otherString);
    }

    /**
     * Delegate for {@link String#endsWith(String)}
     */
    @Contract(pure = true) @SuppressWarnings("WeakerAccess") public boolean endsWith(@NonNull final String string) {
        return this.string.endsWith(string);
    }

    public AsciiString toLowerCase() {
        if (this.lowercase || this.isEmpty()) {
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

    @SuppressWarnings("WeakerAccess") public AsciiString toUpperCase() {
        if (this.uppercase) {
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

    @Contract(value = " -> this", pure = true) @Override public AsciiString toAsciiString() {
        return this;
    }

    /**
     * Get the integer value of the string. Does not verify whether the string contains non-integer characters or not.
     * Supports negative values.
     *
     * @return Parsed integer
     */
    @Contract(pure = true) public int toInteger() {
        int index = 0;
        int value = 0;
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

    @Contract(pure = true) public boolean isInteger() {
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
    @Contract(pure = true) public long toLong() {
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

    @Override @SuppressWarnings("ALL") public int compareTo(@NonNull final CharSequence sequence) {
        if (this == sequence || this.equals(sequence)) {
            return 0;
        }
        return this.string.compareTo(sequence.toString());
    }

    public List<AsciiString> split(@NonNull final String delimiter) {
        return Arrays.stream(this.string.split(delimiter))
            .map(string -> AsciiString.of(string, false)).collect(Collectors.toList());
    }

    @Contract(pure = true) public boolean startsWith(@Nonnull @NonNull final AsciiString part) {
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

    @Contract(pure = true) public boolean startsWith(@NonNull final String part) {
        return this.string.startsWith(part);
    }
}

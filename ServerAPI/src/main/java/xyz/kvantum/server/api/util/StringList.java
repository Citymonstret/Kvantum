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

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Represents a list of strings, that is stored inside of a string, with the
 * format: string1,string2,string3,string4...
 * {@inheritDoc}
 */
@SuppressWarnings("unused") public final class StringList
    implements Collection<String>, AsciiStringable {

    private final Collection<String> content;

    /**
     * Initialize a new string list
     *
     * @param string Initial content
     */
    public StringList(@Nullable final String string) {
        this.content = new ArrayList<>();
        if (!this.addAll(string)) {
            throw new IllegalArgumentException("Failed to add all elements provided to the list");
        }
    }

    /**
     * Remove an item from the list
     *
     * @param string Item to be removed
     * @return True if the item was removed, else false
     */
    public boolean remove(@NonNull final String string) {
        return this.content.remove(string);
    }

    @SuppressWarnings("WeakerAccess") public boolean addAll(@Nullable final String string) {
        if (string != null && !string.isEmpty()) {
            final StringTokenizer tokenizer = new StringTokenizer(string, ",");
            while (tokenizer.hasMoreTokens()) {
                final String element = tokenizer.nextToken();
                if (element == null || element.isEmpty()) {
                    continue;
                }
                if (!this.content.add(element)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Add an item to the list
     *
     * @param string Item to be added
     * @return True if the item was added, else false
     */
    public boolean add(@NonNull final String string) {
        return this.content.add(string);
    }

    @Override public boolean remove(@Nonnull @NonNull final Object o) {
        return o instanceof String && this.remove((String) o);
    }

    @Override @SuppressWarnings("ALL")
    public boolean containsAll(@NonNull final Collection<?> collection) {
        return this.content.containsAll(collection);
    }

    @Override @SuppressWarnings("ALL")
    public boolean addAll(@NonNull final Collection<? extends String> collection) {
        return this.content.addAll(collection);
    }

    @Override @SuppressWarnings("ALL")
    public boolean removeAll(@NonNull final Collection<?> collection) {
        return this.content.removeAll(collection);
    }

    @Override @SuppressWarnings("ALL")
    public boolean retainAll(@NonNull final Collection<?> collection) {
        return this.content.retainAll(collection);
    }

    @Override public void clear() {
        this.content.clear();
    }

    /**
     * Check if the list contains an item
     *
     * @param string Item
     * @return True if the list contains the item, else false
     */
    public boolean contains(@Nonnull @NonNull final String string) {
        return this.content.contains(string);
    }

    /**
     * Convert the list to a string, joining with ","
     *
     * @return Joined list
     */
    @Nonnull @Override public String toString() {
        return CollectionUtil.join(this.content, ",");
    }

    @Override public AsciiString toAsciiString() {
        return AsciiString.of(this.toString(), false);
    }

    @Override public int size() {
        return this.content.size();
    }

    @Override public boolean isEmpty() {
        return this.content.isEmpty();
    }

    @Override public boolean contains(@Nonnull @NonNull final Object o) {
        return o instanceof String && this.contains((String) o);
    }

    /**
     * Provide the item iterator. Delegate for {@link ArrayList#iterator()}
     *
     * @return list iterator
     */
    @Nonnull @Override public Iterator<String> iterator() {
        return this.content.iterator();
    }

    @Nonnull @Override public Object[] toArray() {
        return this.content.toArray();
    }

    @Nonnull @Override @SuppressWarnings("ALL")
    public <T> T[] toArray(@Nonnull @NonNull final T[] ts) {
        return this.content.toArray(ts);
    }

}

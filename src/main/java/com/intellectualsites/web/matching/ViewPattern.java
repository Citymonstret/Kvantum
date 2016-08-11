package com.intellectualsites.web.matching;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;

@SuppressWarnings("unused")
public class ViewPattern {

    private final List<Part> parts;
    private final String raw;

    public ViewPattern(@NonNull final String in) {
        this.raw = in;

        final SmartString string = new SmartString(raw.toLowerCase()); // TODO: Decide if casing is important

        string.replaceLastIf('/', SmartString.nil);

        parts = new ArrayList<>();

        boolean openOptional = false;
        boolean openRequired = false;
        boolean first = true;

        String name = "";
        for (char c : string) {
            if (c == '<') {
                if (!name.isEmpty()) {
                    parts.add(new Static(name));
                }
                openRequired = true;
                name = "";
            } else if (c == '[') {
                if (!name.isEmpty()) {
                    parts.add(new Static(name));
                }
                openOptional = true;
                name = "";
            } else if (openRequired && c == '>') {
                openRequired = false;
                parts.add(new Variable(name, Variable.TYPE_REQUIRED));
            } else if (openOptional && c == ']') {
                openOptional = false;
                parts.add(new Variable(name, Variable.TYPE_OPTIONAL));
            } else if (c == '/') {
                if (!name.isEmpty()) {
                    parts.add(new Static(name));
                    name = "";
                }
                parts.add(new Split());
            } else {
                name += c;
            }
        }

    }

    public Map<String, String> matches(@NonNull final String in) {
        final SmartString url;
        if (in.contains("?")) {
            url = new SmartString(in.split("\\?")[0]);
        } else {
            url = new SmartString(in);
        }

        url.replaceIf(0, '/', SmartString.nil);
        url.replaceLastIf('/', SmartString.nil);

        if (parts.isEmpty()) {
            if (url.toString().isEmpty()) {
                return new HashMap<>();
            } else {
                return null;
            }
        }

        final String[] p = url.toString().split("((?<=/)|(?=/))");
        final List<String> finalList = new ArrayList<>();
        for (final String l : p) {
            if (l.contains(".")) {
                final String[] k = l.split("(?=\\.)");
                finalList.add(k[0]);
                finalList.add(k[1]);
            } else {
                finalList.add(l);
            }
        }

        final Map<String, String> variables = new HashMap<>();
        final Iterator<String> stringIterator = finalList.iterator();

        Part lastPart = null;

        for (final Part part : parts) {

            boolean has = stringIterator.hasNext();
            String next = has ? stringIterator.next() : "";

            if (part instanceof Variable) {
                Variable v = (Variable) part;
                if (v.getType() == Variable.TYPE_REQUIRED) {
                    if (!has) {
                        return null;
                    }
                }
            } else if (part instanceof Static) {
                if (!has) {
                    return null;
                } else {
                    if (!next.equalsIgnoreCase(part.toString())) {
                        return null;
                    }
                }
            }

            if (part instanceof Variable) {
                final Variable variable = (Variable) part;
                variables.put(variable.getName(), next);
            }
        }
        if (stringIterator.hasNext()) {
            return null;
        }
        return variables;
    }

    @Override
    public String toString() {
        return this.raw;
    }

    private abstract static class Part {

        @Override
        public abstract String toString();

    }

    @RequiredArgsConstructor
    private static class Static extends Part {

        @NonNull
        private final String string;

        @Override
        public String toString() {
            return string;
        }
    }

    @SuppressWarnings("unused")
    private static class SmartString implements Iterable<Character> {

        private static final char nil = 'Æ';

        private char[] chars;
        private int length;

        private boolean changed = false;

        private SmartString(@NonNull final String in) {
            this.chars = in.toCharArray();
            this.length = in.length();
        }

        char lastCharacter() {
            return chars[length - 1];
        }

        void replaceLast(char c) {
            set(length - 1, c);
        }

        void replaceAll(char c, char w) {
            int[] indices = findAll(c);
            for (int i : indices) {
                set(i, w);
            }
        }

        void replaceLastIf(char c, char k) {
            replaceIf(length - 1, c, k);
        }

        void replaceIf(int n, char c, char k) {
            if (length == 0) {
                return;
            }
            if (chars[n] == c) {
                set(n, k);
            }
        }

        int[] findAll(char c) {
            int[] indices = new int[length];
            int written = 0;
            for (int i = 0; i < length; i++) {
                if (chars[i] == c) {
                    indices[written++] = i;
                }
            }
            int[] n = new int[written];
            System.arraycopy(indices, 0, n, 0, n.length);
            return n;
        }

        void set(final int i, final char c) {
            if (length == 0) {
                return;
            }
            chars[i] = c;
            changed = true;
        }

        void remove(final int i) {
            set(i, nil);
        }

        void regenerate() {
            char[] temp = new char[length];
            int index = 0;
            for (char c : chars) {
                if (c != nil) {
                    temp[index++] = c;
                }
            }
            chars = new char[index];
            System.arraycopy(temp, 0, chars, 0, index);
            length = chars.length;
            changed = false;
        }

        @Override
        public String toString() {
            if (changed) {
                regenerate();
            }
            return new String(chars);
        }

        @Override
        public Iterator<Character> iterator() {
            return new Iterator<Character>() {

                {
                    SmartString.this.regenerate();
                }

                int index = 0;

                @Override
                public boolean hasNext() {
                    return index < length;
                }

                @Override
                public Character next() {
                    return chars[index++];
                }
            };
        }
    }

    private static final class Split extends Part {

        @Override
        public String toString() {
            return "/";
        }
    }

    @RequiredArgsConstructor
    private static class Variable extends Part {

        private static int TYPE_REQUIRED = 0, TYPE_OPTIONAL = 1;

        @Getter
        private final String name;

        @Getter
        private final int type;

        @Override
        public String toString() {
            return this.name + (type == TYPE_REQUIRED ? "" : "?");
        }
    }

}

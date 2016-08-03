package com.intellectualsites.web.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.Map;

@UtilityClass
public class StringUtil {

    public static <K,V> String join(@NonNull final Map<K, V> map, @NonNull final String combiner, @NonNull final String separator) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<Map.Entry<K,V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<K,V> entry = iterator.next();
            builder.append(entry.getKey()).append(combiner).append(entry.getValue());
            if (iterator.hasNext()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

}

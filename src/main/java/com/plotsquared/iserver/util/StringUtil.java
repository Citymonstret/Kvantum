package com.plotsquared.iserver.util;

import java.util.Iterator;
import java.util.Map;

public final class StringUtil {

    public static <K, V> String join(final Map<K, V> map, final String combiner, final String separator) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<K, V> entry = iterator.next();
            builder.append(entry.getKey()).append(combiner).append(entry.getValue());
            if (iterator.hasNext()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

}

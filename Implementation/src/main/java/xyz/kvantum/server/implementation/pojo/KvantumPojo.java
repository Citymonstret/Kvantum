package xyz.kvantum.server.implementation.pojo;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;

import java.util.Map;

/**
 * Representation of a POJO instance constructed
 * by a {@link KvantumPojoFactory} instance
 */
@SuppressWarnings({ "unused", "WeakerAccess" })
@RequiredArgsConstructor
public final class KvantumPojo
{

    private final Map<String, Object> fieldValues;

    /**
     * Get a specified value. Will throw exceptions
     * if no such key is stored.
     *
     * @param key Key
     * @return Object
     * @see #containsKey(String) To check if a key is stored
     */
    public final Object get(final String key)
    {
        return fieldValues.get( key );
    }

    /**
     * Check if this instance contains a given key
     *
     * @param key Key to check for
     * @return True if the key exists
     */
    public final boolean containsKey(final String key)
    {
        return fieldValues.containsKey( key );
    }

    /**
     * Check if this instance contains a given value
     *
     * @param value Value to check for
     * @return True if the value exists
     */
    public final boolean containsValue(final Object value)
    {
        return fieldValues.containsValue( value );
    }

    /**
     * Get the values for all getters in the POJO
     *
     * @return Immutable map with all values
     */
    public final Map<String, Object> getAll()
    {
        return fieldValues;
    }

    /**
     * Construct a {@link JSONObject} from this instance
     *
     * @return JSON object
     */
    public JSONObject toJson()
    {
        return new JSONObject( fieldValues );
    }
}

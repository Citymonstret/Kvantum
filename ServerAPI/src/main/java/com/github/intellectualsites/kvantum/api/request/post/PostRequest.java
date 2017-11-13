package com.github.intellectualsites.kvantum.api.request.post;

import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.request.RequestChild;
import com.github.intellectualsites.kvantum.api.util.Assert;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public abstract class PostRequest implements RequestChild
{

    /*
    TODO: Support more POST request body types, such as:
    - JSON
     */

    @Getter
    private final Request parent;
    @Getter(AccessLevel.PROTECTED)
    private final String rawRequest;
    @Getter
    private final Map<String, String> variables;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String request;
    private boolean loaded;

    protected PostRequest(final Request parent, final String rawRequest, boolean lazyLoad)
    {
        Assert.notNull( parent );
        Assert.notNull( rawRequest );

        this.loaded = false;
        this.parent = parent;
        this.rawRequest = rawRequest;
        this.request = rawRequest; // Can be overridden
        this.variables = new HashMap<>();
        if ( !lazyLoad )
        {
            this.load();
        }
    }

    private void load()
    {
        this.parseRequest( rawRequest );
        this.loaded = true;
    }

    private void checkIfShouldLoad()
    {
        if ( this.loaded )
        {
            return;
        }
        this.load();
    }

    protected abstract void parseRequest(String rawRequest);

    public abstract EntityType getEntityType();

    /**
     * Get a parameter
     *
     * @param key Parameter key
     * @return Parameter value if found, else null
     */
    public String get(final String key)
    {
        Assert.notNull( key );

        this.checkIfShouldLoad();

        if ( !this.getVariables().containsKey( key ) )
        {
            return null;
        }
        return this.getVariables().get( key );
    }

    /**
     * Check if a parameter is stored
     *
     * @param key Parameter key
     * @return True of the parameter is stored; else false
     */
    public boolean contains(final String key)
    {
        Assert.notNull( key );

        this.checkIfShouldLoad();

        return this.getVariables().containsKey( key );
    }

    /**
     * Get a copy of the internal parameter map
     *
     * @return copy of the internal map
     */
    final public Map<String, String> get()
    {
        this.checkIfShouldLoad();

        return new HashMap<>( this.variables );
    }

}
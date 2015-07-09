package com.intellectualsites.web.object.cache;

import com.intellectualsites.web.object.Request;

public interface CacheApplicable {

    boolean isApplicable(Request r);

}

package com.github.intellectualsites.iserver.api.cache;

import com.github.intellectualsites.iserver.api.account.IAccount;
import com.github.intellectualsites.iserver.api.response.ResponseBody;
import com.github.intellectualsites.iserver.api.views.RequestHandler;

import java.util.Optional;

public interface ICacheManager
{

    String getCachedInclude(String group);

    Optional<IAccount> getCachedAccount(int id);

    Optional<Integer> getCachedId(String username);

    void setCachedAccount(IAccount account);

    Optional<String> getCachedFile(String file);

    void setCachedFile(String file, String content);

    void setCachedInclude(String group, String document);

    boolean hasCache(RequestHandler view);

    void setCache(RequestHandler view, ResponseBody body);

    CachedResponse getCache(RequestHandler view);

}

package com.github.intellectualsites.iserver.api.account;

import java.util.Map;
import java.util.Optional;

public interface IAccount
{

    Map<String, String> getRawData();

    void internalMetaUpdate(String key, String value);

    boolean passwordMatches(String password);

    Optional<String> getData(String key);

    void setData(String key, String value);

    void removeData(String key);

    int getId();

    String getUsername();

    String getPassword();

    void setManager(IAccountManager manager);

}

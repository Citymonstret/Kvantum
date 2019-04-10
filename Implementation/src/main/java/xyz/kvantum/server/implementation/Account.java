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
package xyz.kvantum.server.implementation;

import lombok.*;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import xyz.kvantum.server.api.account.AccountExtension;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.account.roles.AccountRole;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.orm.KvantumObjectFactory;
import xyz.kvantum.server.api.orm.annotations.KvantumConstructor;
import xyz.kvantum.server.api.orm.annotations.KvantumField;
import xyz.kvantum.server.api.orm.annotations.KvantumInsert;
import xyz.kvantum.server.api.orm.annotations.KvantumObject;
import xyz.kvantum.server.api.pojo.Ignore;
import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.AutoCloseable;
import xyz.kvantum.server.api.util.StringList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link IAccount} that also fully supports {@link KvantumObjectFactory} and {@link
 * KvantumPojoFactory}
 *
 * @see #getKvantumPojoFactory()
 * @see #getKvantumAccountFactory()
 */
@KvantumObject(checkValidity = true) @EqualsAndHashCode(of = {"username", "id"}) @NoArgsConstructor
@Entity("accounts") @SuppressWarnings("WeakerAccess") public final class Account
    extends AutoCloseable implements IAccount {

    @Getter private static final KvantumObjectFactory<Account> kvantumAccountFactory =
        KvantumObjectFactory.from(Account.class);
    @Getter private static final KvantumPojoFactory<IAccount> kvantumPojoFactory =
        KvantumPojoFactory.forClass(IAccount.class);

    private static final String KEY_ROLE_LIST = "internalRoleList";
    private final Map<Class<? extends AccountExtension>, AccountExtension> extensions =
        new ConcurrentHashMap<>();
    @Min(-1) @KvantumField @Id @Getter private int id;
    @NotEmpty @KvantumField @Getter @NonNull private String username;
    @KvantumField @NonNull private String password;
    @NonNull private Map<String, String> data;
    @Setter @Transient private transient IAccountManager manager;
    private StringList rawRoleList;
    private Collection<AccountRole> roleList;

    public Account(final int id, final String username, final String password,
        final Map<String, String> data) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.data = data;
    }

    @KvantumConstructor
    public Account(@KvantumInsert(value = "id", defaultValue = "-1") final int userID,
        @KvantumInsert(value = "username") final String username,
        @KvantumInsert("password") final String password) {
        this(userID, username, password, getDefaultDataSet());
    }

    private static Map<String, String> getDefaultDataSet() {
        final Map<String, String> map = new ConcurrentHashMap<>();
        map.put("created", "true");
        return map;
    }

    @Override @Ignore public Map<String, String> getRawData() {
        return new HashMap<>(data);
    }

    @Override public void internalMetaUpdate(final String key, final String value) {
        this.data.put(key, value);
    }

    @Override public boolean passwordMatches(final String password) {
        return IAccountManager.checkPassword(password, this.password);
    }

    @Override public Optional<String> getData(final String key) {
        Assert.notEmpty(key);

        return Optional.ofNullable(data.get(key));
    }

    @Override public void setData(final String key, final String value) {
        Assert.notEmpty(key);
        Assert.notEmpty(value);

        if (data.containsKey(key)) {
            removeData(key);
        }
        this.data.put(key, value);
        this.manager.setData(this, key, value);
    }

    @Override public void removeData(final String key) {
        Assert.notEmpty(key);

        if (!data.containsKey(key)) {
            return;
        }
        this.data.remove(key);
        this.manager.removeData(this, key);
    }

    @Override @Ignore public Collection<AccountRole> getAccountRoles() {
        if (this.roleList == null) {
            this.roleList = new HashSet<>();
            this.rawRoleList = new StringList(getData(KEY_ROLE_LIST).orElse(""));
            for (final String string : rawRoleList) {
                final Optional<AccountRole> roleOptional = manager.getAccountRole(string);
                if (roleOptional.isPresent()) {
                    this.roleList.add(roleOptional.get());
                } else {
                    Logger.warn("Account [{}] has account role [{}] stored,"
                            + " but the role is not registered in  the account manager", getUsername(),
                        string);
                }
            }
        }
        return this.roleList;
    }

    @Override public void addRole(@NonNull final AccountRole role) {
        if (this.roleList == null) {
            this.getAccountRoles();
        }
        if (this.roleList.contains(role)) {
            return;
        }
        this.roleList.add(role);
        this.rawRoleList.add(role.getRoleIdentifier());
        this.setData(KEY_ROLE_LIST, rawRoleList.toString());
    }

    @Override public void removeRole(@NonNull final AccountRole role) {
        if (this.roleList == null) {
            this.getAccountRoles();
        }
        if (this.roleList.contains(role)) {
            this.roleList.remove(role);
            this.rawRoleList.remove(role.getRoleIdentifier());
            this.setData(KEY_ROLE_LIST, rawRoleList.toString());
        }
    }

    @Override
    public <T extends AccountExtension> T attachExtension(@NonNull final Class<T> extension) {
        if (this.getExtension(extension).isPresent()) {
            throw new IllegalArgumentException("Cannot attach an extension twice");
        }
        try {
            final T instance = AccountExtension.createInstance(extension);
            instance.attach(this);
            this.extensions.put(extension, instance);
            return instance;
        } catch (final Exception e) {
            return null;
        }
    }

    @SuppressWarnings("ALL") @Override public <T extends AccountExtension> Optional<T> getExtension(
        @NonNull final Class<T> extension) {
        final Object extensionInstance = this.extensions.get(extension);
        if (extensionInstance == null) {
            return Optional.empty();
        }
        return Optional.of((T) extensionInstance);
    }

    @Override public String getSuppliedPassword() {
        return this.password;
    }

    @Override public void saveState() {
        for (final AccountExtension extension : this.extensions.values()) {
            extension.saveState();
        }
    }

    @Override public boolean isPermitted(@NonNull final String permissionKey) {
        if (this.roleList == null) {
            this.getAccountRoles();
        }
        //
        // Loop through every role, and return on first match.
        // We do not cache the results, as roles are hot-swappable
        //
        for (final AccountRole role : this.getAccountRoles()) {
            if (role.hasPermission(permissionKey)) {
                return true;
            }
        }
        return false;
    }

    @Override public KvantumPojo<IAccount> toKvantumPojo() {
        return kvantumPojoFactory.of(this);
    }

    @Override protected void handleClose() {
        this.saveState();
    }
}

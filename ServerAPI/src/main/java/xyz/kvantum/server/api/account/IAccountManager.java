/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
package xyz.kvantum.server.api.account;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import org.mindrot.jbcrypt.BCrypt;
import xyz.kvantum.server.api.account.roles.AccountRole;
import xyz.kvantum.server.api.account.roles.defaults.Administrator;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.repository.KvantumRepository;
import xyz.kvantum.server.api.repository.Matcher;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.util.ApplicationStructure;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages {@link IAccount} and depends on {@link ApplicationStructure} <p> The global implementation can be retrieved
 * using {@link Kvantum#getApplicationStructure()} then {@link ApplicationStructure#getAccountManager()} </p>
 */
@SuppressWarnings("unused") public interface IAccountManager
    extends KvantumRepository<IAccount, Integer> {

    String SESSION_ACCOUNT_CONSTANT = "__user_id__";
    Map<String, AccountRole> ROLE_MAP = new ConcurrentHashMap<>();

    /**
     * Check if a given password matches the real password
     *
     * @param candidate Candidate password
     * @param password  Real password
     * @return true if the passwords are matching
     */
    static boolean checkPassword(final String candidate, final String password) {
        return BCrypt.checkpw(candidate, password);
    }

    /**
     * Add an account decorator to the manager
     *
     * @param decorator Decorator to add
     */
    void addAccountDecorator(AccountDecorator decorator);

    /**
     * Get the container {@link ApplicationStructure}
     *
     * @return {@link ApplicationStructure} implementation
     */
    ApplicationStructure getApplicationStructure();

    /**
     * Setup the account manager
     *
     * @throws Exception if anything goes wrong
     */
    void setup() throws Exception;

    /**
     * Create an {@link IAccount}
     *
     * @param username Account username
     * @param password Account password
     * @return {@link Optional} containing the account if it was created successfully, otherwise an empty optional
     * ({@link Optional#empty()} is returned.
     */
    Optional<IAccount> createAccount(String username, String password);

    /**
     * Create an account from a placeholder {@link IAccount}
     *
     * @param temporary Placeholder data object
     * @return Created account
     */
    Optional<IAccount> createAccount(IAccount temporary);

    /**
     * Get an {@link IAccount} by username.
     *
     * @param username Account username
     * @return {@link Optional} containing the account if it exsists otherwise an empty optional ({@link
     * Optional#empty()} is returned.
     */
    Optional<IAccount> getAccount(String username);

    /**
     * Get an {@link IAccount} by ID.
     *
     * @param accountId Account ID
     * @return {@link Optional} containing the account if it exsists otherwise an empty optional ({@link
     * Optional#empty()} is returned.
     */
    Optional<IAccount> getAccount(int accountId);

    /**
     * Get an {@link IAccount} by session.
     *
     * @param session session.
     * @return {@link Optional} containing the account if it exsists otherwise an empty optional ({@link
     * Optional#empty()} is returned.
     */
    default Optional<IAccount> getAccount(final ISession session) {
        if (!session.contains(SESSION_ACCOUNT_CONSTANT)) {
            return Optional.empty();
        }
        return getAccount((int) session.get(SESSION_ACCOUNT_CONSTANT));
    }

    /**
     * Bind an {@link IAccount} to a {@link ISession}
     *
     * @param account Account
     * @param session Session
     */
    default void bindAccount(final IAccount account, final ISession session) {
        session.set(SESSION_ACCOUNT_CONSTANT, account.getId());
    }

    /**
     * Unbind any account from a {@link ISession}. Saves the account state beforehand.
     *
     * @param session Session to be unbound
     */
    default void unbindAccount(final ISession session) {
        final Object object = session.get(SESSION_ACCOUNT_CONSTANT);
        if (object instanceof IAccount) {
            ((IAccount) object).saveState();
        }
        session.set(SESSION_ACCOUNT_CONSTANT, null);
    }

    /**
     * Set the data for an account
     *
     * @param account Account
     * @param key     Data key
     * @param value   Data value
     */
    void setData(IAccount account, String key, String value);

    /**
     * Remove a data value from an account
     *
     * @param account Account
     * @param key     Data key
     */
    void removeData(IAccount account, String key);

    /**
     * Load the data into a {@link IAccount}
     *
     * @param account Account to be loaded
     */
    void loadData(IAccount account);

    /**
     * Delete an account from the database
     *
     * @param account Account to be deleted
     */
    void deleteAccount(IAccount account);

    /**
     * Check if the admin account is created, otherwise a new admin account will be created with credentials: <ul>
     * <li><b>Username:</b> admin</li> <li><b>Password:</b> admin</li> </ul>
     */
    default void checkAdmin() {
        final Optional<AccountRole> administratorAccountRole =
            getAccountRole(Administrator.ADMIN_IDENTIFIER);
        if (!administratorAccountRole.isPresent()) {
            registerAccountRole(Administrator.instance);
        }
        if (!getAccount("admin").isPresent()) {
            Optional<IAccount> adminAccount = createAccount("admin", "admin");
            if (!adminAccount.isPresent()) {
                Message.ACCOUNT_ADMIN_FAILED.log();
            } else {
                Message.ACCOUNT_ADMIN_CREATED.log("admin");
                adminAccount.get().setData("administrator", "true");
                adminAccount.get().addRole(Administrator.instance);
            }
        }
    }

    /**
     * Register an account role
     *
     * @param role Role instance
     */
    default void registerAccountRole(@NonNull final AccountRole role) {
        this.ROLE_MAP.put(role.getRoleIdentifier(), role);
    }

    /**
     * Get all account roles that are registered in the manager
     *
     * @return registered account roles
     */
    default Collection<AccountRole> getRegisteredAccountRoles() {
        return ImmutableList.copyOf(this.ROLE_MAP.values());
    }

    /**
     * Try to retrieve an account role based on its identifier
     *
     * @param roleIdentifier Role identifier ({@link AccountRole#getRoleIdentifier()})
     * @return Optional
     */
    default Optional<AccountRole> getAccountRole(@NonNull final String roleIdentifier) {
        if (this.ROLE_MAP.containsKey(roleIdentifier)) {
            return Optional.of(this.ROLE_MAP.get(roleIdentifier));
        }
        return Optional.empty();
    }

    @Override default ImmutableList<? extends IAccount> findAllById(
        @NonNull final Collection<Integer> collection) {
        final ImmutableList.Builder<IAccount> builder = ImmutableList.builder();
        collection.stream().map(this::getAccount).filter(Optional::isPresent).map(Optional::get)
            .forEach(builder::add);
        return builder.build();
    }

    @Override default ImmutableList<? extends IAccount> findAllByQuery(
        @NonNull final Matcher<?, ? super IAccount> matcher) {
        final ImmutableList.Builder<IAccount> builder = ImmutableList.builder();
        findAll().stream().filter(matcher::matches).forEach(builder::add);
        return builder.build();
    }

    @Override default ImmutableCollection<? extends IAccount> save(
        @NonNull final Collection<? extends IAccount> collection) {
        final ImmutableList.Builder<IAccount> builder =
            ImmutableList.builderWithExpectedSize(collection.size());
        collection.stream().map(this::createAccount).filter(Optional::isPresent).map(Optional::get)
            .forEach(builder::add);
        return builder.build();
    }

    @Override default void delete(@NonNull final Collection<IAccount> collection) {
        collection.forEach(this::deleteAccount);
    }

    @Override default Optional<? extends IAccount> findSingle(@NonNull final Integer identifier) {
        return this.getAccount(identifier);
    }

    @Override
    default ImmutableCollection<? extends IAccount> findAll(@NonNull final Integer identifier) {
        final Optional<? extends IAccount> optional = findSingle(identifier);
        return optional.<ImmutableCollection<? extends IAccount>>map(ImmutableList::of)
            .orElseGet(ImmutableList::of);
    }
}

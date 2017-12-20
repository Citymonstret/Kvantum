/*
 *    Copyright (C) 2017 IntellectualSites
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
package xyz.kvantum.server.api.account.verification;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.verification.PredicatedRule;
import xyz.kvantum.server.api.verification.Rule;
import xyz.kvantum.server.api.verification.Verifier;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

@SuppressWarnings({ "unused", "WeakerAccess" })
public class AccountVerifier
{

    public static final Pattern PATTERN_EMAIL = Pattern.compile(
            "\\A[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@" +
                    "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\z" );
    public static final Rule<IAccount> PASSWORD_MIN_CHARACTERS = PredicatedRule
            .create( "Password must be at least 8 characters long", account ->
                    account.getSuppliedPassword().length() >= 8 );
    public static final Rule<IAccount> PASSWORD_MAX_CHARACTERS = PredicatedRule
            .create( "Password cannot be more than 16 characters long", account ->
                    account.getSuppliedPassword().length() <= 16 );
    public static final Rule<IAccount> PASSWORD_NOT_USERNAME = PredicatedRule
            .create( "Password cannot be the same as the username", account ->
                    !account.getUsername().equalsIgnoreCase( account.getSuppliedPassword() ) );
    public static final Rule<IAccount> USERNAME_MIN_5_CHARACTERS = PredicatedRule
            .create( "Username must be at least 5 characters long", account ->
                    account.getUsername().length() >= 5 );
    public static final Rule<IAccount> EMAIL_PATTERN = PredicatedRule
            .create( "Email must follow valid email " + "syntax", account ->
            {
                final Optional<String> email = account.getData( "email" );
                //
                // Assume email is valid, if
                // it isn't supplied at all
                //
                return email.map( s -> PATTERN_EMAIL.matcher( s ).matches() ).orElse( true );
            } );
    @NonNull
    @Setter
    @Getter
    private static AccountVerifier globalAccountVerifier = new AccountVerifier();

    @Setter
    @Getter
    @NonNull
    private Verifier<IAccount> internalVerifier;

    protected AccountVerifier()
    {
        final Verifier.VerifierBuilder<IAccount> builder = Verifier.builder();
        builder.nullable( false )
                .withRule( PASSWORD_MIN_CHARACTERS )
                .withRule( PASSWORD_MAX_CHARACTERS )
                .withRule( PASSWORD_NOT_USERNAME )
                .withRule( USERNAME_MIN_5_CHARACTERS )
                .withRule( EMAIL_PATTERN );
        this.internalVerifier = builder.build();
    }

    public final boolean isValid(final IAccount account)
    {
        return this.verifyAccount( account ).isEmpty();
    }

    public final Collection<Rule<IAccount>> verifyAccount(final IAccount account)
    {
        return this.internalVerifier.verify( account );
    }

}

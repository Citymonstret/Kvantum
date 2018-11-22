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
package xyz.kvantum.server.api.account.verification;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.verification.PredicatedRule;
import xyz.kvantum.server.api.verification.Rule;
import xyz.kvantum.server.api.verification.Verifier;

/**
 * Utility object that aims to provide an easy, and uniform, way to verify that account information follows a set
 * standard. This system is used in the account command registration system. <p> There is a default implementation
 * setup, that can be replaced. This implementation can be retrieved by using {@link #getGlobalAccountVerifier()} and
 * set using {@link #setGlobalAccountVerifier(AccountVerifier)}. The default implementation checks the following: <ul>
 * <li>Email follows email syntax (If email is given)</li> <li>Password has at least 8 characters</li> <li>Password is
 * no more than 16 characters</li> <li>Password is not the same as the username</li> <li>Username has at least 5
 * characters</li> </ul>
 */
@SuppressWarnings({ "unused", "WeakerAccess" }) public class AccountVerifier
{

	public static final Pattern PATTERN_EMAIL = Pattern.compile(
			"\\A[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
					+ "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\z" );
	public static final Rule<IAccount> PASSWORD_MIN_CHARACTERS = PredicatedRule
			.create( "Password must be at least 8 characters long",
					account -> account.getSuppliedPassword().length() >= 8 );
	public static final Rule<IAccount> PASSWORD_MAX_CHARACTERS = PredicatedRule
			.create( "Password cannot be more than 16 characters long",
					account -> account.getSuppliedPassword().length() <= 16 );
	public static final Rule<IAccount> PASSWORD_NOT_USERNAME = PredicatedRule
			.create( "Password cannot be the same as the username",
					account -> !account.getUsername().equalsIgnoreCase( account.getSuppliedPassword() ) );
	public static final Rule<IAccount> USERNAME_MIN_5_CHARACTERS = PredicatedRule
			.create( "Username must be at least 5 characters long", account -> account.getUsername().length() >= 5 );
	public static final Rule<IAccount> EMAIL_PATTERN = PredicatedRule
			.create( "Email must follow valid email " + "syntax", account -> {
				final Optional<String> email = account.getData( "email" );
				//
				// Assume email is valid, if
				// it isn't supplied at all
				//
				return email.map( s -> PATTERN_EMAIL.matcher( s ).matches() ).orElse( true );
			} );
	@NonNull @Setter @Getter private static AccountVerifier globalAccountVerifier = new AccountVerifier();

	@Setter @Getter @NonNull private Verifier<IAccount> internalVerifier;

	public AccountVerifier()
	{
		final Verifier.VerifierBuilder<IAccount> builder = Verifier.builder();
		builder.nullable( false ).withRule( PASSWORD_MIN_CHARACTERS ).withRule( PASSWORD_MAX_CHARACTERS )
				.withRule( PASSWORD_NOT_USERNAME ).withRule( USERNAME_MIN_5_CHARACTERS ).withRule( EMAIL_PATTERN );
		this.internalVerifier = builder.build();
	}

	/**
	 * Check if an account is valid
	 *
	 * @param account Account to verify
	 * @return True if the account passes all checks, false if not
	 */
	public final boolean isValid(@NonNull final IAccount account)
	{
		return this.verifyAccount( account ).isEmpty();
	}

	/**
	 * Check if an account is valid, and return a list of constraint violations if it isn't
	 *
	 * @param account Account to verify
	 * @return Collection containing all violations, An empty collection if the account is valid
	 */
	public final Collection<Rule<IAccount>> verifyAccount(@NonNull final IAccount account)
	{
		return this.internalVerifier.verify( account );
	}

}

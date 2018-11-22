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
package xyz.kvantum.example;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import xyz.kvantum.server.api.account.AccountMatcherFactory;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.account.verification.AccountVerifier;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.orm.KvantumObjectFactory;
import xyz.kvantum.server.api.orm.KvantumObjectParserResult;
import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;
import xyz.kvantum.server.api.repository.FieldComparator;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.util.KvantumJsonFactory;
import xyz.kvantum.server.api.util.ParameterScope;
import xyz.kvantum.server.api.verification.Rule;
import xyz.kvantum.server.api.views.annotatedviews.ViewMatcher;
import xyz.kvantum.server.api.views.annotatedviews.converters.StandardConverters;
import xyz.kvantum.server.api.views.rest.RequestRequirements;
import xyz.kvantum.server.implementation.Account;

/**
 * Example of account registration
 */
public class ExampleAccountRegistration
{

	//
	// Just a copy of the global account verifier. Used to make sure that the supplied
	// details are up to par
	//
	private final AccountVerifier accountVerifier = AccountVerifier.getGlobalAccountVerifier();

	//
	// Factory for Account instances, will be used to past the POST request into an instance of Account
	//
	private final KvantumObjectFactory<Account> kvantumObjectFactory = KvantumObjectFactory.from( Account.class );

	//
	// Factory for KvantumPojo<Account> instances, will be used to convert the account to a JSONObject
	//
	private final KvantumPojoFactory<Account> kvantumPojoFactory = KvantumPojoFactory.forClass( Account.class );

	//
	// Request structure validator, will be used to verify that all required parameters
	// are submitted
	//
	private final RequestRequirements requestRequirements = new RequestRequirements();

	ExampleAccountRegistration()
	{
		ServerImplementation.getImplementation().getRouter().scanAndAdd( this );
		//
		// Here we add the required parameters: username & password
		//
		this.requestRequirements.addRequirement( new RequestRequirements.PostVariableRequirement( "username" ) );
		this.requestRequirements.addRequirement( new RequestRequirements.PostVariableRequirement( "password" ) );
	}

	@ViewMatcher(filter = "register", httpMethod = HttpMethod.POST, outputType = StandardConverters.JSON) @SuppressWarnings("unused") public JSONObject onRegister(
			final AbstractRequest request)
	{
		final JSONObject output = new JSONObject();
		//
		// We make sure that the client has provided all required parameters
		//
		final RequestRequirements.RequirementStatus requirementStatus = requestRequirements.testRequirements( request );
		if ( !requirementStatus.passed() )
		{
			output.put( "status", "error" );
			output.put( "cause", "missing-params" );
			output.put( "message", requirementStatus.getMessage() );
		} else
		{
			//
			// Here we parse the request and create a temporary account instance
			//
			final KvantumObjectParserResult<Account> kvantumObjectParserResult = kvantumObjectFactory
					.build( ParameterScope.POST ).parseRequest( request );
			if ( !kvantumObjectParserResult.isSuccess() )
			{
				output.put( "status", "error" );
				output.put( "cause", kvantumObjectParserResult.getError().getCause() );
			} else
			{
				//
				// Here we verify that the submitted parameters are up to par with the standards
				//
				final Account account = kvantumObjectParserResult.getParsedObject();
				final Collection<Rule<IAccount>> brokenRules = accountVerifier.verifyAccount( account );
				if ( !brokenRules.isEmpty() )
				{
					output.put( "status", "error" );
					output.put( "cause", "validation-error" );
					final JSONArray cause = KvantumJsonFactory.toJsonArray(
							brokenRules.stream().map( Rule::getRuleDescription ).collect( Collectors.toList() ) );
					output.put( "validation-errors", cause );
				} else
				{
					//
					// Here we make sure that the submitted account details are not
					// already associated with an account
					//
					final IAccountManager accountManager = ServerImplementation.getImplementation()
							.getApplicationStructure().getAccountManager();
					final AccountMatcherFactory<Account, IAccount> factory = new AccountMatcherFactory<>();
					final FieldComparator<? extends Account, ? super IAccount> comparator = factory
							.createMatcher( account );
					if ( !accountManager.findAllByQuery( comparator ).isEmpty() )
					{
						output.put( "status", "error" );
						output.put( "cause", "duplicate-account-details" );
					} else
					{
						//
						// Here we attempt to create an account with the submitted details
						//
						final Optional<IAccount> accountCreationOptional = accountManager.createAccount( account );
						if ( !accountCreationOptional.isPresent() )
						{
							output.put( "status", "error" );
							output.put( "cause", "creation-error" );
						} else
						{
							final Account created = ( Account ) accountCreationOptional.get();
							output.put( "status", "success" );
							//
							// Here we convert the account into a KvantumPojo<Account> instance
							// and then turn it into a JSONObject
							//
							final KvantumPojo<Account> kvantumPojo = kvantumPojoFactory.of( account );
							output.put( "account", kvantumPojo.toJson() );
						}
					}
				}
			}
		}
		return output;
	}
}

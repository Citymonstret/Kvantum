/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.api.views.rest;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.kvantum.server.api.orm.KvantumObjectFactory;
import xyz.kvantum.server.api.request.AbstractRequest;

/**
 * A simple framework which allows for validation of requests by providing some static conditions that must be met. This
 * is especially useful when using {@link KvantumObjectFactory} parsing.
 *
 * Example:
 * <pre>{@code
 * final RequestRequirements requestRequirements = new RequestRequirements()
 *      .addRequirement( new RequestRequirements.PostVariableRequirements( "username" ) );
 * final RequestRequirements.RequirementStatus requirementStatus = requestRequirements.testRequirements( request );
 * if ( !requirementStatus.passed() )
 * {
 *      Logger.debug( "Request failed checks: {}", requirementStatus.getMessage() );
 * }
 * }</pre>
 */
@SuppressWarnings({ "unused", "WeakerAccess" }) @NoArgsConstructor public class RequestRequirements
{

	private final Collection<RequestRequirement> requirements = new ArrayDeque<>();

	private static <K, V> Optional<V> mapOptional(Map<K, V> map, K instance)
	{
		if ( map.containsKey( instance ) )
		{
			return Optional.of( map.get( instance ) );
		} else
		{
			return Optional.empty();
		}
	}

	public RequestRequirements addRequirement(final RequestRequirement requirement)
	{
		this.requirements.add( requirement );
		return this;
	}

	public RequirementStatus testRequirements(final AbstractRequest request)
	{
		RequirementStatus status;
		for ( final RequestRequirement requirement : this.requirements )
		{
			if ( !( status = requirement.test( request ) ).passed )
			{
				return status;
			}
		}
		return RequirementStatus.builder().passed( true ).get();
	}

	public final static class GetVariableRequirement extends VariableRequirement
	{

		public GetVariableRequirement(String key)
		{
			super( key );
		}

		@Override protected Optional<String> getVariable(AbstractRequest request, String key)
		{
			return mapOptional( request.getQuery().getParameters(), key );
		}
	}

	public final static class PostVariableRequirement extends VariableRequirement
	{

		public PostVariableRequirement(String key)
		{
			super( key );
		}

		@Override protected Optional<String> getVariable(AbstractRequest request, String key)
		{
			return mapOptional( request.getPostRequest().get(), key );
		}
	}

	public abstract static class VariableRequirement extends RequestRequirement
	{

		private final String key;

		public VariableRequirement(final String key)
		{
			this.key = key;
		}

		protected abstract Optional<String> getVariable(AbstractRequest request, String key);

		@Override final RequirementStatus test(AbstractRequest request)
		{
			Optional<String> optional = getVariable( request, key );
			RequirementStatus.Builder builder = RequirementStatus.builder();
			if ( optional.isPresent() )
			{
				builder.passed( true );
			} else
			{
				builder.passed( false ).message( "Missing variable: " + key ).internalMessage( key );
			}
			return builder.get();
		}
	}

	public abstract static class RequestRequirement
	{

		abstract RequirementStatus test(AbstractRequest request);

	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE) public static final class RequirementStatus
	{

		@Getter private String message = "";
		@Getter private String internalMessage;
		private boolean passed = true;

		public static Builder builder()
		{
			return new Builder();
		}

		public boolean passed()
		{
			return this.passed;
		}

		@NoArgsConstructor(access = AccessLevel.PRIVATE) public static final class Builder
		{

			private final RequirementStatus requirementStatus = new RequirementStatus();

			public Builder message(final String message)
			{
				requirementStatus.message = message;
				return this;
			}

			public Builder internalMessage(final String internalMessage)
			{
				requirementStatus.internalMessage = internalMessage;
				return this;
			}

			public Builder passed(final boolean passed)
			{
				requirementStatus.passed = passed;
				return this;
			}

			public RequirementStatus get()
			{
				return requirementStatus;
			}

		}

	}

}

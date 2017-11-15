/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.views.rest;

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * A simple framework which allows for validation of requests by providing
 * some static conditions that must be met.
 * <p>
 * This is especially useful when using {@link com.github.intellectualsites.kvantum.api.orm.KvantumObjectFactory}
 * parsing
 * </p>
 * <p>
 * Example:
 * <pre>{@code
 * final RequestRequirements requestRequirements = new RequestRequirements()
 *      .addRequirement( new RequestRequirements.PostVariableRequirements( "username" ) );
 * final RequestRequirements.RequirementStatus requirementStatus = requestRequirements.testRequirements( request );
 * if ( !requirementStatus.passed() )
 * {
 *      Logger.debug( "Request failed checks: %s", requirementStatus.getMessage() );
 * }
 * }</pre>
 * </p>
 */
@SuppressWarnings( { "unused", "WeakerAccess" } )
@NoArgsConstructor
public class RequestRequirements
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
        return null;
    }

    public final static class GetVariableRequirement extends VariableRequirement
    {

        public GetVariableRequirement(String key)
        {
            super( key );
        }

        @Override
        protected Optional<String> getVariable(AbstractRequest request, String key)
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

        @Override
        protected Optional<String> getVariable(AbstractRequest request, String key)
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

        @Override
        final RequirementStatus test(AbstractRequest request)
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

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class RequirementStatus
    {

        @Getter
        private String message = "";
        @Getter
        private String internalMessage;
        private boolean passed = true;

        public static Builder builder()
        {
            return new Builder();
        }

        public boolean passed()
        {
            return this.passed;
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder
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

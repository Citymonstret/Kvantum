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
package com.github.intellectualsites.kvantum.api.validation;

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.request.HttpMethod;
import com.github.intellectualsites.kvantum.api.request.post.PostRequest;

import java.util.*;

public class ValidationManager
{

    private final Map<RequestValidation.ValidationStage, List<RequestValidation>> validators;
    private boolean empty = true;

    public ValidationManager()
    {
        this.validators = new HashMap<>();
        for ( final RequestValidation.ValidationStage stage : RequestValidation.ValidationStage.values() )
        {
            validators.put( stage, new ArrayList<>() );
        }
    }

    public boolean isEmpty()
    {
        return this.empty;
    }

    public List<RequestValidation> getValidators(final RequestValidation.ValidationStage stage)
    {
        return Collections.unmodifiableList( validators.get( stage ) );
    }

    public void addValidator(final RequestValidation validator)
    {
        this.empty = false;
        this.validators.get( validator.getStage() ).add( validator );
    }

    public void validate(final AbstractRequest request) throws ValidationException
    {
        if ( request.getQuery().getMethod() == HttpMethod.POST )
        {
            for ( final RequestValidation<PostRequest> validator :
                    this.getValidators( RequestValidation.ValidationStage.POST_PARAMETERS ) )
            {
                final RequestValidation.ValidationResult result = validator.validate( request
                        .getPostRequest() );
                if ( !result.isSuccess() )
                {
                    throw new ValidationException( result );
                }
            }
        } else
        {
            for ( final RequestValidation<AbstractRequest.Query> validator :
                    this.getValidators( RequestValidation.ValidationStage.GET_PARAMETERS ) )
            {
                final RequestValidation.ValidationResult result = validator.validate( request.getQuery() );
                if ( !result.isSuccess() )
                {
                    throw new ValidationException( result );
                }
            }
        }
    }

}

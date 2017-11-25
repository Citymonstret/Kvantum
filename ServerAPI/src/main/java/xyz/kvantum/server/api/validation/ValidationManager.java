/*
 *
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
package xyz.kvantum.server.api.validation;

import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.request.post.PostRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

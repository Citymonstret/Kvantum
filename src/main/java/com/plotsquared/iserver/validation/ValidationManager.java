package com.plotsquared.iserver.validation;

import com.plotsquared.iserver.views.RequestHandler;

import java.util.*;

public class ValidationManager
{

    private final RequestHandler parent;
    private final Map<RequestValidation.ValidationStage, List<RequestValidation>> validators;
    private boolean empty = true;

    public ValidationManager(final RequestHandler parent)
    {
        this.parent = parent;
        this.validators = new HashMap<>();
        for ( final RequestValidation.ValidationStage stage : RequestValidation.ValidationStage.values() )
        {
            validators.put(stage, new ArrayList<>());
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

}

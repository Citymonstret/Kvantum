package com.plotsquared.iserver.validation;

public class ValidationException extends Exception
{

    public ValidationException(RequestValidation.ValidationResult result)
    {
        super( "Validation Failed: " + result.getRequest() + " failed validation: " + result.getMessage() );
    }

}

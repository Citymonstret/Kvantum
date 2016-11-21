package com.plotsquared.iserver.validation;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.util.RequestChild;

public abstract class RequestValidation<T>
{

    private final ValidationStage stage;

    public RequestValidation(final ValidationStage stage)
    {
        this.stage = stage;
    }

    public abstract ValidationResult validate(final T t);

    public ValidationStage getStage()
    {
        return stage;
    }

    public enum ValidationStage
    {
        ADDRESS,
        POST_PARAMETERS,
        GET_PARAMETERS
    }

    public class ValidationResult
    {

        private final Request request;
        private final boolean success;
        private final String message;

        public ValidationResult(final RequestChild request)
        {
            this.request = request.getParent();
            this.success = true;
            this.message = null;
        }

        public ValidationResult(final RequestChild request, final String message)
        {
            this.request = request.getParent();
            this.success = false;
            this.message = message;
        }

        public Request getRequest()
        {
            return request;
        }

        public boolean isSuccess()
        {
            return success;
        }

        public String getMessage()
        {
            return message;
        }
    }

}

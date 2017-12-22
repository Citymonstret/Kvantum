/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
import xyz.kvantum.server.api.request.RequestChild;

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

        private final AbstractRequest request;
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

        public AbstractRequest getRequest()
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

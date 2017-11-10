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

import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.request.RequestChild;

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

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
package xyz.kvantum.server.api.validation;

import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.request.post.PostRequest;

import java.util.*;

@SuppressWarnings({"unused", "WeakerAccess "}) public class ValidationManager {

    private final Map<RequestValidation.ValidationStage, List<RequestValidation>> validators;
    private boolean empty = true;

    public ValidationManager() {
        this.validators = new HashMap<>();
        for (final RequestValidation.ValidationStage stage : RequestValidation.ValidationStage
            .values()) {
            validators.put(stage, new ArrayList<>());
        }
    }

    @Contract(value = "_ -> param1", pure = true) @SneakyThrows @SuppressWarnings("ALL")
    private static <T> RequestValidation<T> castValidator(final RequestValidation validator) {
        return (RequestValidation<T>) validator;
    }

    @Contract(value = "_ -> param1", pure = true) private static RequestValidation<PostRequest> asPostRequestValidator(
        final RequestValidation validator) {
        return castValidator(validator);
    }

    @Contract(value = "_ -> param1", pure = true) private static RequestValidation<AbstractRequest.Query> asQueryValidator(
        final RequestValidation validator) {
        return castValidator(validator);
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public List<RequestValidation> getValidators(final RequestValidation.ValidationStage stage) {
        return Collections.unmodifiableList(validators.get(stage));
    }

    public void addValidator(final RequestValidation validator) {
        this.empty = false;
        this.validators.get(validator.getStage()).add(validator);
    }

    public void validate(final AbstractRequest request) throws ValidationException {
        if (request.getQuery().getMethod() == HttpMethod.POST) {
            for (final RequestValidation<?> validator : this
                .getValidators(RequestValidation.ValidationStage.POST_PARAMETERS)) {
                final RequestValidation.ValidationResult result =
                    asPostRequestValidator(validator).validate(request.getPostRequest());
                if (!result.isSuccess()) {
                    throw new ValidationException(result);
                }
            }
        } else {
            for (final RequestValidation<?> validator : this
                .getValidators(RequestValidation.ValidationStage.GET_PARAMETERS)) {
                final RequestValidation.ValidationResult result =
                    asQueryValidator(validator).validate(request.getQuery());
                if (!result.isSuccess()) {
                    throw new ValidationException(result);
                }
            }
        }
    }
}

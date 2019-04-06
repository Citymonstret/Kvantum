/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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

import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.request.post.RequestEntity;

import java.util.*;

/**
 * Manager that handles validation of {@link AbstractRequest requests} using
 * {@link RequestValidation validators}
 */
@SuppressWarnings({"unused", "WeakerAccess "}) public class ValidationManager {

    private final Map<RequestValidation.ValidationStage, List<RequestValidation>> validators;
    private boolean empty = true;

    /**
     * Initialize a new {@link ValidationManager}
     */
    public ValidationManager() {
        this.validators = new HashMap<>();
        for (final RequestValidation.ValidationStage stage : RequestValidation.ValidationStage
            .values()) {
            validators.put(stage, new ArrayList<>());
        }
    }

    @SneakyThrows @SuppressWarnings("ALL")
    private static <T> RequestValidation<T> castValidator(final RequestValidation validator) {
        return (RequestValidation<T>) validator;
    }

    private static RequestValidation<RequestEntity> asPostRequestValidator(
        final RequestValidation validator) {
        return castValidator(validator);
    }

    private static RequestValidation<AbstractRequest.Query> asQueryValidator(
        final RequestValidation validator) {
        return castValidator(validator);
    }

    /**
     * Check whether or not the {@link ValidationManager manager} instance contains
     * any {@link RequestValidation validator} instances
     *
     * @return true if the manager does not contain any validators, else true
     */
    public boolean isEmpty() {
        return this.empty;
    }

    /**
     * Get a {@link List list} containing all {@link RequestValidation validators} for a given
     * {@link xyz.kvantum.server.api.validation.RequestValidation.ValidationStage validation stage}
     *
     * @param stage state for which to get the validators
     * @return unmodifiable collection containing the validators
     */
    public List<RequestValidation> getValidators(final RequestValidation.ValidationStage stage) {
        return Collections.unmodifiableList(validators.get(stage));
    }

    /**
     * Register a link {@link RequestValidation validator} in this {@link ValidationManager manager}
     *
     * @param validator validator to register, cannot be null
     */
    public void addValidator(@NonNull final RequestValidation validator) {
        this.empty = false;
        this.validators.get(validator.getStage()).add(validator);
    }

    /**
     * Validate a {@link AbstractRequest request} using the {@link RequestValidation validators}
     * registered in this {@link ValidationManager manager}, throws an exception if the request
     * fails the validation at any of the validation stages
     *
     * @param request request to validate, cannot be null
     * @throws ValidationException on failure, containing result about the failure
     */
    public void validate(@NonNull final AbstractRequest request) throws ValidationException {
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

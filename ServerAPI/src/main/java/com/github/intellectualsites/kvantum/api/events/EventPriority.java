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
package com.github.intellectualsites.kvantum.api.events;

/**
 * This is how we decide what event listeners
 * that gets to act in what order. This is useful
 * as it can be used to create a hierarchy for
 * internal listeners and alike
 *
 * @author Citymonstret
 */
public enum EventPriority
{

    /**
     * Low Priority
     */
    LOW,

    /**
     * Medium Priority
     */
    MEDIUM,

    /**
     * High Priority
     */
    HIGH
}

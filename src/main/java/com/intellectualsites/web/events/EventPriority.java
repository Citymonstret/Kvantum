package com.intellectualsites.web.events;

/**
 * This is how we decide what event listeners
 * that gets to act in what order. This is useful
 * as it can be used to create a hierarchy for
 * internal listeners and alike
 *
 * @author Citymonstret
 */
public enum EventPriority {

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
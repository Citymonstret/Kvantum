package com.github.intellectualsites.iserver.api.util;

import java.util.Map;

@FunctionalInterface
public interface VariableHolder
{

    Map<String, String> getVariables();

}

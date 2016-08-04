package com.intellectualsites.web.object;

@FunctionalInterface
public interface Generator<I,O> {

    O generate(I input);

}

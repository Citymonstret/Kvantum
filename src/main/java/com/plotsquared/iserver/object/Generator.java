package com.plotsquared.iserver.object;

@FunctionalInterface
public interface Generator<I, O> {

    O generate(I input);

}

package com.github.intellectualsites.kvantum.implementation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class ReturnStatus extends Throwable
{

    @Getter
    private final String status;

    @Getter
    private final WorkerContext applicableContext;

}

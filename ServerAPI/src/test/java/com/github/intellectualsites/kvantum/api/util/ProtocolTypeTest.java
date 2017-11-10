package com.github.intellectualsites.kvantum.api.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class ProtocolTypeTest
{

    @Test
    void getByName()
    {
        final Optional<ProtocolType> protocolTypeOptional = ProtocolType.getByName( "HTTPS" );
        Assertions.assertTrue( protocolTypeOptional.isPresent() );
        Assertions.assertEquals( ProtocolType.HTTPS, protocolTypeOptional.get() );
    }

}

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
package com.github.intellectualsites.kvantum.api.orm;

import com.github.intellectualsites.kvantum.api.mocking.MockRequest;
import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumConstructor;
import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumField;
import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumObject;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.request.HttpMethod;
import com.github.intellectualsites.kvantum.api.util.ParameterScope;
import net.sf.oval.constraint.MinLength;
import net.sf.oval.constraint.NotEmpty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KvantumObjectFactoryTest
{

    @Test
    void from()
    {
        //
        // Make sure that the request is parsed successfully
        //
        final KvantumObjectFactory<MockObject> factory = KvantumObjectFactory.from( MockObject.class );
        MockRequest mockRequest = new MockRequest( new AbstractRequest.Query( HttpMethod.POST,
                "/?koala=animal" ) );
        KvantumObjectParserResult<MockObject> result = factory.build( ParameterScope.GET )
                .parseRequest( mockRequest );
        Assertions.assertNotNull( result );
        Assertions.assertTrue( result.isSuccess() );
        Assertions.assertNull( result.getError() );
        Assertions.assertNotNull( result.getParsedObject() );
        final MockObject mockObject = result.getParsedObject();
        Assertions.assertEquals( "animal", mockObject.string1 );
        Assertions.assertEquals( "york", mockObject.string2 );

        //
        // Test if the validation fails (minimum size of string1 is 2, but provided string size is 1)
        //
        mockRequest = new MockRequest( new AbstractRequest.Query( HttpMethod.POST, "/?koala=a" ) );
        result = factory.build( ParameterScope.GET ).parseRequest( mockRequest );
        Assertions.assertNotNull( result );
        Assertions.assertFalse( result.isSuccess() );
        Assertions.assertNotNull( result.getError() );
        Assertions.assertNull( result.getParsedObject() );
        Assertions.assertTrue( result.getError()
                instanceof KvantumObjectParserResult.KvantumObjectParserValidationFailed );
        final KvantumObjectParserResult.KvantumObjectParserValidationFailed validationFailed = (
                KvantumObjectParserResult.KvantumObjectParserValidationFailed) result.getError();
        Assertions.assertEquals( 1, validationFailed.getViolations().size() );
    }

    @KvantumObject(checkValidity = true)
    private static class MockObject
    {

        @MinLength(2)
        @NotEmpty
        @KvantumField(kvantumName = "koala")
        private String string1;

        @KvantumField(kvantumName = "new", defaultValue = "york")
        private String string2;

        @KvantumConstructor
        private MockObject()
        {
        }

    }

}

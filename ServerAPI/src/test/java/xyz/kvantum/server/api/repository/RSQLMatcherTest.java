package xyz.kvantum.server.api.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RSQLMatcherTest
{

    @Test
    void testMatches()
    {
        final RSQLMatcherFactory<MockObject> factory = new RSQLMatcherFactory<>();
        final MockObject mockObject1 = new MockObject( "Anders", 32 );
        final MockObject mockObject2 = new MockObject( "Bertil", 59 );
        final String query1 = "age=lt=40";
        final String query2 = "name==Anders";
        final RSQLMatcher<MockObject> matcher1 = factory.createMatcher( query1 );
        final RSQLMatcher<MockObject> matcher2 = factory.createMatcher( query2 );
        Assertions.assertTrue( matcher1.matches( mockObject1 ) );
        Assertions.assertFalse( matcher1.matches( mockObject2 ) );
        Assertions.assertTrue( matcher2.matches( mockObject1 ) );
        Assertions.assertFalse( matcher2.matches( mockObject2 ) );
    }

    /**
     * Simple mocking pojo
     */
    @SuppressWarnings("unused")
    private static final class MockObject
    {

        private final String name;
        private final int age;

        private MockObject(final String name, final int age)
        {
            this.name = name;
            this.age = age;
        }

        public String getName()
        {
            return this.name;
        }

        public int getAge()
        {
            return this.age;
        }

        @Override
        public String toString()
        {
            return String.format( "{name=\"%s\",age=%d}", name, age );
        }
    }

}

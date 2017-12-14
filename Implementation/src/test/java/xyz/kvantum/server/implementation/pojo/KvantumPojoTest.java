package xyz.kvantum.server.implementation.pojo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KvantumPojoTest
{

    @Test
    void testPojo()
    {
        final KvantumPojoFactory<Person> kvantumPojoFactory = KvantumPojoFactory.forClass( Person.class );
        Assertions.assertNotNull( kvantumPojoFactory );
        final Person person = new Person( "Olof", 32 );
        final KvantumPojo kvantumPojo = kvantumPojoFactory.of( person );
        Assertions.assertNotNull( kvantumPojo );
        Assertions.assertTrue( kvantumPojo.containsKey( "name" ) );
        Assertions.assertTrue( kvantumPojo.containsKey( "age" ) );
        Assertions.assertEquals( 2, kvantumPojo.getAll().size() );
        Assertions.assertEquals( kvantumPojo.get( "name" ), "Olof" );
        Assertions.assertEquals( kvantumPojo.get( "age" ), 32 );
        Assertions.assertEquals( kvantumPojo.toJson().toString(), "{\"name\":\"Olof\",\"age\":32}" );
    }

    private static final class Person
    {

        private final String name;
        private final int age;

        public Person(final String name, final int age)
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
    }

}

/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
package xyz.kvantum.server.api.pojo;

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
        final KvantumPojo<Person> kvantumPojo = kvantumPojoFactory.of( person );
        Assertions.assertNotNull( kvantumPojo );
        Assertions.assertTrue( kvantumPojo.containsGetter( "name" ) );
        Assertions.assertTrue( kvantumPojo.containsGetter( "age" ) );
        Assertions.assertEquals( 2, kvantumPojo.getAll().size() );
        Assertions.assertEquals( kvantumPojo.get( "name" ), "Olof" );
        Assertions.assertEquals( kvantumPojo.get( "age" ), 32 );
        Assertions.assertEquals( kvantumPojo.toJson().toString(), "{\"name\":\"Olof\",\"age\":32}" );
        kvantumPojo.set( "name", "Eric" );
        Assertions.assertEquals( person, kvantumPojo.getPojo() );
        Assertions.assertEquals( kvantumPojo.getPojo().getName(), kvantumPojo.get( "name" ) );
        Assertions.assertEquals( kvantumPojo, kvantumPojoFactory.of( person ) );
    }

    private static final class Person
    {

        private String name;
        private int age;

        public Person(final String name, final int age)
        {
            this.name = name;
            this.age = age;
        }

        public String getName()
        {
            return this.name;
        }

        public void setName(final String name)
        {
            this.name = name;
        }

        public int getAge()
        {
            return this.age;
        }
    }

}

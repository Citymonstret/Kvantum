/*
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

import com.google.common.collect.ImmutableMap;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class PojoXmlFactory<Pojo>
{

    private final XStream xStream;
    private final KvantumPojoFactory<Pojo> kvantumPojoFactory;

    PojoXmlFactory(@NonNull final KvantumPojoFactory<Pojo> kvantumPojoFactory)
    {
        this.kvantumPojoFactory = kvantumPojoFactory;
        this.xStream = new XStream();
        this.xStream.registerConverter( new MapEntryConverter() );
        this.xStream.alias( "root", KvantumPojo.class );
    }

    public String toXml(final KvantumPojo<Pojo> kvantumPojo)
    {
        return xStream.toXML( kvantumPojo.getAll() );
    }

    public String toXml(final Pojo pojo)
    {
        return this.toXml( kvantumPojoFactory.of( pojo ) );
    }

    private static final class MapEntryConverter implements Converter
    {

        @Override
        public void marshal(final Object source,
                            final HierarchicalStreamWriter writer,
                            final MarshallingContext context)
        {
            ( (ImmutableMap<?, ?>) source ).forEach( (key, value) ->
            {
                writer.startNode( key.toString() );
                writer.setValue( value.toString() );
                writer.endNode();
            } );
        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader,
                                final UnmarshallingContext context)
        {
            final Map<String, String> map = new HashMap<>();
            while ( reader.hasMoreChildren() )
            {
                reader.moveDown();
                map.put( reader.getNodeName(), reader.getValue() );
                reader.moveUp();
            }
            return map;
        }

        @Override
        public boolean canConvert(@NonNull final Class type)
        {
            return ImmutableMap.class.isAssignableFrom( type );
        }
    }
}

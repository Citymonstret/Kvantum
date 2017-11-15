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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.util.KvantumJsonFactory;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * GSON serializer for accounts. Will only expose ID and username (not data), so this
 * can be used publicly
 */
final public class AccountSerializer implements JsonSerializer<Account>, JsonDeserializer<Account>
{

    @Override
    public JsonElement serialize(final Account src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add( "id", new JsonPrimitive( src.getId() ) );
        jsonObject.add( "username", KvantumJsonFactory.stringToPrimitive( src.getUsername() ) );
        return jsonObject;
    }

    @Override
    public Account deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws
            JsonParseException
    {
        if ( json instanceof JsonObject )
        {
            final JsonObject object = (JsonObject) json;
            return new Account( object.get( "id" ).getAsInt(),
                    object.get( "username" ).getAsString(), "" );
        } else
        {
            throw new JsonParseException( "Provided json not an object" );
        }
    }

}

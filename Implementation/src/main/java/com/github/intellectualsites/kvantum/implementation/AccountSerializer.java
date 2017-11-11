package com.github.intellectualsites.kvantum.implementation;

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
        jsonObject.add( "username", new JsonPrimitive( src.getUsername() ) );
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

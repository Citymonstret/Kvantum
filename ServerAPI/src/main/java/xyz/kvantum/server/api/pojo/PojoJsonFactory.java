package xyz.kvantum.server.api.pojo;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class PojoJsonFactory<Pojo>
{

    private final KvantumPojoFactory<Pojo> kvantumPojoFactory;

    public JSONObject toJson(final KvantumPojo<Pojo> kvantumPojo)
    {
        return new JSONObject( kvantumPojo.getAll() );
    }

    public JSONObject toJson(final Pojo pojo)
    {
        return this.toJson( kvantumPojoFactory.of( pojo ) );
    }

}

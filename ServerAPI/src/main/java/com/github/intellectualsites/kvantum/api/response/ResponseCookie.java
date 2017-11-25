package com.github.intellectualsites.kvantum.api.response;

import com.github.intellectualsites.kvantum.api.util.TimeUtil;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;

@EqualsAndHashCode
@Getter
@Builder
public final class ResponseCookie
{

    @Builder.Default
    public String path = "/";
    private String cookie;
    private String value;
    @Builder.Default
    private Date expires = null;
    @Builder.Default
    private boolean httpOnly = false;
    @Builder.Default
    private boolean secure = false;

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder( this.cookie )
                .append( "=" ).append( value ).append( "; Path=" ).append( path );
        if ( this.expires != null )
        {
            builder.append( "; Expires=" ).append( TimeUtil.getHTTPTimeStamp( this.expires ) );
        }
        if ( this.secure )
        {
            builder.append( "; Secure" );
        }
        if ( this.httpOnly )
        {
            builder.append( "; HttpOnly" );
        }
        return builder.toString();
    }

}

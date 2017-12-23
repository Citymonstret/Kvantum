package xyz.kvantum.server.api.response;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.TimeUtil;

@Getter
@RequiredArgsConstructor
@Builder
public final class FinalizedResponse
{

    private static final String LOG_FORMAT = "%h %l %u [%t] \"%r\" %>s %b";

    @NonNull
    private final String address;
    @Nullable
    private final AbstractRequest.Authorization authorization;
    private final long timeFinished;
    @NonNull
    private final AbstractRequest.Query query;
    @NonNull
    private final String status;
    private final int length;

    public String toLogString()
    {
        return LOG_FORMAT.replace( "%h", this.address ).replace( "%l", "-" )
                .replace( "%u", authorization != null ? authorization.getUsername() : "-" )
                .replace( "%t", TimeUtil.getAccessLogTimeStamp( this.timeFinished ) )
                .replace( "%r", query.getMethod().name()
                        + " " + query.getResource() + " HTTP/1.1" )
                .replace( "%>s", this.status.substring( 0, 3 ) )
                .replace( "%b", String.valueOf( this.length ) );
    }
}

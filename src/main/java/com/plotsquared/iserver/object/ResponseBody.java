package com.plotsquared.iserver.object;

public interface ResponseBody extends HeaderProvider
{

    byte[] getBytes();

    Header getHeader();

    String getContent();

    boolean isText();

}

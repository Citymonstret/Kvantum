package com.intellectualsites.web.object;

public interface ResponseBody extends HeaderProvider {

    byte[] getBytes();

    Header getHeader();

    String getContent();

    boolean isText();

}

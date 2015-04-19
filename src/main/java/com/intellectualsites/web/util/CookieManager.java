package com.intellectualsites.web.util;

import com.intellectualsites.web.object.Cookie;
import com.intellectualsites.web.object.Request;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class CookieManager {

    public static Cookie[] getCookies(final Request r) {
        String raw = r.getHeader("Cookie");
        if (raw.equals("")) {
            return new Cookie[0];
        }
        raw = raw.replaceFirst(" ", "");
        String[] pieces = raw.split("; ");
        Cookie[] cookies = new Cookie[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            String piece = pieces[i];
            String[] piecePieces = piece.split("=");
            if (piecePieces.length == 1) {
                cookies[i] = new Cookie(piecePieces[0], "");
            } else {
                cookies[i] = new Cookie(piecePieces[0], piecePieces[1]);
            }
        }
        return cookies;
    }

}

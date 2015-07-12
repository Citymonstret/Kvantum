package com.intellectualsites.web.util;

import com.intellectualsites.web.object.Cookie;
import com.intellectualsites.web.object.Request;

/**
 * This is an utility class
 * created to handle all
 * cookie related actions
 *
 * @author Citymonstret
 */
public class CookieManager {

    /**
     * Get all cookies from a HTTP Request
     * @param r HTTP Request
     * @return an array containing the cookies
     */
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

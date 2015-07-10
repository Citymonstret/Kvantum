package com.intellectualsites.web.views;

import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.cache.CacheApplicable;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created 2015-04-21 for IntellectualServer
 *
 * @author Citymonstret
 */
public class ImgView extends View implements CacheApplicable {

    public ImgView(String filter, Map<String, Object> options) {
        super(filter, "img", options);
        super.relatedFolderPath = "/assets/img";
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2) + matcher.group(3);

        if(file.endsWith(".png")) {
            request.addMeta("img_type", "png");
        } else if(file.endsWith(".ico")) {
            request.addMeta("img_type", "x-icon");
        } else if(file.endsWith(".gif")) {
            request.addMeta("img_type", "gif");
        } else if (file.endsWith(".jpg") || file.endsWith(".jpeg")) {
            request.addMeta("img_type", "jpeg");
        } else {
            return false;
        }
        request.addMeta("img_file", file);
        return matcher.matches() && (new  File(getFolder(), file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(getFolder(), r.getMeta("img_file").toString());
        byte[] bytes = new byte[0];
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file), getBuffer());
            bytes = IOUtils.toByteArray(stream);
            stream.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        Response response = new Response(this);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, "image/" + r.getMeta("img_type") + "; charset=utf-8");
        response.setBytes(bytes);
        return response;
    }

    @Override
    public boolean isApplicable(Request r) {
        return false;
    }
}

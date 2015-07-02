package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.View;
import com.intellectualsites.web.util.Context;
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
public class ImgView extends View {

    private File folder;
    private int buffer;

    public ImgView(String filter, Map<String, Object> options) {
        super(filter, options);

        if (containsOption("buffer")) {
            this.buffer = getOption("buffer");
        } else {
            this.buffer = 1024 * 64;
        }
        if (containsOption("folder")) {
            this.folder = new File(getOption("folder").toString());
        } else {
            this.folder = new File(Context.coreFolder, "/assets/img");
        }

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                System.out.println("Couldn't create the img folder...");
            }
        }
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
        return matcher.matches() && (new  File(folder, file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(folder, r.getMeta("img_file").toString());
        byte[] bytes = new byte[0];
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file), buffer);
            bytes = IOUtils.toByteArray(stream);
            stream.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        Response response = new Response(this);
        response.getHeader().set("Content-Type", "image/" + r.getMeta("img_type") + "; charset=utf-8");
        response.setBytes(bytes);
        return response;
    }
}

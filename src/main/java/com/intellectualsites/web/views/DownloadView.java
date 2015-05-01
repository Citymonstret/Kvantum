package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.View;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created 2015-05-01 for IntellectualServer
 *
 * @author Citymonstret
 */
public class DownloadView extends View {

    private File folder;

    public DownloadView(String filter, Map<String, Object> options) {
        super(filter, options);

        if (containsOption("folder")) {
            this.folder = new File(getOption("folder").toString());
        } else {
            this.folder = new File("./assets/downloads");
        }

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                System.out.println("Couldn't create the download folder...");
            }
        }
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2) + matcher.group(3);

        if(file.endsWith(".zip")) {
            request.addMeta("file_type", "zip");
        } else if(file.endsWith(".txt")) {
            request.addMeta("file_type", "txt");
        } else if(file.endsWith(".pdf")) {
            request.addMeta("file_type", "pdf");
        } else {
            return false;
        }
        request.addMeta("zip_file", file);
        return matcher.matches() && (new  File(folder, file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(folder, r.getMeta("zip_file").toString());
        byte[] bytes = new byte[0];
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
            bytes = IOUtils.toByteArray(stream);
            stream.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        Response response = new Response(this);
        response.getHeader().set("Content-Type", "application/octet-stream; charset=utf-8");
        response.getHeader().set("Content-Disposition", "attachment; filename=\"" + r.getMeta("zip_file").toString() + "\"");
        response.getHeader().set("Content-Transfer-Encoding", "binary");
        response.getHeader().set("Content-Length", "" + file.length());
        response.setBytes(bytes);
        return response;
    }

}

package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Header;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
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

    public DownloadView(String filter, Map<String, Object> options) {
        super(filter, "download", options);
        super.relatedFolderPath = "/assets/downloads";
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
        return matcher.matches() && (new  File(getFolder(), file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(getFolder(), r.getMeta("zip_file").toString());
        byte[] bytes = new byte[0];
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file), getBuffer());
            bytes = IOUtils.toByteArray(stream);
            stream.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        Response response = new Response(this);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_OCTET_STREAM);
        response.getHeader().set(Header.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + r.getMeta("zip_file").toString() + "\"");
        response.getHeader().set(Header.HEADER_CONTENT_TRANSFER_ENCODING, "binary");
        response.getHeader().set(Header.HEADER_CONTENT_LENGTH, "" + file.length());
        response.setBytes(bytes);
        return response;
    }

}

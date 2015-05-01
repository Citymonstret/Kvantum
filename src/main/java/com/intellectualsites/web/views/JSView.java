package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created 2015-04-22 for IntellectualServer
 *
 * @author Citymonstret
 */
public class JSView extends View {

    private File folder;

    public JSView(String filter, Map<String, Object> options) {
        super(filter, options);

        if (containsOption("folder")) {
            this.folder = new File(getOption("folder").toString());
        } else {
            this.folder = new File("./assets/js");
        }

        if (!this.folder.exists()) {
            if (!this.folder.mkdirs()) {
                System.out.println("Couldn't create the js folder...");
            }
        }
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2);
        if (!file.endsWith(".js"))
            file = file + ".js";
        request.addMeta("js_file", file);
        return matcher.matches() && (new  File(folder, file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(folder, r.getMeta("js_file").toString());
        StringBuilder document = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                document.append(line).append("\n");
            }
            reader.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        Response response = new Response(this);
        response.getHeader().set("Content-Type", "text/javascript; charset=utf-8");
        response.setContent(document.toString());
        return response;
    }
}

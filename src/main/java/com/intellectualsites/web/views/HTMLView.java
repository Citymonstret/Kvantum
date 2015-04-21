package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class HTMLView extends View {

    private final File folder;

    public HTMLView() {
        super("(\\/)([A-Za-z0-9]*)(.html)?");

        this.folder = new File("./html");
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                System.out.println("Couldn't create the html folder...");
            }
        }
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2);
        if (file == null || file.equals("")) {
            file = "index";
        }
        request.addMeta("html_file", file);
        return foundFile(file);
    }

    @Override
    public Response generate(final Request r) {
        File file = new File(folder, r.getMeta("html_file") + ".html");
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
        response.setContent(document.toString());
        return response;
    }

    private boolean foundFile(final String file) {
        return new File(folder, file + ".html").exists();
    }
}

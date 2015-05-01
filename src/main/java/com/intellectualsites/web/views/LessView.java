package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.View;
import org.lesscss.LessCompiler;

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
public class LessView extends View {

    public static LessCompiler compiler;

    private File folder;

    public LessView(String filter, Map<String, Object> options) {
        super(filter);

        if (containsOption("folder")) {
            this.folder = new File(getOption("folder").toString());
        } else {
            this.folder = new File("./assets/less");
        }

        if (!this.folder.exists()) {
            if (!this.folder.mkdirs()) {
                System.out.println("Couldn't create the less folder...");
            }
        }
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2);
        if (!file.endsWith(".less"))
            file = file + ".less";
        request.addMeta("less_file", file);
        return matcher.matches() && (new  File(folder, file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(folder, r.getMeta("less_file").toString());
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
        response.getHeader().set("Content-Type", "text/css; charset=utf-8");

        if (compiler == null) {
            compiler = new LessCompiler();
        }
        try {
            response.setContent(compiler.compile(document.toString()));
        } catch(final Exception e) {
            response.setContent("");
            e.printStackTrace();
        }

        return response;
    }
}

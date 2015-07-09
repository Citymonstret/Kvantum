package com.intellectualsites.web.views;

import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.cache.CacheApplicable;
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
public class LessView extends View implements CacheApplicable {

    public static LessCompiler compiler;

    public LessView(String filter, Map<String, Object> options) {
        super(filter, "less", options);
        super.relatedFolderPath = "/assets/less";
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2);
        if (!file.endsWith(".less"))
            file = file + ".less";
        request.addMeta("less_file", file);
        return matcher.matches() && (new File(getFolder(), file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(getFolder(), r.getMeta("less_file").toString());
        StringBuilder document = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file), getBuffer());
            String line;
            while ((line = reader.readLine()) != null) {
                document.append(line).append("\n");
            }
            reader.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        Response response = new Response(this);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_CSS);

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

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }
}

package com.intellectualsites.web.views;

import com.intellectualsites.web.config.YamlConfiguration;
import com.intellectualsites.web.object.IgnoreSyntax;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.View;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

public class RedirectView extends View implements IgnoreSyntax {

    private final YamlConfiguration configuration;

    public RedirectView(String pattern, Map<String, Object> options) {
        super(pattern);

        File file;
        if (options.containsKey("folder")) {
            file = new File(options.get("folder").toString(), "redirect.yml");
        } else {
            file = new File("./redirect/", "redirect.yml");
        }

        try {
            configuration = new YamlConfiguration("redirect", file);
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }

        configuration.loadFile();
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        if (configuration.contains(matcher.group(1))) {
            request.addMeta("redirect_url", configuration.get(matcher.group(1)));
            return true;
        }
        return false;
    }

    @Override
    public Response generate(final Request r) {
        Response response = new Response(this);
        response.getHeader().redirect(r.getMeta("redirect_url").toString());
        return response;
    }
}

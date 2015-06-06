package com.intellectualsites.web.object.syntax;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.core.ServerAPI;
import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Syntax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Include extends Syntax {

    public Include() {
        super(Pattern.compile("\\{\\{include:([/A-Za-z\\.\\-]*)\\}\\}"));
    }

    @Override
    public String process(String in, Matcher matcher, Request r, Map<String, ProviderFactory> factories) {
        while (matcher.find()) {
            File file = new File(Server.getInstance().coreFolder, matcher.group(1));
            if (file.exists()) {
                StringBuilder c = new StringBuilder();
                String line;
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    while ((line = reader.readLine()) != null)
                        c.append(line).append("\n");
                    reader.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                if (file.getName().endsWith(".css")) {
                    in = in.replace(matcher.group(), "<style>\n" + c + "</style>");
                } else {
                    in = in.replace(matcher.group(), c.toString());
                }
            } else {
                Server.getInstance().log("Couldn't find file for '%s'", matcher.group());
            }
        }
        return in;
    }
}

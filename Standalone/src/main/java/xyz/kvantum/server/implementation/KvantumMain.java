/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.implementation;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.util.RequestManager;
import xyz.kvantum.server.api.util.TimeUtil;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Optional;

/**
 * Application entry point
 */
@SuppressWarnings({"WeakerAccess", "unused"}) public final class KvantumMain {

    /**
     * Launcher method
     *
     * @param arguments Command line arguments
     */
    public static void main(final String[] arguments) throws Throwable {
        //
        // Determine whether or not the server is running with extended
        // privileges. Not doing this will affect port binding
        // to the reserved port range.
        //
        // This only matters if the server is run on a linux system
        //
        final String osName = System.getProperty("os.name");
        if (osName.toLowerCase(Locale.ENGLISH).startsWith("linux")) {
            System.out.println("Server running on Linux! Checking privileges...");

            boolean isPrivileged = false;

            final Process process = Runtime.getRuntime().exec(new String[] {"id", "-u"});
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    isPrivileged = line.equals("0");
                }
            }
            process.destroyForcibly();

            if (!isPrivileged) {
                System.out.println("\nWARNING\n"
                    + "The server is not privileged, and might therefore not be able to bind to port 80.\n"
                    + "Not running as a privileged user may also cause complications with file creation. Beware.\n");
            }
        }

        //
        // Here we parse command line arguments
        //
        final Options options = new Options();
        final JCommander jCommander = new JCommander(options);
        jCommander.parse(arguments);
        jCommander.setProgramName("Kvantum");

        if (options.help) {
            final StringBuilder message = new StringBuilder();
            jCommander.usage(message);

            System.out.print("[Kvantum][Help]: ");
            System.out.print(message);
            System.out.println();

            System.exit(0);
        } else {
            String folder = options.folder;
            if (folder.isEmpty()) {
                if (System.getenv().containsKey("KVANTUM_HOME")) {
                    folder = System.getenv("KVANTUM_HOME");
                } else {
                    folder = "./";
                }
            }

            final File file = new File(folder, "kvantum");

            System.out.printf("INFO%nUsing server folder: %s%n%n", file.getAbsolutePath());

            final Optional<Kvantum> server =
                ServerContext.builder().coreFolder(file).standalone(true)
                    .router(RequestManager.builder().build())
                    .serverSupplier(StandaloneServer::new).build().create();
            if (server.isPresent()) {
                if (!options.debug.isEmpty()) {
                    CoreConfig.debug = true;
                    CoreConfig.verbose = true;
                }
                if (options.port != -1) {
                    CoreConfig.port = options.port;
                }
                try {
                    if (!server.get().start()) {
                        throw new KvantumInitializationException(
                            "The server instance was not started");
                    }
                } catch (final Exception e) {
                    throw new KvantumInitializationException("Failed to start the server instance",
                        e);
                }
            } else {
                throw new KvantumInitializationException("Failed to create a server instance");
            }
        }
    }

    public static Optional<? extends Kvantum> start(final File coreFolder) {
        return start(ServerContext.builder().standalone(false).coreFolder(coreFolder)
            .router(RequestManager.builder().build()).build());
    }

    public static Optional<? extends Kvantum> start() {
        return start(new File("./"));
    }

    /**
     * Create & Start the server
     *
     * @param serverContext context to initialize the server with
     * @return Optional of nullable server
     */
    public static Optional<? extends Kvantum> start(final ServerContext serverContext) {
        if (serverContext == null) {
            throw new NullPointerException("Supplied server context cannot be null!");
        }
        Optional<? extends Kvantum> server = serverContext.create();
        try {
            if (server.isPresent()) {
                if (!server.get().start()) {
                    throw new KvantumInitializationException("The server was not started");
                }
            }
        } catch (final Exception e) {
            e.printStackTrace(); // Can't use ErrorDigest
        }
        return server;
    }

    /**
     * Command line arguments
     */
    private static class Options {

        @Parameter(names = "-folder", description = "Application base folder path") private String
            folder = "";

        @Parameter(names = "-help", description = "Show this list") private boolean help = false;

        @Parameter(names = "-port", description = "The server port") private int port = -1;

        @Parameter(names = "-debug", description = "Enable debugging") private String debug = "";

    }
}

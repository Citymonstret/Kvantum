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
package xyz.kvantum.server.api.views;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.kvantum.files.FileSystem;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.exceptions.KvantumException;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.matching.FilePattern;
import xyz.kvantum.server.api.matching.Router;
import xyz.kvantum.server.api.matching.ViewPattern;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.HeaderOption;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.api.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Higher level implementation of {@link RequestHandler} which primarely focuses on static resource handling. Extended
 * by {@link StaticFileView}
 * {@inheritDoc}
 */
@SuppressWarnings("ALL") @EqualsAndHashCode(of = "internalName", callSuper = false)
public class View extends RequestHandler {

    /**
     * Variable corrensponding to {@link #PATTERN_VARIABLE_FILE}
     */
    public static final String VARIABLE_FILE = "file";
    /**
     * Variable corrensponding to {@link #PATTERN_VARIABLE_FOLDER}
     */
    public static final String VARIABLE_FOLDER = "folder";
    /**
     * Variable corrensponding to {@link #PATTERN_VARIABLE_EXTENSION}
     */
    public static final String VARIABLE_EXTENSION = "extension";
    /**
     * Pattern corrensponding to {@link #VARIABLE_FILE}
     */
    public static final String PATTERN_VARIABLE_FILE = "{file}";
    /**
     * Pattern corrensponding to {@link #VARIABLE_FOLDER}
     */
    public static final String PATTERN_VARIABLE_FOLDER = "{folder}";
    /**
     * Pattern corrensponding to {@link #VARIABLE_EXTENSION}
     */
    public static final String PATTERN_VARIABLE_EXTENSION = "{extension}";
    public static final String CONSTANT_BUFFER = "buffer";
    public static final String CONSTANT_VARIABLES = "variables";
    private static final String INTERNAL_NAME = "internalName";
    private static final String EXTENSION_REWRITE = "extensionRewrite";
    private static final String FORCE_HTTPS = "forceHTTPS";
    private static final String HEADERS = "headers";
    private static final String METHOD = "method";
    private static final String FILE_PATTERN = "filePattern";
    private static final String FOLDER = "folder";
    private static final String FILE_MATCHER = "fileMatcher";
    private static final String DEFAULT_RESPONSE = "<h1>Hello World!</h1>";
    protected final Map<HeaderOption, String> headers = new HashMap<>();
    protected final Map<String, Object> options;
    private final String internalName;
    private final UUID uuid;
    private final ViewPattern viewPattern;
    private final HttpMethod httpMethod;
    public String relatedFolderPath;
    protected boolean forceHTTPS;
    protected String defaultFilePattern = "${file}.${extension}";
    private Path folder;
    private int buffer = -1;
    private ViewReturn viewReturn;
    private FilePattern filePattern;
    @Getter @Setter private Supplier<FileSystem> fileSystemSupplier =
        ServerImplementation.getImplementation()::getFileSystem;

    /**
     * Delegate for {@link #View(String, String, HttpMethod)} with httpMethod = {@link HttpMethod#ALL}
     *
     * @param pattern      View pattern ({@link ViewPattern})
     * @param internalName A unique internal name
     */
    public View(final String pattern, final String internalName) {
        this(pattern, internalName, HttpMethod.ALL);
    }

    /**
     * Constructor without prestored options
     *
     * @param pattern      View pattern ({@link ViewPattern})
     * @param internalName A unique internal name
     * @param httpMethod   The http method that will be served by this view
     */
    public View(final String pattern, final String internalName, final HttpMethod httpMethod) {
        this(pattern, internalName, null, httpMethod);
    }

    /**
     * Delegate for {@link #View(String, String, Map, ViewReturn, HttpMethod)} with viewReturn = null
     *
     * @param pattern      View pattern ({@link ViewPattern})
     * @param internalName A unique internal name
     * @param options      Pre Stored options (Nullable)
     * @param httpMethod   The http method that this view will serve
     */
    public View(final String pattern, final String internalName, final Map<String, Object> options,
        final HttpMethod httpMethod) {
        this(pattern, internalName, options, null, httpMethod);
    }

    /**
     * Primary constructor
     *
     * @param pattern      View pattern ({@link ViewPattern})
     * @param internalName A unique internal name
     * @param options      Pre Stored options (Nullable)
     * @param viewReturn   {@link ViewReturn} which will be used to generate responses (Nullable)
     * @param httpMethod   The http method that this view will serve
     */
    public View(@NonNull final String pattern, @NonNull final String internalName,
        @Nullable final Map<String, Object> options, @Nullable final ViewReturn viewReturn,
        @NonNull final HttpMethod httpMethod) {
        if (options == null) {
            this.options = new HashMap<>();
        } else {
            this.options = options;
        }
        this.internalName = this.options.getOrDefault(INTERNAL_NAME, internalName).toString();
        this.forceHTTPS = (boolean) this.options.getOrDefault(FORCE_HTTPS, false);
        if (this.options.containsKey(HEADERS)) {
            ((Map<String, String>) this.options.get(HEADERS)).forEach((key, value) -> headers
                .put(HeaderOption.getOrCreate(AsciiString.of(key, false)), value));
        }
        this.viewPattern = new ViewPattern(pattern);
        this.viewReturn = viewReturn;
        this.uuid = UUID.randomUUID();
        if (httpMethod == null) {
            if (options.containsKey(METHOD)) {
                this.httpMethod = HttpMethod.valueOf(options.get(METHOD).toString());
            } else {
                this.httpMethod = HttpMethod.ALL;
            }
        } else {
            this.httpMethod = httpMethod;
        }
    }

    protected FilePattern getFilePattern() {
        if (this.filePattern == null) {
            if (this.options.containsKey(FILE_PATTERN)) {
                this.filePattern = FilePattern.compile(this.options.get(FILE_PATTERN).toString());
            } else {
                this.filePattern = FilePattern.compile(defaultFilePattern);
            }
        }
        return this.filePattern;
    }

    /**
     * Get a stored option
     *
     * @param <T> Type
     * @param s   Key
     * @return (Type Casted) Value
     * @see #containsOption(String) Check if the option exists before getting it
     */
    @SuppressWarnings("ALL") public final <T> T getOption(@NonNull final String s) {
        return ((T) options.get(s));
    }

    /**
     * Get an option, if it exists
     *
     * @param s   Option key
     * @param <T> Option type (Casts to this type)
     * @return Value
     */
    public final <T> Optional<T> getOptionSafe(@NonNull final String s) {
        if (options.containsKey(s)) {
            return Optional.of((T) options.get(s));
        }
        return Optional.empty();
    }

    /**
     * Get all options as a string
     *
     * @return options as string
     */
    @Nonnull public final String getOptionString() {
        final StringBuilder b = new StringBuilder();
        for (final Map.Entry<String, Object> e : options.entrySet()) {
            b.append(';').append(e.getKey()).append('=').append(e.getValue().toString());
        }
        return b.toString();
    }

    /**
     * Check if the option is stored
     *
     * @param s Key
     * @return True if the option is stored, False if it isn't
     */
    public final boolean containsOption(@NonNull final String s) {
        return options.containsKey(s);
    }

    @Nonnull @Override public final String getName() {
        return this.internalName;
    }

    @Override public boolean forceHTTPS() {
        return this.forceHTTPS;
    }

    /**
     * Register the view to the implemented {@link Router}
     */
    final public void register() {
        ServerImplementation.getImplementation().getRouter().add(this);
    }

    /**
     * Get the folder used by this view, doesn't have to be used
     *
     * @return File
     */
    @Nonnull protected Path getFolder() {
        if (this.folder == null) {
            if (containsOption(FOLDER)) {
                this.folder = fileSystemSupplier.get().getPath(getOption(FOLDER).toString());
            } else if (relatedFolderPath != null) {
                this.folder = fileSystemSupplier.get().getPath(relatedFolderPath);
            } else {
                this.folder = fileSystemSupplier.get().getPath(String.format("/%s", internalName));
            }
            if (!folder.exists() && !folder.create()) {
                Message.COULD_NOT_CREATE_FOLDER.log(folder);
            }
        }
        return this.folder;
    }

    /**
     * <p> Get a file from the {@link #getFolder()} folder, based on request variables and the {@link #fileName} or
     * {@link #getOption(String)} "filepattern", pattern </p> <p> This uses {@literal {pattern}}'s: {@literal {file}},
     * {@literal {folder}} and {@literal {extension}} <br>
     * For example: <pre>/{folder}/{filename}}.{extension}</pre>
     * </p>
     *
     * @return The file (use {@link File#exists()}!)
     * @see #PATTERN_VARIABLE_EXTENSION
     * @see #PATTERN_VARIABLE_FILE
     * @see #PATTERN_VARIABLE_FOLDER
     * @see #VARIABLE_EXTENSION
     * @see #VARIABLE_FILE
     * @see #VARIABLE_FOLDER
     * @see #getFolder()
     */
    @Nonnull protected Path getFile(@Nonnull @NonNull final AbstractRequest request) {
        Assert.isValid(request);

        if (request.getMeta(FILE_MATCHER) == null) {
            throw new KvantumException("fileMatcher isn't set");
        }

        final FilePattern.FileMatcher fileMatcher =
            (FilePattern.FileMatcher) request.getMeta(FILE_MATCHER);

        if (!fileMatcher.matches()) {
            throw new KvantumException("getFile called when matches = false");
        }

        if (CoreConfig.debug) {
            ServerImplementation.getImplementation()
                .log("Translated file name: '{}'", fileMatcher.getFileName());
        }

        String fileName = fileMatcher.getFileName();

        if (containsOption(EXTENSION_REWRITE)) {
            if (CoreConfig.debug) {
                Logger.debug("Rewrite found for : " + toString());
            }

            final String variableExtension = request.getVariables().get(VARIABLE_EXTENSION);

            final Map<String, Object> rewrite = getOption(EXTENSION_REWRITE);
            if (rewrite.containsKey(variableExtension)) {
                final String rewritten = rewrite.get(variableExtension).toString();
                if (CoreConfig.debug) {
                    Logger.debug("Rewrote {} to {}", variableExtension, rewritten);
                }
                fileName = fileName.replace(variableExtension, rewritten);
            }

        }

        if (CoreConfig.debug) {
            Logger.debug("Final file name: " + fileName);
        }

        return getFolder().getPath(fileName);
    }

    /**
     * Get the file buffer (if needed)
     *
     * @return file buffer
     */
    protected final int getBuffer() {
        if (this.buffer == -1) {
            if (containsOption(CONSTANT_BUFFER)) {
                this.buffer = getOption(CONSTANT_BUFFER);
            } else {
                this.buffer = 65536; // 64kb
            }
        }
        return this.buffer;
    }

    /**
     * Check if the request URL matches the regex pattern
     *
     * @param request Request, from which the URL should be checked
     * @return True if the request Matches, False if not
     * @see #passes(AbstractRequest) - This is called!
     */
    @Override final public boolean matches(@Nonnull @NonNull final AbstractRequest request) {
        Assert.isValid(request);

        final HttpMethod requestMethod = request.getQuery().getMethod();
        if (this.httpMethod != HttpMethod.ALL && this.httpMethod != requestMethod) {
            if (CoreConfig.debug) {
                Logger.debug("Invalid http method {0}, expected {1} for request {2} in handler {3}",
                    requestMethod, this.httpMethod, request, this);
            }
            return false;
        }

        final Map<String, String> map = viewPattern.matches(request.getQuery().getFullRequest());
        if (map != null) {
            request.addMeta(CONSTANT_VARIABLES, map);
        }

        if (CoreConfig.debug && map == null) {
            ServerImplementation.getImplementation().log("Request: '{0}' failed to " + "pass '{1}'",
                request.getQuery().getFullRequest(), viewPattern.toString());
        }

        return map != null && passes(request);
    }

    /**
     * This is for further testing (... further than regex...) For example, check if a file exists etc.
     *
     * @param request The request from which the URL is fetches
     * @return True if the request matches, false if not
     */
    protected boolean passes(@Nullable final AbstractRequest request) {
        return true;
    }

    @Nonnull @Override public String toString() {
        return String.format("{name:%s,uuid:%s,pattern:%s}", internalName, uuid.toString(),
            viewPattern.toString());
    }

    /**
     * Generate a response
     *
     * @param request Incoming request
     * @return Either the view generated by the configured view return, or a generated response.
     */
    @Nonnull @Override public Response generate(@Nonnull @NonNull final AbstractRequest request) {
        if (viewReturn != null) {
            return viewReturn.get(request);
        } else {
            final Response response = new Response(this);
            this.applyDefaultHeaders(response);
            this.handle(request, response);
            return response;
        }
    }

    /**
     * Apply default headers
     *
     * @param response Working response
     */
    protected final void applyDefaultHeaders(@Nonnull @NonNull final Response response) {
        if (!headers.isEmpty()) {
            headers.forEach((header, value) -> response.getHeader().set(header, value));
        }
    }

    /**
     * OVERRIDE ME
     *
     * @param request  Incoming request
     * @param response Working response
     */
    protected void handle(@Nullable final AbstractRequest request,
        @Nonnull @NonNull final Response response) {
        response.setResponse(DEFAULT_RESPONSE);
    }

    /**
     * Set an internal option
     *
     * @param key   Option key
     * @param value Option value
     */
    public void setOption(@Nonnull @NonNull final String key,
        @Nonnull @NonNull final Object value) {
        this.options.put(key, value);
    }

}

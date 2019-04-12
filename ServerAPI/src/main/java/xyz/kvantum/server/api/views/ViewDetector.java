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

import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.util.MapBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Can be used to detect file structures that could be served by the standard library of {@link View views}
 */
@SuppressWarnings("unused") public final class ViewDetector {

    private final String basePath;
    private final Collection<String> ignore;
    private final Path basePathObject;
    private final Set<Path> paths = new HashSet<>();
    private final Map<String, Map<String, Object>> viewEntries = new HashMap<>();

    public ViewDetector(final String basePath, final Path basePathObject,
        final Collection<String> ignore) {
        this.basePath = basePath;
        this.ignore = ignore;
        this.basePathObject = basePathObject;
    }

    public int loadPaths() {
        this.paths.add(this.basePathObject);
        this.addSubPaths(this.basePathObject);
        return this.paths.size();
    }

    public Collection<Path> getPaths() {
        return new ArrayList<>(this.paths);
    }

    public Map<String, Map<String, Object>> getViewEntries() {
        return new HashMap<>(this.viewEntries);
    }

    public void generateViewEntries() {
        this.paths.forEach(p -> loadSubPath(viewEntries, basePath, basePathObject.toString(), p));
    }

    private void loadSubPath(final Map<String, Map<String, Object>> viewEntries,
        final String basePath, final String toRemove, final Path path) {
        String extension = null;
        boolean moreThanOneType = false;
        boolean hasIndex = false;
        String indexExtension = "";

        for (final Path subPath : path.getSubPaths(false)) {
            if (extension == null) {
                extension = subPath.getExtension();
            } else if (!extension.equalsIgnoreCase(subPath.getExtension())) {
                moreThanOneType = true;
            }
            if (!hasIndex) {
                hasIndex = subPath.getEntityName().equals("index");
                indexExtension = subPath.getExtension();
            }
        }

        if (extension == null) {
            return;
        }

        final String type;
        if (moreThanOneType) {
            type = "std";
        } else {
            switch (extension) {
                case "html":
                    type = "html";
                    break;
                case "js":
                    type = "javascript";
                    break;
                case "css":
                    type = "css";
                    break;
                case "png":
                case "jpg":
                case "jpeg":
                case "ico":
                    type = "img";
                    break;
                case "zip":
                case "txt":
                case "pdf":
                    type = "download";
                    break;
                default:
                    type = "std";
                    break;
            }
        }

        final String folder = "./" + path.toString();
        final String viewPattern;
        if (moreThanOneType) {
            if (hasIndex) {
                viewPattern =
                    (path.toString().replace(toRemove, basePath)) + "[file=index].[extension="
                        + indexExtension + "]";
            } else {
                viewPattern = (path.toString().replace(toRemove, basePath)) + "<file>.<extension>";
            }
        } else {
            if (hasIndex) {
                viewPattern =
                    (path.toString().replace(toRemove, basePath)) + "[file=index].[extension="
                        + indexExtension + "]";
            } else {
                viewPattern = (path.toString().replace(toRemove, basePath)) + "<file>." + extension;
            }
        }

        final Map<String, Object> info =
            MapBuilder.<String, Object>newHashMap().put("filter", viewPattern)
                .put("options", MapBuilder.newHashMap().put("folder", folder).get())
                .put("type", type).get();

        viewEntries.put(UUID.randomUUID().toString(), info);
    }

    private void addSubPaths(final Path path) {
        for (final Path subPath : path.getSubPaths()) {
            if (!subPath.isFolder() || ignore.contains(subPath.getEntityName())) {
                continue;
            }
            paths.add(subPath);
            addSubPaths(subPath);
        }
    }

}

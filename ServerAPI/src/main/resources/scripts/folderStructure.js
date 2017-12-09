/*
 *    Copyright (C) 2017 IntellectualSites
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
var Logger = Java.type('xyz.kvantum.server.api.logging.Logger');

var folderString = options.getOrDefault("folder", "public");
var folder = Kvantum.getFileSystem().getPath(folderString);
var prefix = options.getOrDefault("replace", "");

var content = "Available Files:<br/><ul>";

folder.getSubPaths().forEach(function (path) {
    var name = prefix + path.getEntityName();
    content += "<li><a href='" + name + "'>" + name + "</a></li>";
});

content += "</ul><br/>Session: " + request.getSession().get("id");

response.setContent(content);

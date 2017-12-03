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

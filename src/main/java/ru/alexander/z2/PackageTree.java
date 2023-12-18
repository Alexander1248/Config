package ru.alexander.z2;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageTree {
    private final static String levelStr = "  ";
    private final String packageName;
    private final List<PackageTree> dependencies;
    public PackageTree(String packageName, Map<String, PackageTree> hash, int level, int maxLevel) throws IOException, InterruptedException {
        this.packageName = packageName;
        System.out.println(levelStr.repeat(level) + packageName);
        hash.put(packageName, this);
        if (maxLevel != -1 && level > maxLevel){
            dependencies = null;
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(
                URI.create("https://pypi.org/pypi/" + packageName + "/json")
        ).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            client.close();
            dependencies = null;
            return;
        }
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        if (json.get("message") != null) {
            client.close();
            dependencies = null;
            return;
        }

        JsonElement array = json.getAsJsonObject("info").get("requires_dist");
        if (array.isJsonNull())  {
            client.close();
            dependencies = null;
            return;
        }
        dependencies = new ArrayList<>();
        Pattern pattern = Pattern.compile("^[a-z0-9]+", Pattern.CASE_INSENSITIVE);
        for (JsonElement element : array.getAsJsonArray()) {
            String name = element.getAsString();
            Matcher matcher = pattern.matcher(name);
            if (matcher.find())
                name = name.substring(matcher.start(), matcher.end());
            if (!hash.containsKey(name))
                hash.put(name, new PackageTree(name, hash, level + 1, maxLevel));
            dependencies.add(hash.get(name));
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public List<PackageTree> getDependencies() {
        return dependencies;
    }


    public void print(int level) {
        System.out.println(levelStr.repeat(level) + packageName);
        for (PackageTree dependency : dependencies)
            dependency.print(level + 1);
    }
}

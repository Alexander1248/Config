package ru.alexander.z5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class GitTree {
    private final Map<String, Commit> commits = new HashMap<>();
    private final Map<String, List<String[]>> trees = new HashMap<>();
    public GitTree(String path) throws IOException {
        File git = new File(path, ".git");
        if (!git.exists()) return;
        List<File> files = new ArrayList<>();
        deepSearch(files, new File(git, "objects"));
        files.removeIf(file -> file.getAbsolutePath().contains("\\pack\\"));
        files.removeIf(file -> file.getAbsolutePath().contains("\\info\\"));
        for (File file : files) {
            FileInputStream fis = new FileInputStream(file);
            InflaterInputStream inStream = new InflaterInputStream(fis);
            int i;
            StringBuilder type = new StringBuilder();
            while ((i = inStream.read()) != 0) {
                type.append((char) i);
            }

            String p = file.getAbsolutePath();
            int index = p.lastIndexOf("\\", p.lastIndexOf("\\") - 1);
            String nodeHash = p.substring(index + 1).replace("\\", "");
            if (type.toString().startsWith("blob")) continue;
            if (type.toString().startsWith("tree")) {
                List<String[]> data = new ArrayList<>();

                int len = Integer.parseInt(type.toString().replace("tree ", ""));
                while (len > 0) {
                    StringBuilder nodeType = new StringBuilder();
                    while((i = inStream.read()) != 0x20){
                        nodeType.append((char) i);
                        len--;
                    }

                    StringBuilder filename = new StringBuilder();
                    while((i = inStream.read()) != 0){
                        filename.append((char) i);
                        len--;
                    }

                    StringBuilder hash = new StringBuilder();
                    for(int count = 0; count < 20 ; count++){
                        i = inStream.read();
                        String str = Integer.toHexString(i);
                        if (str.length() == 1) str = "0" + str;
                        hash.append(str);
                        len--;
                    }
                    len -= 2;

                    data.add(new String[]{
                            nodeType.toString(),
                            filename.toString(),
                            hash.toString()
                    });
                }
                trees.put(nodeHash, data);
                continue;
            }
            String data = new String(inStream.readAllBytes());

            String parent = null, h = null;
            while (true) {
                if (data.startsWith("tree ")) {
                    i = data.indexOf("\n");
                    h = data.substring(0, i).replace("tree ", "");
                    data = data.substring(i + 1);
                }
                else if (data.startsWith("parent ")) {
                    i = data.indexOf("\n");
                    parent = data.substring(0, i).replace("parent ", "");
                    data = data.substring(i + 1);
                }
                else if (data.startsWith("author ") || data.startsWith("committer ")) {
                    i = data.indexOf("\n");
                    data = data.substring(i + 1);
                }
                else break;
            }
            commits.put(nodeHash, new Commit(parent, h, data));
        }
    }

    public Map<String, Commit> getCommits() {
        return commits;
    }

    public Map<String, List<String[]>> getTrees() {
        return trees;
    }

    private void deepSearch(List<File> output, File file) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) deepSearch(output, files[i]);
            else output.add(files[i]);
        }
    }

    public record Commit(String parent, String tree, String description) {}
}

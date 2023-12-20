package ru.alexander.z5;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {
//    D:\Projects\JavaProjects\NeuralEngine
//    D:\Projects\JavaProjects\BodyTreckering
//
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter git project path: ");
        GitTree gitTree = new GitTree(scanner.nextLine());

        System.out.print("Visualize directories?(Y/N) ");
        boolean withDirectories = scanner.nextLine().equalsIgnoreCase("y");
        boolean withFiles;
        if (withDirectories) {
            System.out.print("Visualize files?(Y/N) ");
            withFiles = scanner.nextLine().equalsIgnoreCase("y");
        }
        else withFiles = false;

        System.out.println();

        Map<String, GitTree.Commit> commits = gitTree.getCommits();
        Map<String, List<String[]>> trees = gitTree.getTrees();
        Map<String, Integer> hashes = new HashMap<>();

        StringBuilder msg = new StringBuilder("digraph G {\n");

        for (Map.Entry<String, GitTree.Commit> entry : commits.entrySet()) {
            GitTree.Commit value = entry.getValue();
            if (value != null) {
                String currentNode = getNode(msg, hashes, entry.getKey(), "goldenrod", "white",
                        "commit " + entry.getKey().substring(0, 7) + "\\n" + getCommitLabel(value));

                if (withDirectories) {
                    List<String[]> tree = trees.get(value.tree());
                    buildTree(tree, trees, msg, hashes, currentNode, withFiles);
                }

                String parentCommit = value.parent();
                if (parentCommit == null) continue;
                String parentNode;
                if (!hashes.containsKey(parentCommit)) {
                    parentNode = getNode(msg, hashes, parentCommit, "goldenrod", "white", "commit "
                            + parentCommit.substring(0, 7) + "\\n" + getCommitLabel(commits.get(parentCommit)));
                } else parentNode = getNode(msg, hashes, parentCommit, "", "", "");

                msg.append(parentNode).append(" -> ").append(currentNode).append("[color=\"goldenrod4\"]").append("\n");
            }

        }

        msg.append("}");


        System.out.println(msg);
    }

    private static void buildTree(List<String[]> tree,
                                  Map<String, List<String[]>> trees,
                                  StringBuilder msg,
                                  Map<String, Integer> hashes,
                                  String currentNode,
                                  boolean withFiles) {
        if (tree == null) return;
        for (String[] element : tree) {
            switch (element[0]) {
                case "40000" -> {
                    String node = getNode(msg, hashes, element[2], "aquamarine3", "white",
                            "tree " + element[2].substring(0, 7) + "\\n" + element[1]);

                    String lineCode = currentNode + " -> " + node;
                    if (!msg.toString().contains(lineCode))
                        msg.append(lineCode).append("[color=\"aquamarine4\"]").append("\n");

                    List<String[]> list = trees.get(element[2]);
                    buildTree(list, trees, msg, hashes, node, withFiles);
                }
                case "100644" -> {
                    if (withFiles) {
                        String node = getNode(msg, hashes, element[2], "gray93", "black", element[1]);

                        String lineCode = currentNode + " -> " + node;
                        if (!msg.toString().contains(lineCode))
                            msg.append(lineCode).append("[color=\"gray70\"]").append("\n");
                    }
                }
            }
        }
    }

    private static String getCommitLabel(GitTree.Commit value) {
        if (value == null) return "";

        String shortDesc = value.description();
        shortDesc = shortDesc.replace("\"", "'");
        while (shortDesc.startsWith("\n") || shortDesc.startsWith(" "))
            shortDesc = shortDesc.substring(1);

        while (shortDesc.endsWith("\n") || shortDesc.endsWith(" "))
            shortDesc = shortDesc.substring(0, shortDesc.length() - 1);

        while (shortDesc.startsWith("gpgsig -----BEGIN PGP SIGNATURE----")) {
            shortDesc = shortDesc.substring(shortDesc.indexOf("-----END PGP SIGNATURE-----") + 27);

            while (shortDesc.startsWith("\n") || shortDesc.startsWith(" "))
                shortDesc = shortDesc.substring(1);

            while (shortDesc.endsWith("\n") || shortDesc.endsWith(" "))
                shortDesc = shortDesc.substring(0, shortDesc.length() - 1);
        }

        int len = 30;
        if (shortDesc.length() > len) {
            int spaceIndex = shortDesc.indexOf(" ", len);
            if (spaceIndex == -1) spaceIndex = shortDesc.length();
            shortDesc = shortDesc.substring(0, spaceIndex) + "...";
        }
        return shortDesc;
    }

    private static int index = 0;
    private static String getNode(StringBuilder msg, Map<String, Integer> hashes,
                                  String hash, String bgColor, String fontColor, String label) {
        if (!hashes.containsKey(hash)) {
            msg.append("n").append(index)
                    .append("[shape=\"rect\", style=\"filled, rounded\", fillcolor=\"")
                    .append(bgColor).append("\", fontcolor=\"")
                    .append(fontColor).append("\", label=\"")
                    .append(label).append("\"]\n");
            hashes.put(hash, index);
            index++;
        }
        return "n" + hashes.get(hash);
    }
}

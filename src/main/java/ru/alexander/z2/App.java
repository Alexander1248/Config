package ru.alexander.z2;

import java.io.IOException;
import java.util.*;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter package name path: ");
        String name = scanner.nextLine();
        System.out.print("Max depth of search: ");
        int depth = scanner.nextInt();

        PackageTree packageTree = new PackageTree(name, new HashMap<>(), 0, depth);

        StringBuilder str = new StringBuilder("digraph G {\n");
        Queue<PackageTree> queue = new LinkedList<>();
        Set<PackageTree> completed = new HashSet<>();
        queue.add(packageTree);
        while (!queue.isEmpty()) {
            PackageTree tree = queue.poll();
            if (completed.contains(tree)) continue;
            completed.add(tree);
            if (tree.getDependencies() == null) continue;

            String colorAppend = "[color=\""
                    + Math.random() + " "
                    + Math.sqrt(Math.random()) + " "
                    + Math.sqrt(Math.random()) + "\"]";
            for (PackageTree dependency : tree.getDependencies()) {
                String lineCode = tree.getPackageName() + " -> " + dependency.getPackageName();
                if (!str.toString().contains(lineCode))
                    str.append(lineCode).append(colorAppend).append("\n");
                queue.add(dependency);
            }
        }
        str.append("}");
        System.out.println();
        System.out.println(str);
    }
}

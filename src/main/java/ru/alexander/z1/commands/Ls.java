package ru.alexander.z1.commands;

import ru.alexander.z1.Command;
import ru.alexander.z1.VirtualEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Ls implements Command {
    @Override
    public String execute(VirtualEnvironment env, String command, String args) {
        try {
            StringBuilder builder = new StringBuilder();
            int pos = args.lastIndexOf(" ");
            String additional = args.substring(pos + 1);
            if (pos == -1) {
                pos = args.length();
                additional = "";
            }

            String lsPath = args.substring(0, pos);
            if (additional.equals("-d")) {
                recursiveLS(builder, env, "", 0);
                return builder.toString();
            }
            else {
                List<Path> list = env.list(lsPath);
                if (list == null) throw new IOException();

                for (Path path : list) {
                    builder.append("| ").append(env.getFileAccessLevel(path.toString()))
                            .append(" - ").append(path.getFileName()).append("\n");
                }
                return builder.toString();
            }
        } catch (IOException e) {
            return "Dir not found!\n";
        }
    }

    private void recursiveLS(StringBuilder builder, VirtualEnvironment env, String p, int level) throws IOException {
        List<Path> list = env.list(p);
        for (Path path : list) {
            builder.append("\t".repeat(level)).append("| ").append(env.getFileAccessLevel(path.toString()))
                    .append(" - ").append(path.getFileName()).append("\n");
            if (Files.isDirectory(path))
                recursiveLS(builder, env, path.toString(), level + 1);
        }
    }

    @Override
    public String recover(VirtualEnvironment env, String command, String args, String recoverData) {
        return "";
    }
}

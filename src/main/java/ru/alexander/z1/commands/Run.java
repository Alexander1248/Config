package ru.alexander.z1.commands;

import ru.alexander.z1.AccessLevel;
import ru.alexander.z1.Command;
import ru.alexander.z1.VirtualEnvironment;

import java.io.IOException;
import java.io.InputStream;

public class Run implements Command {
    @Override
    public String execute(VirtualEnvironment env, String command, String args) {
        AccessLevel systemState = env.level;
        env.level = env.getFileAccessLevel(args);

        StringBuilder str = new StringBuilder();
        try {
            InputStream stream = env.getInputStream(args);
            String[] codeLines = new String(stream.readAllBytes()).replace("\r", "").split("\n");
            stream.close();
            for (int i = 0; i < codeLines.length; i++)
                str.append(env.executeCommand(codeLines[i])).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        env.level = systemState;
        return str.toString();
    }

    @Override
    public String recover(VirtualEnvironment env, String command, String args, String recoverData) {
        return "";
    }
}

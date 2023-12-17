package ru.alexander.z1.commands;

import ru.alexander.z1.AccessLevel;
import ru.alexander.z1.Command;
import ru.alexander.z1.VirtualEnvironment;

import java.io.IOException;

public class Delete implements Command {
    @Override
    public String execute(VirtualEnvironment env, String command, String args) {
        try {
            if (args.isEmpty()) {
                return "Empty file name!\n";
            }
            if (args.contains("/")) {
                return "Hierarchy symbol in file name!\n";
            }
            env.deleteFromFS(args, env.level != AccessLevel.BOOTLOADER);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.flush();
        }
        return "";
    }

    @Override
    public String recover(VirtualEnvironment env, String command, String args, String recoverData) {
        return "";
    }
}

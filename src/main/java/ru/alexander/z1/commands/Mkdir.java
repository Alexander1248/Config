package ru.alexander.z1.commands;

import ru.alexander.z1.AccessLevel;
import ru.alexander.z1.Command;
import ru.alexander.z1.VirtualEnvironment;

import java.io.IOException;

public class Mkdir implements Command {
    @Override
    public String execute(VirtualEnvironment env, String command, String args) {
        try {
            if (args.isEmpty()) {
                return "Empty dir name!";
            }
            env.createDir(args, env.level != AccessLevel.BOOTLOADER);
            return "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String recover(VirtualEnvironment env, String command, String args, String recoverData) {
        return "";
    }
}

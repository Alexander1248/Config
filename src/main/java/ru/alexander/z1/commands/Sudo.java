package ru.alexander.z1.commands;

import ru.alexander.z1.AccessLevel;
import ru.alexander.z1.Command;
import ru.alexander.z1.VirtualEnvironment;

public class Sudo implements Command {
    @Override
    public String execute(VirtualEnvironment env, String command, String args) {
        if (args.equalsIgnoreCase("user")) {
            env.level = AccessLevel.USER;
        } else {
            env.level = AccessLevel.ADMIN;
        }
        return "";
    }

    @Override
    public String recover(VirtualEnvironment env, String command, String args, String recoverData) {
        return "";
    }
}

package ru.alexander.z1.commands;

import ru.alexander.z1.Command;
import ru.alexander.z1.VirtualEnvironment;

public class Cd implements Command {
    @Override
    public String execute(VirtualEnvironment env, String command, String args) {
        if (args.startsWith("..")) {
            int index = env.path.lastIndexOf("/", 1);
            if (index < 0) index = env.path.length();
            else index++;
            env.path = env.path.substring(0, index);
            if (args.startsWith("../")) {
                env.path += args.substring(2);
            }
        }
        else {
            if (!args.endsWith("/")) args += "/";
            if (args.startsWith("/")) {
                if (env.existsInFS(args))
                    env.path = args;
                else return "Dir not found!\n";
            }
            else {
                if (env.existsInFS(env.path + args))
                    env.path += args;
                else return "Dir not found!\n";
            }
        }
        return "";
    }

    @Override
    public String recover(VirtualEnvironment env, String command, String args, String recoverData) {
        return "";
    }
}

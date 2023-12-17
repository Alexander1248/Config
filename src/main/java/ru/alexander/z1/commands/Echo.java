package ru.alexander.z1.commands;

import ru.alexander.z1.Command;
import ru.alexander.z1.VirtualEnvironment;

public class Echo implements Command {
    @Override
    public String execute(VirtualEnvironment env, String command, String args) {
        return args + "\n";
    }

    @Override
    public String recover(VirtualEnvironment env, String command, String args, String recoverData) {
        return "";
    }
}

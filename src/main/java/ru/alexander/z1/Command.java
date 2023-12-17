package ru.alexander.z1;

public interface Command {
    String execute(VirtualEnvironment env, String command, String args);
    String recover(VirtualEnvironment env, String command, String args, String recoverData);
}

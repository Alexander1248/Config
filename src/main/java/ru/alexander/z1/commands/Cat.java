package ru.alexander.z1.commands;

import ru.alexander.z1.Command;
import ru.alexander.z1.VirtualEnvironment;

import java.io.IOException;
import java.io.InputStream;

public class Cat implements Command {
    @Override
    public String execute(VirtualEnvironment env, String command, String args) {
        String out;
        try {
            InputStream stream = env.getInputStream(args);
            out = new String(stream.readAllBytes());
            stream.close();
        } catch (IOException e) {
            out = "File not found!\n";
        }
        return out;
    }

    @Override
    public String recover(VirtualEnvironment env, String command, String args, String recoverData) {
        return "";
    }
}

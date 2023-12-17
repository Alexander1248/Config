package ru.alexander.z1.commands;

import ru.alexander.z1.AccessLevel;
import ru.alexander.z1.Command;
import ru.alexander.z1.VirtualEnvironment;

import java.io.IOException;

public class Access implements Command {
    @Override
    public String execute(VirtualEnvironment env, String command, String args) {
        int pos = args.lastIndexOf(" ");
        String additional = args.substring(pos + 1);
        if (pos == -1) {
            pos = args.length();
            additional = "";
        }

        String lsPath = args.substring(0, pos);
        try {
            env.changeAccessLevel(lsPath,
                    AccessLevel.valueOf(additional.toUpperCase()),
                    env.level != AccessLevel.BOOTLOADER);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.flush();
        } catch (IllegalArgumentException e) {
            System.err.println("Access level with this name not exists!");
            System.err.flush();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Wrong command format!");
            System.err.flush();
        }
        return "";
    }

    @Override
    public String recover(VirtualEnvironment env, String command, String args, String recoverData) {
        return "";
    }
}

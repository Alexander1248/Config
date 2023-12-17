package ru.alexander.z1.commands;

import ru.alexander.z1.AccessLevel;
import ru.alexander.z1.Command;
import ru.alexander.z1.VirtualEnvironment;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


public class Write implements Command {
    @Override
    public String execute(VirtualEnvironment env, String command, String args) {
        try {
            if (args.isEmpty()) {
                return "Empty file name!\n";
            }
            if (!env.existsInFS(args)) {
                return "File not found!\n";
            }

            int pos = args.lastIndexOf(" ");
            String additional = args.substring(pos + 1);
            if (pos == -1) {
                pos = args.length();
                additional = "";
            }

            String lsPath = args.substring(0, pos);
            if (!additional.equals("-e"))
                env.deleteFromFS(lsPath, env.level != AccessLevel.BOOTLOADER);

            PrintWriter writer = new PrintWriter(env.getOutputStream(lsPath));
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals("!exit")) break;
                writer.println(line);
            }
            writer.close();
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

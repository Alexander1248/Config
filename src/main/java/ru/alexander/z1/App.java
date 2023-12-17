package ru.alexander.z1;

import java.io.IOException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter zip file path: ");
        VirtualEnvironment env = new VirtualEnvironment(scanner.nextLine());

        String command;
        while (true) {
            System.out.print(env.level.name() + " " + env.path + "> ");
            if (!scanner.hasNextLine()) break;
            command = scanner.nextLine();
            System.out.print(env.executeCommand(command));
            if (env.isClosing()) return;
        }
    }
}

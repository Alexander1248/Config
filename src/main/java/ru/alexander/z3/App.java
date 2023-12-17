package ru.alexander.z3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException {
        SToJSON sToJSON = new SToJSON(true);
        Scanner scanner = new Scanner(System.in);
        System.out.println("S-Expression file name: ");
        File sExprFile = new File(scanner.nextLine());
        System.out.println("JSON file name: ");
        File jsonFile = new File(scanner.nextLine());
        if (sExprFile.exists()) {
            FileInputStream fis = new FileInputStream(sExprFile);
            String sExpr = new String(fis.readAllBytes());
            fis.close();

            String json = sToJSON.transform(sExpr);

            FileOutputStream fos = new FileOutputStream(jsonFile);
            fos.write(json.getBytes());
            fos.close();
        }
        else {
            System.out.println("S-Expression file not exists!");
        }
    }
}

package ru.alexander.csc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class App {
    public static void main(String[] args) throws IOException {
        ColorSpaceCalculator csc = new ColorSpaceCalculator(
                new ColorSpaceCalculator.Color("red", 1, 0, 0),
                new ColorSpaceCalculator.Color("blue", 0, 0, 1),
                new ColorSpaceCalculator.Color("yellow", 1, 1, 0),
                new ColorSpaceCalculator.Color("white", 1, 1, 1),
                new ColorSpaceCalculator.Color("black", 0, 0, 0)
        );
        csc.addColorMixingEquation(1, 0.5, 0.5,
                0.5, 0, 0, 0.5, 0); // Pink
        csc.addColorMixingEquation(0.5, 1, 1,
                0, 0.5, 0, 0.5, 0); // Light blue
        csc.addColorMixingEquation(1, 1, 0.5,
                0, 0, 0.5, 0.5, 0); // Sand

        csc.addColorMixingEquation(0.4, 0, 0,
                0.5, 0, 0, 0, 0.5); // Dark red
        csc.addColorMixingEquation(0, 0, 0.4,
                0, 0.5, 0, 0, 0.5); // Dark blue
        csc.addColorMixingEquation(0.4, 0.4, 0,
                0, 0, 0.5, 0, 0.5); // Dark yellow

        csc.addColorMixingEquation(0.97, 0.55, 0.05,
                0.5, 0, 0.5, 0, 0); // Orange
        csc.addColorMixingEquation(0.3, 0.85, 0,
                0, 0.5, 0.5, 0, 0); // Green
        csc.addColorMixingEquation(0.36, 0.11, 0.54,
                0.5, 0.5, 0, 0, 0); // Purple

        csc.computeColors(1000000, 0.3);

        ColorSpaceCalculator.Color red = csc.getBaseColor(0);
        ColorSpaceCalculator.Color blue = csc.getBaseColor(1);
        ColorSpaceCalculator.Color yellow = csc.getBaseColor(2);
        ColorSpaceCalculator.Color white = csc.getBaseColor(3);
        ColorSpaceCalculator.Color black = csc.getBaseColor(4);
        System.out.println(red);
        System.out.println(blue);
        System.out.println(yellow);
        System.out.println(white);
        System.out.println(black);
        System.out.println();

        System.out.println(red.transformToNormal());
        System.out.println(blue.transformToNormal());
        System.out.println(yellow.transformToNormal());
        System.out.println(white.transformToNormal());
        System.out.println(black.transformToNormal());
        System.out.println();

        System.out.println(Arrays.toString(new double[]{red.getR(), red.getG(), red.getB()}));
        System.out.println(Arrays.toString(new double[]{blue.getR(), blue.getG(), blue.getB()}));
        System.out.println(Arrays.toString(new double[]{yellow.getR(), yellow.getG(), yellow.getB()}));
        System.out.println(Arrays.toString(new double[]{white.getR(), white.getG(), white.getB()}));
        System.out.println(Arrays.toString(new double[]{black.getR(), black.getG(), black.getB()}));
        System.out.println();

        double[] coefs = csc.transform(new ColorSpaceCalculator.Color(0.4, 0.83, 0.81), 10, 0.001);
        System.out.println(Arrays.toString(coefs));
        ColorSpaceCalculator.Color recalc = csc.mixColors("red", coefs);
        System.out.println(recalc);
        System.out.println(recalc.transformToNormal());
        System.out.println();

        coefs = csc.transform(new ColorSpaceCalculator.Color(0.57, 0.13, 0.21), 10, 0.001);
        System.out.println(Arrays.toString(coefs));
        recalc = csc.mixColors("purple", coefs);
        System.out.println(recalc);
        System.out.println(recalc.transformToNormal());
        System.out.println();


        int size = 30;
        int w = size * size * size;
        int h = size * size;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        for (int y = 0; y < h; y++) {
            double fy0 = (double) y / size;
            fy0 -= (int) fy0;
            double fy1 = (double) (y / size) / size;
            for (int x = 0; x < w; x++) {
                double fx0 = (double) x / size;
                fx0 -= (int) fx0;
                double fx1 = (double) (x / size) / size;
                fx1 -= (int) fx1;
                double fx2 = (double) (x / h) / size;

                ColorSpaceCalculator.Color col = csc.mixColors("col", fx0, fy0, fx2, fx1, fy1).transformToNormal();
                img.setRGB(x, h - 1 - y, new Color(
                        (float) col.getR(), (float) col.getG(), (float) col.getB()
                ).getRGB());
            }
        }
        ImageIO.write(img, "png", new File("test.png"));
    }
}

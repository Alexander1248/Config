package ru.alexander.csc;

import java.util.*;

public class ColorSpaceCalculator {
    private Color[] baseColors;

    private List<Equation> mixingEquations = new ArrayList<>();


    public ColorSpaceCalculator(Color... baseColors) {
        this.baseColors = baseColors;
    }

    public void addColorMixingEquation(Equation equation) {
        mixingEquations.add(equation);
    }

    public void addColorMixingEquation(double rR, double rG, double rB, double... w) {
        mixingEquations.add(new Equation(rR, rG, rB, w));
    }

    public boolean computeColors(int steps, double mixingNumber) {
        if (mixingEquations.size() < 4) return false;

        ArrayList<Integer>[] cnn = new ArrayList[baseColors.length];
        for (int i = 0; i < cnn.length; i++)
            cnn[i] = new ArrayList<>();

        for (int i = 0; i < mixingEquations.size(); i++)
            for (int j = 0; j < baseColors.length; j++)
                if (mixingEquations.get(i).w[j] != 0) cnn[j].add(i);


        Random r = new Random();
        int[] eqIndex = new int[baseColors.length];
        int[] pos = new int[baseColors.length];

        iteration:
        for (int i = 0; i < steps; i++) {
            for (int j = 0; j < baseColors.length; j++)
                pos[j] = j;

            for (int j = 0; j < baseColors.length; j++) {
                int c = r.nextInt(baseColors.length);
                int buff = pos[j];
                pos[j] = pos[c];
                pos[c] = buff;
            }

            for (int j = 0; j < baseColors.length; j++) {
                ArrayList<Integer> list = new ArrayList<>(cnn[pos[j]]);
                eqIndex[j] = list.get(r.nextInt(list.size()));
                for (int k = 0; k < j; k++)
                    list.remove((Integer) eqIndex[k]);

                if (list.isEmpty()) {
                    i--;
                    continue iteration;
                }
            }

            for (int j = 0; j < baseColors.length; j++) {
                Equation equation = mixingEquations.get(eqIndex[j]);
                baseColors[pos[j]].mixColor(equation.getColor(baseColors, pos[j]), mixingNumber);
            }
        }
        double err = 0;
        for (Equation equation : mixingEquations) {
            err += Math.pow(equation.getError(baseColors), 2);
        }
        err = Math.sqrt(err / mixingEquations.size());
        System.out.println("Error: " + err);
        return true;
    }

    public Color getBaseColor(int index) {
        return baseColors[index];
    }
    public int getBaseColorCount() {
        return baseColors.length;
    }

    public Color mixColors(String name, double... w) {
        Color color = new Color(name);
        double sum = 0;
        for (int i = 0; i < baseColors.length; i++) sum += w[i];

        for (int i = 0; i < baseColors.length; i++) {
            color.r += baseColors[i].r * w[i] / sum;
            color.g += baseColors[i].g * w[i] / sum;
            color.b += baseColors[i].b * w[i] / sum;
        }
        return color;
    }

    public double[] transform(Color color, double maxT, double minT) {
        double[] w = new double[baseColors.length];
        double[] ow = new double[w.length];

        Random r = new Random();
        for (int i = 0; i < w.length; i++)
            w[i] = r.nextDouble();

        double t = maxT;
        double it = 1;
        Color col = mixColors("", w);
        double err = Math.sqrt(Math.pow(color.r - col.r, 2)
                + Math.pow(color.g - col.g, 2)
                + Math.pow(color.b - col.b, 2));
        do {
            System.arraycopy(w, 0, ow, 0, w.length);

            for (int i = 0; i < w.length; i++)
                w[i] = Math.max(0, w[i] + r.nextDouble() - 0.5);


            col = mixColors("", w);
            double nErr = Math.sqrt(Math.pow(color.r - col.r, 2)
                    + Math.pow(color.g - col.g, 2)
                    + Math.pow(color.b - col.b, 2));

            if (nErr > err) {
                double prob = Math.exp((err - nErr)/ t);
                if (r.nextDouble() > prob) {
                    double[] buff = ow;
                    ow = w;
                    w = buff;
                }
                else err = nErr;
            }
            else err = nErr;

            t = maxT / it;
            it++;
        } while (t > minT);
        System.out.println("Error: " + err);


        double sum = 0;
        for (int i = 0; i < baseColors.length; i++) sum += w[i];
        for (int i = 0; i < baseColors.length; i++) w[i] /= sum;


        return w;
    }

    public static class Color {
        private final String name;
        private double r;
        private double g;
        private double b;

        public Color(String name) {
            this.name = name;
            r = 0;
            g = 0;
            b = 0;
        }

        public Color(double r, double g, double b) {
            name = "";
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public Color(String name, double r, double g, double b) {
            this.name = name;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public void setColor(Color color) {
            r = color.r;
            g = color.g;
            b = color.b;
        }
        public void mixColor(Color color, double coef) {
            r *= 1 - coef;
            r += color.r * coef;

            g *= 1 - coef;
            g += color.g * coef;

            b *= 1 - coef;
            b += color.b * coef;
        }

        public Color transformToNormal() {
            double r = this.r;
            double g = this.g;
            double b = this.b;

            return new Color(
                    name,
                    Math.max(0, Math.min(1, r)),
                    Math.max(0, Math.min(1, g)),
                    Math.max(0, Math.min(1, b))
            );
        }

        public String getName() {
            return name;
        }

        public double getR() {
            return r;
        }

        public void setR(double r) {
            this.r = r;
        }

        public double getG() {
            return g;
        }

        public void setG(double g) {
            this.g = g;
        }

        public double getB() {
            return b;
        }

        public void setB(double b) {
            this.b = b;
        }

        @Override
        public String toString() {
            return name + " (" + r + ", " + g + ", " + b + ")";
        }
    }

    public record Equation(double rR, double rG, double rB, double... w) {
        public Equation {
            double sum = 0;
            for (int i = 0; i < w.length; i++) sum += w[i];
            for (int i = 0; i < w.length; i++) w[i] /= sum;
        }

        public Color getColor(Color[] colors, int index) {
            double r = rR;
            double g = rG;
            double b = rB;
            for (int i = 0; i < w.length; i++) {
                if (i == index) continue;
                r -= colors[i].r * w[i];
                g -= colors[i].g * w[i];
                b -= colors[i].b * w[i];
            }

            return new Color(
                    r / w[index],
                    g / w[index],
                    b / w[index]
            );
        }

        public double getError(Color[] colors) {
            double dR = -rR;
            double dG = -rG;
            double dB = -rB;
            for (int i = 0; i < w.length; i++) {
                dR += colors[i].r * w[i];
                dG += colors[i].g * w[i];
                dB += colors[i].b * w[i];
            }
            return Math.sqrt(dR * dR + dG * dG + dB * dB);
        }
    }
}

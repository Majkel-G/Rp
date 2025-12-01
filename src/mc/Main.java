/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package mc;

import java.util.*;
import java.io.*;

import org.apache.commons.numbers.gamma.*;
/**
 *
 * @author tom
 *
 */
public class Main {

    private static void validateInput(int d, int n, int repetitions) {
        if (d < 2) {
            throw new IllegalArgumentException("Dimension d must be >= 2, got " + d);
        }
        if (n <= 0) {
            throw new IllegalArgumentException("Sample count n must be > 0, got " + n);
        }
        if (repetitions <= 0) {
            throw new IllegalArgumentException("Repetitions must be > 0, got " + repetitions);
        }
    }

    static class Sphere {
        int d;
        double r;

        public double getVolume() {
            switch(d) {
                case 0:
                    return 1.0;
                case 1:
                    return 2.0 * r;
                default:
                    return 2 * r * Math.PI / d * (new Sphere(d - 2, r)).getVolume();
            }
        }

        public Sphere(int d, double r) {
            this.d = d;
            this.r = r;
        }
    }

    static class DomainSphere extends Domain {
        int d;
        double r;
        RandomLogger rnd;

        @Override
        public Double[] getElement() throws IOException {
            double u[] = new double[d];
            Double x[] = new Double[d];
            double norm = 0.0;
            double c;
            int i;
            for (i = 0; i < d; i++) {
                u[i] = rnd.nextGaussian();
                norm += u[i] * u[i];
            }
            norm = Math.sqrt(norm);
            c = Math.pow(rnd.nextDouble(), 1.0 / d);

            for (i = 0; i < d; i++) {
                x[i] = r * c * u[i] / norm;
            }
            return x;
        }

        public DomainSphere(int d, double r, RandomLogger rnd) {
            this.d = d;
            this.r = r;
            this.rnd = rnd;
        }
    }

    static class FunctionSphere extends Function {
        int d;
        double r;

        @Override
        public Double getValue(Object obj_x) {
            Double x[] = (Double[]) obj_x;
            int i;
            double result = r * r;
            for (i = 0; i < d; i++) {
                result -= x[i] * x[i];
            }
            if (result < 0) {
                System.err.println("WARNING: Inside FunctionSphere.getValue, r^2 - ||x||^2 < 0: " + result +
                        " (d=" + d + ", r=" + r + ")");
                return 0.0;
            }
            return 2 * Math.sqrt(result) / new Sphere(d, r).getVolume();
        }

        public FunctionSphere(int d, double r) {
            this.d = d;
            this.r = r;
        }
    }

    static class PdfSphere extends Function {
        double result;

        @Override
        public Double getValue(Object obj_x) {
            return result;
        }

        public PdfSphere(int d, double r) {
            result = 1.0 / new Sphere(d, r).getVolume();
        }
    }

    static double getCVar2(int j) {
        Sphere sJ = new Sphere(j,1.0);
        Sphere sJ1 = new Sphere(j + 1,1.0);
        return 8.0 * Math.pow(sJ.getVolume(), 2) / ((j + 2.0) * Math.pow(sJ1.getVolume(), 2)) - 1;
    }

    static void printTable() {
        String header = String.format(
                "%-3s | %-22s | %-20s | %-20s | %-20s",
                "j", "CVar2(j)", "V(j)", "V(j+1)/V(j)", "sqrt(pi/(j+1))"
        );
        String line = "-".repeat(header.length());
        System.out.println(line);
        System.out.println(header);
        System.out.println(line);
        for (int j = 1; j < 10; j++) {
            double vj   = Math.pow(Math.PI, j / 2.0) / Gamma.value(j / 2.0 + 1.0);
            double vj1  = Math.pow(Math.PI, (j + 1) / 2.0) / Gamma.value((j + 1) / 2.0 + 1.0);
            double ratio = vj1 / vj;
            double approx = Math.sqrt(Math.PI / (j + 1));
            System.out.printf(
                    "%-3d | %-22.16f | %-20.16f | %-20.16f | %-20.16f%n",
                    j, getCVar2(j), vj, ratio, approx
            );
        }
        System.out.println(line);
    }

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws IOException {
        System.out.print("Režim [1=GENERATE random, 2=REPLAY from log]: ");
        Scanner sc = new Scanner(System.in);
        int modeChoice = sc.nextInt();
        sc.nextLine();
        RandomLogger.Mode rmMode =
                (modeChoice == 2) ? RandomLogger.Mode.REPLAY : RandomLogger.Mode.RANDOM;

        if (args.length < 3) {
            System.err.println("Usage: java mc.Main <d> <n> <repetitions>");
            return;
        }

        double r = 1.0;
        int i, j, k, l;
        int d;
        int n;
        int repetitions;

        d = Integer.parseInt(args[0]);
        n = Integer.parseInt(args[1]);
        repetitions = Integer.parseInt(args[2]);

        try {
            validateInput(d, n, repetitions);
        } catch (IllegalArgumentException e) {
            System.err.println("INPUT ERROR: " + e.getMessage());
            return;
        }

        PrintStream optOut = new PrintStream("opt_" + d + "_" + n + "_" + repetitions + ".txt");
        PrintStream dselOut = new PrintStream("dsel_" + d + "_" + n + "_" + repetitions + ".txt");
        PrintStream uniOut = new PrintStream("uni_" + d + "_" + n + "_" + repetitions + ".txt");

        System.out.print("Name of file you want to read from/ generate randomness: ");
        String rngLogFile= sc.nextLine();

        //String rngLogFile = "rng_" + d + "_" + n + "_" + repetitions + ".txt";
        System.out.println("Random log file: " + rngLogFile);

        RandomLogger rm = new RandomLogger(rmMode, rngLogFile);
        Integrator[] nBallIntegrator = new Integrator[d - 1];
        Stats stats;
        Stats nball_stats;
        Stats[] dsel_stats = new Stats[d - 1];
        int argmax;
        double max;

        printTable();

        for (i = 0; i < d - 1; i++) {
            dsel_stats[i] = new Stats();
        }

        optOut.println("Repetition\tExactVolume\tEstimate\tMeanEstimate\tVariance\tCVar2");
        nball_stats = new Stats();
        for (k = 0; k < repetitions; k++) {
            for (i = 0; i < d - 1; i++) {
                nBallIntegrator[i] = new Integrator(
                        new DomainSphere(i + 1, r, rm),
                        new FunctionSphere(i + 1, r),
                        new PdfSphere(i + 1, r)
                );
            }
            for (int step = 0; step < n * d; step++) {
                int chosen = rm.nextInt(d - 1);
                try {
                    nBallIntegrator[chosen].update();
                    dsel_stats[chosen].update(1.0);
                } catch (IOException e) {
                    System.err.println("ERROR at step " + step + ": " + e.getMessage());
                    throw e;
                }
            }

            double volume = 1.0;
            for (l = 0; l < d - 1; l++) {
                volume *= nBallIntegrator[l].getStats().getAvg();
            }

            nball_stats.update(2.0 * volume);
            optOut.println(k + "\t" + new Sphere(d, r).getVolume() + "\t" + nball_stats.getLast() + "\t" + nball_stats.getAvg() + "\t" + nball_stats.getVar() + "\t" + nball_stats.getCVar2());
        }

        dselOut.println("Dimension\tEstimatedCVar2\tExactCVar2\tActualUpdateShare\tEstimatedFraction\tExactFraction\tSampleCount");
        double cvar2sum = 0.0;
        double exact_cvarsum = 0.0;
        for (i = 0; i < d - 1; i++) {
            double estCVar2 = nBallIntegrator[i].getStats().getCVar2();
            cvar2sum += Math.sqrt(Math.max(estCVar2, 0.0));
            exact_cvarsum += Math.sqrt(Math.max(getCVar2(i + 1), 0.0));
        }
        double sum = 0.0;
        for (i = 0; i < d - 1; i++) {
            sum += d * n * Math.sqrt(Math.max(nBallIntegrator[i].getStats().getCVar2(), 0.0)) / cvar2sum;
        }

        for (i = 0; i < d - 1; i++) {
            double estCvar2 = nBallIntegrator[i].getStats().getCVar2();
            double exactCvar2 = getCVar2(i + 1);

            dselOut.println(i + 1 + "\t" +
                    estCvar2 + "\t" +
                    exactCvar2 + "\t" +
                    (double) dsel_stats[i].getCount() / (d * n) + "\t" +
                    d * n * Math.sqrt(Math.max(estCvar2, 0.0)) / cvar2sum / sum + "\t" +
                    Math.sqrt(Math.max(exactCvar2, 0.0)) / exact_cvarsum + "\t" +
                    dsel_stats[i].getCount());
        }

        uniOut.println("Repetition\tExactVolume\tEstimate\tMeanEstimate\tVariance\tCVar2");
        nball_stats = new Stats();
        for (k = 0; k < repetitions; k++) {
            for (i = 0; i < d - 1; i++) {
                nBallIntegrator[i] = new Integrator(
                        new DomainSphere(i + 1, r, rm),
                        new FunctionSphere(i + 1, r),
                        new PdfSphere(i + 1, r)
                );
            }

            for (i = 0; i < n; i++) {
                for (j = 0; j < d - 1; j++) {
                    try {
                        nBallIntegrator[j].update();
                    } catch (IOException e) {
                        System.err.println("ERROR at iteration " + i + ", dimension " + j + ": " + e.getMessage());
                        throw e;
                    }
                }
            }

            double volume = 1.0;
            for (l = 0; l < d - 1; l++) {
                volume *= nBallIntegrator[l].getStats().getAvg();
            }

            nball_stats.update(2 * volume);

            uniOut.println(k + "\t" + new Sphere(d, r).getVolume() + "\t" + nball_stats.getLast() + "\t" + nball_stats.getAvg() + "\t" + nball_stats.getVar() + "\t" + nball_stats.getCVar2());
        }

        rm.close();

        optOut.close();
        dselOut.close();
        uniOut.close();

        System.out.println("Simulation finished. Results written to files");
    }

}

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
        if (d > 5000) {
            System.err.println("WARNING: d is extremely large, computation may be unstable: d = " + d);
        }
        if (n > 10_000_000) {
            System.err.println("WARNING: n is very large, memory/time may be huge: n = " + n);
        }
    }

    private static void checkFinite(double value, String name, String context) {
        if (!Double.isFinite(value)) {
            System.err.println("ERROR: " + name + " is not finite (" + value + ") in " + context);
        }
    }

    private static void warnIfNegative(double value, String name, String context) {
        if (value < 0) {
            System.err.println("WARNING: " + name + " is negative (" + value + ") in " + context);
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
        Random rnd;

        @Override
        public Double[] getElement() {
            double[] u = new double[d];
            Double[] x = new Double[d];
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

        public DomainSphere(int d, double r, Random rnd) {
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
            Double[] x = (Double[]) obj_x;
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

    static long generateSeed(){
        return System.nanoTime();
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


        if (args.length < 4 || args.length > 5) {
            System.err.println(
                    "Usage: java mc.Main <method> <d> <n> <repetitions> <seed>\n" +
                            "method must be from {rOpt, opt, uni,dsel}");
            return;
        }


        double r = 1.0;
        int i, j, k, l;
        String method = args[0];
        int d, n, repetitions;
        long seed;

        try {
            d = Integer.parseInt(args[1]);
            n = Integer.parseInt(args[2]);
            repetitions = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: d, n, repetitions must be integers.");
            return;
        }

        if (!method.equals("rOpt") && !method.equals("opt") && !method.equals("uni") && !method.equals("dsel") && !method.equals("all")) {
            System.err.println("ERROR: method must be one of {rOpt, opt, uni, all}");
            return;
        }

        if (args.length == 5) {
            try {
                seed = Long.parseLong(args[4]);
            } catch (NumberFormatException e) {
                System.err.println("ERROR: seed must be a long integer.");
                return;
            }
            System.out.println("Using provided seed: " + seed);
        } else {
            seed = generateSeed();
            System.out.println("No seed provided, generated seed: " + seed);
        }
        Random rnd = new Random(seed);

        try {
            validateInput(d, n, repetitions);
        } catch (IllegalArgumentException e) {
            System.err.println("INPUT ERROR: " + e.getMessage());
            return;
        }

        System.out.println("Starting simulation with d=" + d + ", n=" + n + ", repetitions=" + repetitions);

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

        Stats nball_stats2;
        if(method.equals("rOpt") || method.equals("all")) {

            double exact = new Sphere(d, r).getVolume();
            PrintStream logOut = new PrintStream("rOpt_detailed_" + d + "_" + n +  ".txt");
            logOut.println(
                    "UpdateID\tRepetition\tSelectedDimension\tDimensionUpdateCount\tGlobalDimensionUpdateCount\t" +
                            "LocalLastValue\tLocalMean\tLocalStdDev\tLocalVariance\tLocalCVar2\tExactCVar2\t" +
                            "GlobalVolumeEstimate\tProductMeanEstimate\tProductVariance\tProductCVar2\t" +
                            "EstimatedFraction\tExactInDim\t" +
                            "ExactVolume\tSeed"
            );
            long globalUpdateID = 0;

            PrintStream randOptOut = new PrintStream(("rOpt_" + d + "_" + n + "_" + repetitions + ".txt"));
            randOptOut.println("Repetition\tExactVolume\tEstimate\tMeanEstimate\tVariance\tCVar2\tSEED=" + seed);
            nball_stats = new Stats();
            long[] counts= new long[d-1];
            for (k = 0; k < repetitions; k++) {
                nball_stats2=new Stats();
                for (i = 0; i < d - 1; i++) {
                    nBallIntegrator[i] = new Integrator(
                            new DomainSphere(i + 1, r, rnd),
                            new FunctionSphere(i + 1, r),
                            new PdfSphere(i + 1, r)
                    );
                }
                double[] weights = new double[d - 1];

                for (i = 0; i < n * d; i++) {

                    boolean warmup = false;
                    argmax = 0;
                    for (j = 0; j < d - 1; j++) {
                        Stats s = nBallIntegrator[j].getStats();
                        if (s.getCount() < 2 ) {
                            argmax = j;
                            warmup = true;
                            break;
                        }
                    }

                    if (!warmup) {
                        double sumWeights = 0.0;

                        for (j = 0; j < d - 1; j++) {
                            Stats s = nBallIntegrator[j].getStats();
                            long cnt = s.getCount();
                            double cvar2 = s.getCVar2();

                            double w = (cvar2 > 0.0)
                                    ? (cvar2 ) / (cnt * (cnt + 1.0))
                                    : 0.0;

                            weights[j] = w;
                            sumWeights += w;
                        }

                        if (sumWeights == 0.0) {
                            argmax = rnd.nextInt(d - 1);
                        } else {
                            double rrand = rnd.nextDouble(sumWeights);
                            double cumulative = 0.0;

                            for (j = 0; j < d - 1; j++) {
                                cumulative += weights[j];
                                if (rrand <= cumulative) {
                                    argmax = j;
                                    break;
                                }
                            }
                        }
                    }

                    nBallIntegrator[argmax].update();
                    globalUpdateID++;
                    counts[argmax]++;

                    Stats localStats = nBallIntegrator[argmax].getStats();
                    double estCVar2 = localStats.getCVar2();
                    double exactCVar2 = getCVar2(argmax +1);
                    double exactDim= new Sphere(argmax, r).getVolume();
                    /* sumy cez všetky dimenzie */
                    double estSum = 0.0;
                    double exactSum = 0.0;
                    double totalUpdates = 0.0;

                    for (int t = 0; t < d - 1; t++) {
                        double est = nBallIntegrator[t].getStats().getCVar2();
                        double ex = getCVar2(t + 1);

                        estSum += Math.sqrt(Math.max(est, 0.0));
                        exactSum += Math.sqrt(Math.max(ex, 0.0));
                        totalUpdates += dsel_stats[t].getCount();
                    }


                    /* estimated fraction */
                    double estFraction = (estSum > 0.0)
                            ? Math.sqrt(Math.max(estCVar2, 0.0)) / estSum
                            : 0.0;

                    /* exact fraction */
                    double exactFraction = (exactSum > 0.0)
                            ? Math.sqrt(Math.max(exactCVar2, 0.0)) / exactSum
                            : 0.0;

                        double localLast = localStats.getLast();
                        double localMean = localStats.getAvg();
                        double localVar = localStats.getVar();
                        double localStd = Math.sqrt(localVar);
                        double localCvar2 = localStats.getCVar2();

                        long localCount = localStats.getCount();

                        double volume = 1.0;

                        for (int t = 0; t < d - 1; t++) {

                            volume *= nBallIntegrator[t].getStats().getAvg();
                        }

                        double globalEstimate = 2.0 * volume;

                        nball_stats2.update(globalEstimate);

                        double productMean = nball_stats2.getAvg();
                        double productVar = nball_stats2.getVar();
                        double productCvar2 = nball_stats2.getCVar2();

                        /* write log row */
                        logOut.println(
                                globalUpdateID + "\t" +
                                        k + "\t" +
                                        //                                    d + "\t" +
                                        argmax + "\t" +
                                        localCount + "\t" +
                                        counts[argmax] + "\t" +
                                        //                                    globalUpdateID + "\t" +

                                        //                                    localCount + "\t" +
                                        localLast + "\t" +
                                        localMean + "\t" +
                                        localStd + "\t" +
                                        localVar + "\t" +
                                        localCvar2 + "\t" +
                                        exactCVar2 + "\t" +

                                        globalEstimate + "\t" +
                                        productMean + "\t" +
                                        productVar + "\t" +
                                        productCvar2 + "\t" +

                                        estFraction + "\t" +
                                        exactDim + "\t"+
                                        exact + "\t"+
                                        seed
                        );


                    dsel_stats[argmax].update(1.0);
                }
                System.out.println("Repetition: "+k);
                    for(int t=0; t<d-1; t++) {
                        System.out.println("Dim " + t + " samples: " + nBallIntegrator[t].getStats().getCount());
                    }


                double volume = 1.0;
                for (l = 0; l < d - 1; l++) {
                    volume *= nBallIntegrator[l].getStats().getAvg();
                }

                nball_stats.update(2.0 * volume);

                randOptOut.println(
                        k + "\t"
                                + new Sphere(d, r).getVolume() + "\t"
                                + nball_stats.getLast() + "\t"
                                + nball_stats.getAvg() + "\t"
                                + nball_stats.getVar() + "\t"
                                + nball_stats.getCVar2()
                );
            }
            randOptOut.close();
            logOut.close();
        }

        if(method.equals("opt")) {
            PrintStream optOut = new PrintStream(("opt_" + d + "_" + n + "_" + repetitions + ".txt"));
            optOut.println("Repetition\tExactVolume\tEstimate\tMeanEstimate\tVariance\tCVar2\tSEED=" + seed);
            nball_stats = new Stats();
            for (k = 0; k < repetitions; k++) {
                for (i = 0; i < d - 1; i++) {
                    nBallIntegrator[i] = new Integrator(new DomainSphere(i + 1, r, rnd), new FunctionSphere(i + 1, r), new PdfSphere(i + 1, r));
                }

                for (i = 0; i < n * d; i++) {
                    argmax = 0;
                    max = 0.0;
                    for (j = 0; j < d - 1; j++) {
                        stats = nBallIntegrator[j].getStats();
                        if (stats.getCount() < n / 10) { /* no reliable Cvar2 yet */
                            argmax = j;
                            break;
                        }
                        if (stats.getCVar2() / (stats.getCount() * (stats.getCount() + 1)) > max) {
                            max = stats.getCVar2() / (stats.getCount() * (stats.getCount() + 1));
                            argmax = j;
                        }
                    }
                    nBallIntegrator[argmax].update();
                    dsel_stats[argmax].update(1.0);
                }
                double volume = 1.0;
                for (l = 0; l < d - 1; l++) {
                    volume *= nBallIntegrator[l].getStats().getAvg();
                }
                nball_stats.update(2.0 * volume);
                optOut.println(k + "\t" + new Sphere(d, r).getVolume() + "\t" + nball_stats.getLast() + "\t" + nball_stats.getAvg() + "\t" + nball_stats.getVar() + "\t" + nball_stats.getCVar2());
            }
            optOut.close();
        }

        if (method.equals("dsel") || method.equals("all")) {
            PrintStream dselOut = new PrintStream(("dsel_" + d + "_" + n + "_" + repetitions + ".txt"));
            dselOut.println("Dimension\tEstimatedCVar2\tExactCVar2\tActualUpdateShare\tEstimatedFraction\tExactFraction\tSampleCount\tSEED=" + seed);
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

                warnIfNegative(estCvar2, "EstimatedCVar2 (dsel)", "dim " + (i + 1));
                warnIfNegative(exactCvar2, "ExactCVar2 (dsel)", "dim " + (i + 1));

                dselOut.println(i + 1 + "\t" +
                        estCvar2 + "\t" +
                        exactCvar2 + "\t" +
                        (double) dsel_stats[i].getCount() / (d * n)/100 + "\t" +
                        d * n * Math.sqrt(Math.max(estCvar2, 0.0)) / cvar2sum / sum + "\t" +
                        Math.sqrt(Math.max(exactCvar2, 0.0)) / exact_cvarsum + "\t" +
                        dsel_stats[i].getCount());
            }
            dselOut.close();
        }

        if(method.equals("uni") || method.equals("all")) {
            PrintStream uniOut = new PrintStream(("uni_" + d + "_" + n + "_" + repetitions + ".txt"));
            uniOut.println("Repetition\tExactVolume\tEstimate\tMeanEstimate\tVariance\tCVar2\tSEED=" + seed);
            nball_stats = new Stats();
            for (k = 0; k < repetitions; k++) {
                for (i = 0; i < d - 1; i++) {
                    nBallIntegrator[i] = new Integrator(new DomainSphere(i + 1, r, rnd), new FunctionSphere(i + 1, r), new PdfSphere(i + 1, r));
                }
                for (i = 0; i < n; i++) {
                    for (j = 0; j < d - 1; j++) {
                        nBallIntegrator[j].update();
                    }
                }
                double volume = 1.0;
                for (l = 0; l < d - 1; l++) {
                    volume *= nBallIntegrator[l].getStats().getAvg();
                }

                checkFinite(volume, "volume (uniform)", "repetition " + k);
                warnIfNegative(volume, "volume (uniform)", "repetition " + k);

                nball_stats.update(2 * volume);

                checkFinite(nball_stats.getLast(), "last estimate (uniform)", "repetition " + k);
                checkFinite(nball_stats.getAvg(), "mean estimate (uniform)", "repetition " + k);
                warnIfNegative(nball_stats.getVar(), "variance (uniform)", "repetition " + k);
                warnIfNegative(nball_stats.getCVar2(), "CVar2 (uniform)", "repetition " + k);

                uniOut.println(k + "\t" + new Sphere(d, r).getVolume() + "\t" + nball_stats.getLast() + "\t" + nball_stats.getAvg() + "\t" + nball_stats.getVar() + "\t" + nball_stats.getCVar2());
            }
            uniOut.close();
        }


        System.out.println("Simulation finished. Results written to files");
    }
}

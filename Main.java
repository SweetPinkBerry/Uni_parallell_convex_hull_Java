import java.util.concurrent.TimeUnit;
import java.util.Arrays;

class Main {

    static double getMedianOfSequential(int n, int[] x, int[] y, IntList points) {
        double[] times = new double[7];
        for (int i = 0; i < times.length; i++) {
            ConvexHull ch = new ConvexHull(n, x, y, points);
            double start = (double) System.nanoTime();
            IntList coHull = ch.sequentialHull();
            double end = (double) System.nanoTime();
            times[i] = ((end - start) / 1000000);
        }
        Arrays.sort(times);
        return times[3];
    }


    static double getMedianOfParallel(int n, int[] x, int[] y, IntList points, int k) {
        double[] times = new double[7];
        for (int i = 0; i < times.length; i++) {
            ConvexHull ch = new ConvexHull(n, x, y, points, k);
            double start = (double) System.nanoTime();
            IntList coHull = ch.parallelHull();
            double end = (double) System.nanoTime();
            times[i] = ((end - start) / 1000000);
        }
        Arrays.sort(times);
        return times[3];
    }


    static void test(int n, int seed) {
        int[] x = new int[n], y = new int[n];
        NPunkter17 p = new NPunkter17(n, seed);
        p.fyllArrayer(x, y);
        IntList points = p.lagIntList();

        double seq = getMedianOfSequential(n, x, y, points);
        double par4 = getMedianOfParallel(n, x, y, points, 4);
        double par8 = getMedianOfParallel(n, x, y, points, 8);
        double par16 = getMedianOfParallel(n, x, y, points, 16);

        double speedup4 = (seq / par4);
        double speedup8 = (seq / par8);
        double speedup16 = (seq / par16);
        System.out.println("Median time for sequental convex hull\t:\t" + seq + " ms");
        System.out.println("Median time for parallel (4 threads)\t:\t" + par4 + " ms");
        System.out.println("Median time for parallel (8 threads)\t:\t" + par8 + " ms");
        System.out.println("Median time for parallel (16 threads)\t:\t" + par16 + " ms");
        System.out.println("Speedup 4 threads\t:\t" + speedup4);
        System.out.println("Speedup 8 threads\t:\t" + speedup8);
        System.out.println("Speedup 16 threads\t:\t" + speedup16);
    }


    public static void main(String[] args) {
        int n, seed, k;

        try {
            n = Integer.parseInt(args[0]);
            seed = Integer.parseInt(args[1]);
            k = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.out.println("Correct usage: java Main <n> <seed> <k>");
            return;
        }

        if (n < 0) {
            System.out.println("ERROR: <n>, the number of points must be a positive number");
            return;
        }
        if (k == 0) {
            k = Runtime.getRuntime().availableProcessors();
        }

        if (k < 0) {
            System.out.println("ERROR: <k>, the number of threads must be 0 or a positive number");
            System.out.println("NOTE: if <k> is set as 0, the number of threads will be set as the number of cores on the machine");
            System.out.println("Program will now run in test-mode");
            test(n, seed);
        }
        else {
            double start = (double) System.nanoTime();
            int[] x = new int[n], y = new int[n];
            NPunkter17 p = new NPunkter17(n, seed);
            p.fyllArrayer(x, y);
            IntList points = p.lagIntList();
            double end = (double) System.nanoTime();
            double time = (end - start) / 1000000;
            System.out.println("Time to create points\t:\t" + time + " ms");
            double seq = getMedianOfSequential(n, x, y, points);
            double par = getMedianOfParallel(n, x, y, points, k);
            double speedup = (seq / par);
            System.out.println("Median time for sequental convex hull\t:\t" + seq + " ms");
            System.out.println("Median time for parallel (" + k + " threads)\t:\t" + par + " ms");
            System.out.println("Speedup\t:\t" + speedup);


            if (n < 10000) {
                ConvexHull ch = new ConvexHull(n, x, y, points, k);
                IntList coHull = ch.parallelHull();
                Oblig5Precode pc2 = new Oblig5Precode(ch, coHull);
                pc2.drawGraph();
                pc2.writeHullPoints();
            }
        }
    }
}
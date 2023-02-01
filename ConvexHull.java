import java.lang.Math.*;
import java.util.concurrent.CyclicBarrier;

class ConvexHull {
    int[] x, y;
    int n, MAX_X, MAX_Y, MIN_X, k;
    IntList points;
    Lego[] runnables;
    CyclicBarrier cb;


    ConvexHull(int n, int[] x, int[] y, IntList points) {
        this.n = n;
        this.x = x;
        this.y = y;
        this.points = points;
        this.k = 0;
    }

    ConvexHull(int n, int[] x, int[] y, IntList points, int k) {
        this.n = n;
        this.x = x;
        this.y = y;
        this.points = points;
        this.k = k;
    }


    class Lego implements Runnable {
        IntList partConvexHull;
        int id, max_x, min_x, start, end;

        Lego(int id, int start, int end) {
            this.id = id;
            this.start = start;
            this.end = end;
        }

        public void run() {
            IntList subPoints = new IntList();

            for (int i = start; i < end; i++) {
                subPoints.add(i);
                if (x[i] > x[max_x]) max_x = i;
                else if (x[i] < x[min_x]) min_x = i;
            }

            partConvexHull = new IntList();
            partConvexHull.add(max_x);
            findPointsToLeft(min_x, max_x, subPoints, partConvexHull);
            partConvexHull.add(min_x);
            findPointsToLeft(max_x, min_x, subPoints, partConvexHull);

            try {
                cb.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    IntList parallelHull() {
        cb = new CyclicBarrier(k + 1);
        //Start threads on a part of the array
        int pointsPerThread = (int) Math.floor(n / k);

        runnables = new Lego[k];

        for (int i = 0; i < k - 1; i++) {
            int start = i * pointsPerThread;
            int end = start + pointsPerThread;
            runnables[i] = new Lego(i, start, end);
            (new Thread(runnables[i])).start();
        }
        int start = (k - 1) * pointsPerThread;
        int end = n;
        runnables[k - 1] = new Lego(k - 1, start, end);
        (new Thread(runnables[k - 1])).start();

        try {
            cb.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Create a list of all the outer points found
        IntList hullPoints = new IntList();
        for (Lego legoMan : runnables) {
            hullPoints.append(legoMan.partConvexHull);
        }

        //Sequentially find the final outer points
        for (int i = 0; i < n; i++) {
            if (x[i] > x[MAX_X]) MAX_X = i;
            else if (x[i] < x[MIN_X]) MIN_X = i;
            if (y[i] > y[MAX_Y]) MAX_Y = i;
        }

        IntList convexHull = new IntList();

        convexHull.add(MAX_X);
        findPointsToLeft(MIN_X, MAX_X, hullPoints, convexHull);
        convexHull.add(MIN_X);
        findPointsToLeft(MAX_X, MIN_X, hullPoints, convexHull);
        
        if (points.size() < 100) {
            convexHull.print();
        }
        return convexHull;
    }


    IntList sequentialHull() {
        for (int i = 0; i < n; i++) {
            if (x[i] > x[MAX_X]) MAX_X = i;
            else if (x[i] < x[MIN_X]) MIN_X = i;
            if (y[i] > y[MAX_Y]) MAX_Y = i;
        }

        IntList convexHull = new IntList();

        convexHull.add(MAX_X);
        findPointsToLeft(MIN_X, MAX_X, points, convexHull);
        convexHull.add(MIN_X);
        findPointsToLeft(MAX_X, MIN_X, points, convexHull);

        if (points.size() < 100) {
            convexHull.print();
        }
        return convexHull;
    }


    void findPointsToLeft(int p1, int p2, IntList points, IntList convexHull) {
        int a = y[p1] - y[p2];
        int b = x[p2] - x[p1];
        int c = (y[p2] * x[p1]) - (y[p1] * x[p2]);

        int mDist = 0;
        int mPoint = -1;

        IntList pointsToLeft = new IntList();

        for (int i = 0; i < points.size(); i++) {
            int p = points.get(i);

            int d = a * x[p] + b * y[p] + c;

            //if the distance is on or above the line,
            //and the point is not one of the points making up the line
            //add the point to list
            if (d >= 0 && p != p1 && p != p2) {
                pointsToLeft.add(p);

                //if it is the greatest distance up to now
                //set as the point used to make the next line
                if (d > mDist) {
                    mDist = d;
                    mPoint = p;
                }
            }
        }

        //if the greatest distance is never set
        //the list of points are all 0 or empty
        if (mPoint == -1) {

            int[] nullPoints = pointsToLeft.data;
            int len = pointsToLeft.len;

            //for all points, find the one closest to p2, and add it to the convex hull
            while (len > 0) {
                int first = compareDistanceToPoint(p2, nullPoints[0]);
                int index = 0;
                for (int i = 1; i < len; i++) {
                    int temp = compareDistanceToPoint(p2, nullPoints[i]);
                    if (temp < first) {
                        first = temp;
                        index = i;
                    }
                }
                convexHull.add(nullPoints[index]);
                int tempA = nullPoints[len - 1];
                nullPoints[len - 1] = nullPoints[index];
                nullPoints[index] = tempA;
                len--;
            }
        }

        if (mPoint >= 0) {
            findPointsToLeft(mPoint, p2, pointsToLeft, convexHull);
            convexHull.add(mPoint);
            findPointsToLeft(p1, mPoint, pointsToLeft, convexHull);
        }
    }

    int compareDistanceToPoint(int p2, int p) {
        int a = y[p] - y[p2];
        int b = x[p2] - x[p];
        return (a * a) + (b * b);
    }
}
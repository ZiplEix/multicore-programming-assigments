class DivideAndConquerSum2 {

    private static int NUM_END = 10000;

    public static void main(String[] args) {
        if (args.length == 1) {
            NUM_END = Integer.parseInt(args[0]);
        }

        long[] int_arr = new long[NUM_END];
        int i;
        long s;

        for (i = 0; i < NUM_END; i++)
            int_arr[i] = i + 1;
        s = sum(int_arr);
        System.out.println("sum from 1 to " + NUM_END + "=");
        System.out.println(s);
    }

    static long sum(long[] arr) {
        SumThread t = new SumThread(arr, 0, arr.length);
        t.run();
        return t.ans;
    }
}

class SumThread extends Thread {
    private static int SEQUENTIAL_CUTOFF = 1000;
    int lo;
    int hi;
    long[] arr; // arguments
    long ans = 0; // result

    SumThread(long[] a, int l, int h) {
        lo = l;
        hi = h;
        arr = a;
    }

    public void run() { // override
        if ((hi - lo) < SEQUENTIAL_CUTOFF)
            for (int i = lo; i < hi; i++)
                ans += arr[i];
        else {
            SumThread left = new SumThread(arr, lo, (hi + lo) / 2);
            SumThread right = new SumThread(arr, (hi + lo) / 2, hi);
            left.start();
            right.start();
            try {
                left.join();
                right.join();
            } catch (InterruptedException IntExp) {
            }
            ans = left.ans + right.ans;
            System.out.println("        sum(" + lo + "to" + (hi - 1) + ")=" + ans);
        }
    }
}

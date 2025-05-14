import java.util.concurrent.locks.ReentrantLock;

public class pc_static_block {
    private static int NUM_END = 200000;
    private static int NUM_THREAD = 1;

    private static int counter = 0;
    private static final ReentrantLock mutex = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 2) {
            NUM_THREAD = Integer.parseInt(args[0]);
            NUM_END = Integer.parseInt(args[1]);
        }

        Thread[] threads = new Thread[NUM_THREAD];
        long programStartTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_THREAD; i++) {
            final int threadIndex = i;
            final int start = i * NUM_END / NUM_THREAD;
            final int end = (i + 1) * NUM_END / NUM_THREAD - 1;

            threads[i] = new Thread(() -> {
                System.out.println("Thread " + threadIndex + ": Starting from " + start + " to " + end);
                long startTime = System.currentTimeMillis();

                for (int j = start; j <= end; j++) {
                    if (isPrime(j)) {
                        mutex.lock();
                        try {
                            counter++;
                        } finally {
                            mutex.unlock();
                        }
                    }
                }

                long endTime = System.currentTimeMillis();
                long timeDiff = endTime - startTime;
                System.out.println("Thread " + threadIndex + ": Execution Time: " + timeDiff + "ms");
            });

            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long programEndTime = System.currentTimeMillis();
        long programTimeDiff = programEndTime - programStartTime;

        System.out.println("Program Execution Time: " + programTimeDiff + "ms");
        System.out.println("Number of primes from 1 to " + (NUM_END - 1) + ": " + counter);

        System.out.println(programTimeDiff + "ms");
    }

    private static boolean isPrime(int x) {
        int i;
        if (x <=1) return false;

        for (i = 2; i < x; i++) {
            if (x % i == 0) return false;
        }

        return true;
    }
}

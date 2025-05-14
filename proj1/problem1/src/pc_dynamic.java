import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class pc_dynamic {
    private static int NUM_END = 200000;
    private static int NUM_THREAD = 1;
    private static final int TASK_SIZE = 10;

    private static int counter = 0;
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final AtomicInteger nextTask = new AtomicInteger(1);

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 2) {
            NUM_THREAD = Integer.parseInt(args[0]);
            NUM_END = Integer.parseInt(args[1]);
        }

        Thread[] threads = new Thread[NUM_THREAD];
        long programStartTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_THREAD; i++) {
            final int threadIndex = i;

            threads[i] = new Thread(() -> {
                System.out.println("Thread " + threadIndex + ": Started dynamic task assignment...");
                long startTime = System.currentTimeMillis();

                while (true) {
                    int start = nextTask.getAndAdd(TASK_SIZE);
                    if (start > NUM_END) break;

                    int end = Math.min(start + TASK_SIZE - 1, NUM_END);

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
                }

                long endTime = System.currentTimeMillis();
                System.out.println("Thread " + threadIndex + ": Execution Time: " + (endTime - startTime) + "ms");
            });

            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long programEndTime = System.currentTimeMillis();
        System.out.println("Program Execution Time: " + (programEndTime - programStartTime) + "ms");
        System.out.println("Number of primes from 1 to " + NUM_END + ": " + counter);

        System.out.println(programEndTime - programStartTime + "ms");
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

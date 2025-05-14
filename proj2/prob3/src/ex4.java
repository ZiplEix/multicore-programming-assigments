import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ex4 {
    public static void main(String[] args) {
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties,
            () -> System.out.println("All parties arrived, resuming tasks...")
        );

        for (int i = 1; i <= parties; i++) {
            new Thread(new Worker(barrier), "Thread-" + i).start();
        }
    }
}

class Worker implements Runnable {
    private CyclicBarrier barrier;

    public Worker(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " is working...");
            Thread.sleep(1000);
            System.out.println(Thread.currentThread().getName() + " waiting at the barrier");
            barrier.await();
            System.out.println(Thread.currentThread().getName() + " resumed work!");
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
        }
    }
}

import java.util.concurrent.*;

public class ex5 {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        Callable<String> task = () -> {
            Thread.sleep(1000);
            return Thread.currentThread().getName() + " finished";
        };

        for (int i = 1; i <= 5; i++) {
            Future<String> future = executorService.submit(task);
            try {
                System.out.println("Task result: " + future.get());
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Task interrupted");
            }
        }

        executorService.shutdown();
    }
}

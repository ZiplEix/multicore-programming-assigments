import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ex2 {
    private static int sharedData = 0;
    private static ReadWriteLock lock = new ReentrantReadWriteLock();

    public static void main(String[] args) {
        // Writer thread
        new Thread(() -> {
            lock.writeLock().lock();
            try {
                sharedData++;
                System.out.println("Written: " + sharedData);
            } finally {
                lock.writeLock().unlock();
            }
        }).start();

        // Reader thread
        new Thread(() -> {
            lock.readLock().lock();
            try {
                System.out.println("Read: " + sharedData);
            } finally {
                lock.readLock().unlock();
            }
        }).start();
    }
}

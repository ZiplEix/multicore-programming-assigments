import java.util.concurrent.atomic.AtomicInteger;

public class ex3 {
    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger(10);

        // Getting the value
        System.out.println("Initial value: " + atomicInteger.get());

        // Setting a new value
        atomicInteger.set(20);
        System.out.println("Updated value: " + atomicInteger.get());

        // Adding and getting the previous value
        int previous = atomicInteger.getAndAdd(5);
        System.out.println("Previous value: " + previous);
        System.out.println("After addition: " + atomicInteger.get());

        // Adding and getting the new value
        int updated = atomicInteger.addAndGet(10);
        System.out.println("After addAndGet: " + updated);
    }
}

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParkingBlockingQueue {
    public static void main(String[] args) {
        ParkingGarage parkingGarage = new ParkingGarage(7);
        for (int i = 1; i <= 10; i++) {
            new Car("Car " + i, parkingGarage).start();
        }
    }
}

class ParkingGarage {
    private BlockingQueue<String> parkingSpots;

    public ParkingGarage(int places) {
        parkingSpots = new ArrayBlockingQueue<>(places);
        // Initialize the parking spots as available
        for (int i = 0; i < places; i++) {
            parkingSpots.add("Spot");
        }
    }

    public void enter(String carName) {
        try {
            System.out.println(carName + ": trying to enter");
            parkingSpots.take();
            System.out.println(carName + ": just entered");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void leave(String carName) {
        try {
            parkingSpots.put("Spot");
            System.out.println(carName + ":                                     have been left");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Car extends Thread {
    private ParkingGarage parkingGarage;

    public Car(String name, ParkingGarage p) {
        super(name);
        this.parkingGarage = p;
    }

    private void aboutToLeave() {
        System.out.println(getName() + ":                                     about to leave");
    }

    public void run() {
        while (true) {
            try {
                sleep((int) (Math.random() * 10000)); // drive before parking
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            parkingGarage.enter(getName());
            try {
                sleep((int) (Math.random() * 20000)); // stay within the parking garage
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            aboutToLeave();
            parkingGarage.leave(getName());
        }
    }
}

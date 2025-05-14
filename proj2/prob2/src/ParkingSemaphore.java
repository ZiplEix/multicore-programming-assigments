import java.util.concurrent.Semaphore;

public class ParkingSemaphore {
    public static void main(String[] args) {
        ParkingGarage parkingGarage = new ParkingGarage(7);
        for (int i = 1; i <= 10; i++) {
            new Car("Car " + i, parkingGarage).start();
        }
    }
}

class ParkingGarage {
    private final Semaphore semaphore;

    public ParkingGarage(int places) {
        semaphore = new Semaphore(places);
    }

    public void enter(String carName) {
        try {
            System.out.println(carName + ": trying to enter");
            semaphore.acquire();
            System.out.println(carName + ": just entered");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void leave(String carName) {
        System.out.println(carName + ":                                     about to leave");
        semaphore.release();
        System.out.println(carName + ":                                     have been left");
    }
}

class Car extends Thread {
    private final ParkingGarage parkingGarage;

    public Car(String name, ParkingGarage p) {
        super(name);
        this.parkingGarage = p;
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
            parkingGarage.leave(getName());
        }
    }
}

import java.util.*;
import java.lang.*;

public class MatmultD {
    private static Scanner sc = new Scanner(System.in);
    private static int[][] a, b, c;
    private static int thread_no;
    private static long[] thread_times;

    public static void main(String[] args) throws InterruptedException {
        thread_no = args.length == 1 ? Integer.parseInt(args[0]) : 1;

        a = readMatrix();
        b = readMatrix();
        c = new int[a.length][b[0].length];
        thread_times = new long[thread_no];

        long programStartTime = System.currentTimeMillis();

        Thread[] threads = new Thread[thread_no];
        int rowsPerThread = a.length / thread_no;
        int remainder = a.length % thread_no;

        int currentRow = 0;

        for (int i = 0; i < thread_no; i++) {
            int startRow = currentRow;
            int rowsForThisThread = rowsPerThread + (i < remainder ? 1 : 0);
            int endRow = startRow + rowsForThisThread;

            threads[i] = new Thread(new Worker(i, startRow, endRow));
            threads[i].start();
            currentRow = endRow;
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long programEndTime = System.currentTimeMillis();

        printMatrix(c);

        System.out.printf("Total execution time: %d ms\n", programEndTime - programStartTime);
        System.out.println(programEndTime - programStartTime);
    }

    public static int[][] readMatrix() {
        int rows = sc.nextInt();
        int cols = sc.nextInt();
        int[][] result = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = sc.nextInt();
            }
        }
        return result;
    }

    public static void printMatrix(int[][] matrix) {
        System.out.println("Matrix[" + matrix.length + "][" + matrix[0].length + "]");
        int sum = 0;
        for (int[] row : matrix) {
            for (int val : row) {
                sum += val;
            }
        }
        System.out.println("Matrix Sum = " + sum);
    }

    static class Worker implements Runnable {
        int id, startRow, endRow;

        Worker(int id, int startRow, int endRow) {
            this.id = id;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        public void run() {
            long startTime = System.currentTimeMillis();

            int n = a[0].length;
            int p = b[0].length;

            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < p; j++) {
                    for (int k = 0; k < n; k++) {
                        c[i][j] += a[i][k] * b[k][j];
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            thread_times[id] = endTime - startTime;
            System.out.printf("Thread %d execution time: %d ms\n", id, thread_times[id]);
        }
    }
}

import java.io.*;
import java.util.*;

public class BenchmarkMatmult {
    private static final String PROGRAM = "MatmultD";
    private static final int[] THREAD_COUNTS = {1, 2, 4, 6, 8, 10, 12, 14, 16, 32};
    private static final String MATRIX_FILE = "../mat/mat1000.txt";
    private static final int RUNS = 10;

    public static void main(String[] args) throws Exception {
        Map<Integer, Long> results = new LinkedHashMap<>();

        System.out.println("Compiling " + PROGRAM + ".java...");
        Process compile = new ProcessBuilder("javac", PROGRAM + ".java").inheritIO().start();
        int status = compile.waitFor();
        if (status != 0) {
            System.err.println("Compilation failed for " + PROGRAM + ".java");
            return;
        }

        for (int threads : THREAD_COUNTS) {
            List<Long> validTimes = new ArrayList<>();

            for (int i = 0; i < RUNS; i++) {
                List<String> command = new ArrayList<>();
                command.add("bash");
                command.add("-c");
                command.add("java " + PROGRAM + " " + threads + " < " + MATRIX_FILE);

                System.out.println("Run " + (i + 1) + " with " + threads + " threads: " + String.join(" ", command));

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                String lastLine = null;

                while ((line = reader.readLine()) != null) {
                    lastLine = line;
                }

                process.waitFor();

                if (lastLine != null && lastLine.matches("\\d+")) {
                    long timeMs = Long.parseLong(lastLine);
                    if (timeMs >= 0) {
                        validTimes.add(timeMs);
                    } else {
                        System.err.printf("⛔ Ignored negative time (%dms) with %d threads (run %d).\n", timeMs, threads, i + 1);
                    }
                } else {
                    System.err.printf("⚠️ No valid time output with %d threads (run %d): %s\n", threads, i + 1, lastLine);
                }
            }

            if (!validTimes.isEmpty()) {
                double average = validTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                double maxAllowed = average * 1.2;

                List<Long> filteredTimes = new ArrayList<>();
                for (long time : validTimes) {
                    if (time <= maxAllowed) {
                        filteredTimes.add(time);
                    } else {
                        System.err.printf("🚫 Excluded outlier time: %dms (outside ±20%% of avg %dms)\n", time, (long) average);
                    }
                }

                if (!filteredTimes.isEmpty()) {
                    long filteredAvg = (long) filteredTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                    results.put(threads, filteredAvg);
                } else {
                    System.err.printf("❌ All runs excluded after filtering with %d threads.\n", threads);
                }
            } else {
                System.err.printf("❌ No valid runs with %d threads.\n", threads);
            }
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        int count = 0;
        for (Map.Entry<Integer, Long> entry : results.entrySet()) {
            json.append("  \"").append(entry.getKey()).append("\": ").append(entry.getValue());
            if (++count < results.size()) json.append(",");
            json.append("\n");
        }
        json.append("}");

        System.out.println("\n=== MatmultD Benchmark Results ===");
        System.out.println(json.toString());

        try (FileWriter writer = new FileWriter("../results.json")) {
            writer.write(json.toString());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}

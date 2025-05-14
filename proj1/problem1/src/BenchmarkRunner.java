import java.io.*;
import java.util.*;

public class BenchmarkRunner {
    private static final String[] PROGRAMS = {
        "pc_serial",
        "pc_static_block",
        "pc_static_cyclic",
        "pc_dynamic"
    };

    private static final int[] THREAD_COUNTS =  {1, 2, 4, 6, 8, 10, 12, 14, 16, 32};
    private static final int NUM_END = 200_000;

    public static void main(String[] args) throws Exception {
        Map<String, Map<Integer, Long>> results = new LinkedHashMap<>();

        for (String program : PROGRAMS) {
            System.out.println("Compiling " + program + ".java...");
            Process compile = new ProcessBuilder("javac", program + ".java").inheritIO().start();
            int status = compile.waitFor();
            if (status != 0) {
                System.err.println("Compilation failed for " + program + ".java");
                return;
            }
        }

        for (String program : PROGRAMS) {
            Map<Integer, Long> programResults = new LinkedHashMap<>();

            for (int threads : THREAD_COUNTS) {
                if (program.equals("pc_serial") && threads != 1) continue;

                List<Long> validTimes = new ArrayList<>();

                for (int i = 0; i < 10; i++) {
                    List<String> command = new ArrayList<>();
                    command.add("java");
                    command.add(program);
                    if (!program.equals("pc_serial")) {
                        command.add(String.valueOf(threads));
                        command.add(String.valueOf(NUM_END));
                    }

                    System.out.println("Run " + (i + 1) + ": " + command);

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

                    if (lastLine != null && lastLine.matches(".*\\d+ms$")) {
                        String[] parts = lastLine.split("\\s+");
                        String timeStr = parts[parts.length - 1].replace("ms", "");
                        long timeMs = Long.parseLong(timeStr);

                        if (timeMs >= 0) {
                            validTimes.add(timeMs);
                        } else {
                            System.err.println("⛔ Ignored negative time (" + timeMs + "ms) from " + program + " with " + threads + " threads (run " + (i + 1) + ").");
                        }
                    } else {
                        System.err.println("⚠️ No valid time output from " + program + " with " + threads + " threads (run " + (i + 1) + ").");
                    }
                }

                // Première moyenne brute
                if (!validTimes.isEmpty()) {
                    double average = validTimes.stream().mapToLong(Long::longValue).average().orElse(0);

                    // Filtrage à ±20%
                    double maxAllowed = average * 1.2;

                    List<Long> filteredTimes = new ArrayList<>();
                    for (long time : validTimes) {
                        if (time <= maxAllowed) {
                            filteredTimes.add(time);
                        } else {
                            System.err.println("🚫 Excluded outlier time: " + time + "ms (outside ±20% of avg " + (long)average + "ms)");
                        }
                    }

                    if (!filteredTimes.isEmpty()) {
                        long filteredAvg = (long) filteredTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                        programResults.put(threads, filteredAvg);
                    } else {
                        System.err.println("❌ All runs excluded after filtering for " + program + " with " + threads + " threads.");
                    }
                } else {
                    System.err.println("❌ No valid runs for " + program + " with " + threads + " threads.");
                }
            }

            results.put(program, programResults);
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        int pCount = 0;
        for (Map.Entry<String, Map<Integer, Long>> entry : results.entrySet()) {
            json.append("  \"").append(entry.getKey()).append("\": {\n");
            int tCount = 0;
            for (Map.Entry<Integer, Long> run : entry.getValue().entrySet()) {
                json.append("    \"").append(run.getKey()).append("\": ").append(run.getValue());
                if (++tCount < entry.getValue().size()) json.append(",");
                json.append("\n");
            }
            json.append("  }");
            if (++pCount < results.size()) json.append(",");
            json.append("\n");
        }
        json.append("}");

        System.out.println("\n=== Benchmark Results ===");
        System.out.println(json.toString());

        try (FileWriter writer = new FileWriter("../results.json")) {
            writer.write(json.toString());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}

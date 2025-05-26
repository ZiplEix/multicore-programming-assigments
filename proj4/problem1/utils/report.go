package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"regexp"
	"strconv"
)

var (
	threadCounts  = []int{1, 2, 4, 6, 8, 10, 12, 14, 16}
	runsPerConfig = 10
	programBinary = "../openmp_ray"
)

func main() {
	results := map[string]map[int]int64{}
	results["OpenMP_RayTracing"] = map[int]int64{}

	for _, threads := range threadCounts {
		fmt.Printf("\nBenchmarking OpenMP Ray Tracing with %d threads...\n", threads)
		times := []int64{}

		for run := 0; run < runsPerConfig; run++ {
			cmd := exec.Command(programBinary, strconv.Itoa(threads))
			var stdout bytes.Buffer
			cmd.Stdout = &stdout
			cmd.Stderr = os.Stderr

			if err := cmd.Run(); err != nil {
				fmt.Fprintf(os.Stderr, "Run %d for %d threads failed: %v\n", run+1, threads, err)
				continue
			}

			output := stdout.String()
			re := regexp.MustCompile(`OpenMP \(\d+ threads\) ray tracing: ([\d.]+) sec`)
			match := re.FindStringSubmatch(output)

			if len(match) == 2 {
				sec, err := strconv.ParseFloat(match[1], 64)
				if err == nil {
					ms := int64(sec * 1000)
					times = append(times, ms)
					fmt.Printf("Run %d: Parsed time: %dms\n", run+1, ms)
				} else {
					fmt.Printf("❌ Failed to parse float for %d threads: %s\n", threads, match[1])
				}
			} else {
				fmt.Printf("❌ No time match found in output for %d threads:\n%s\n", threads, output)
			}
		}

		if len(times) == 0 {
			fmt.Printf("❌ No valid runs for %d threads.\n", threads)
			continue
		}

		var sum int64
		for _, t := range times {
			sum += t
		}
		avg := float64(sum) / float64(len(times))

		var filtered []int64
		for _, t := range times {
			if float64(t) >= avg*0.8 && float64(t) <= avg*1.2 {
				filtered = append(filtered, t)
			} else {
				fmt.Printf("🚫 Excluded outlier for %d threads: %dms (avg: %.2fms)\n", threads, t, avg)
			}
		}

		if len(filtered) == 0 {
			fmt.Printf("❌ All runs excluded after filtering for %d threads. Consider adjusting filtering or `runsPerConfig`.\n", threads)
			continue
		}

		var filteredSum int64
		for _, t := range filtered {
			filteredSum += t
		}

		results["OpenMP_RayTracing"][threads] = filteredSum / int64(len(filtered))
	}

	jsonOut, err := json.MarshalIndent(results, "", "  ")
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error marshalling JSON: %v\n", err)
		return
	}
	fmt.Println("\n=== Benchmark Results ===")
	fmt.Println(string(jsonOut))
	os.WriteFile("results.json", jsonOut, 0644)
	fmt.Println("\nResults saved to results_openmp_ray.json")
}

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
	threadCounts    = []int{1, 2, 4, 6, 8, 10, 12, 14, 16}
	schedulingTypes = map[int]string{
		1: "static_default",
		2: "dynamic_default",
		3: "static_10",
		4: "dynamic_10",
	}
	runsPerConfig = 10
)

func main() {
	results := map[string]map[int]int64{}

	for schedID, schedName := range schedulingTypes {
		results[schedName] = map[int]int64{}
		for _, threads := range threadCounts {
			fmt.Printf("\nBenchmarking %s with %d threads...\n", schedName, threads)
			times := []int64{}

			for run := 0; run < runsPerConfig; run++ {
				cmd := exec.Command("../prob1", strconv.Itoa(schedID), strconv.Itoa(threads))
				var stdout bytes.Buffer
				cmd.Stdout = &stdout
				cmd.Stderr = os.Stderr

				if err := cmd.Run(); err != nil {
					fmt.Fprintf(os.Stderr, "Run %d failed: %v\n", run+1, err)
					continue
				}

				output := stdout.String()
				re := regexp.MustCompile(`Execution time: ([\d.]+) milliseconds`)
				match := re.FindStringSubmatch(output)

				if len(match) == 2 {
					ms, err := strconv.ParseFloat(match[1], 64)
					if err == nil {
						times = append(times, int64(ms))
						fmt.Printf("Run %d: Parsed time: %dms\n", run+1, int64(ms))
					} else {
						fmt.Printf("❌ Failed to parse float: %s\n", match[1])
					}
				} else {
					fmt.Printf("❌ No match found in output:\n%s\n", output)
				}

				// scanner := bufio.NewScanner(&stdout)
				// var lastLine string
				// for scanner.Scan() {
				// 	lastLine = scanner.Text()
				// }

				// if strings.Contains(lastLine, "milliseconds") {
				// 	fmt.Printf("Run %d: %s\n", run+1, lastLine)
				// 	re := regexp.MustCompile(`Execution time: ([\d.]+) milliseconds`)
				// 	match := re.FindString(lastLine)
				// 	if match != "" {
				// 		if ms, err := strconv.ParseFloat(match, 64); err == nil {
				// 			times = append(times, int64(ms))
				// 			fmt.Printf("Parsed time: %dms\n", int64(ms))
				// 		} else {
				// 			fmt.Printf("3❌ Failed to parse time from output: %s\n", lastLine)
				// 		}
				// 	} else {
				// 		fmt.Printf("2❌ Failed to parse time from output: %s\n", lastLine)
				// 	}
				// } else {
				// 	fmt.Printf("1❌ Failed to parse time from output: %s\n", lastLine)
				// }
			}

			if len(times) == 0 {
				fmt.Printf("❌ No valid runs for %s with %d threads.\n", schedName, threads)
				continue
			}

			// Filter outliers ±20%
			var sum int64
			for _, t := range times {
				sum += t
			}
			avg := float64(sum) / float64(len(times))

			var filtered []int64
			for _, t := range times {
				if float64(t) <= avg*1.2 {
					filtered = append(filtered, t)
				} else {
					fmt.Printf("🚫 Excluded outlier: %dms\n", t)
				}
			}

			if len(filtered) == 0 {
				fmt.Printf("❌ All runs excluded after filtering for %s with %d threads.\n", schedName, threads)
				continue
			}

			var filteredSum int64
			for _, t := range filtered {
				filteredSum += t
			}

			results[schedName][threads] = filteredSum / int64(len(filtered))
		}
	}

	jsonOut, _ := json.MarshalIndent(results, "", "  ")
	fmt.Println("\n=== Benchmark Results ===")
	fmt.Println(string(jsonOut))
	os.WriteFile("results.json", jsonOut, 0644)
}

import json
import sys
import matplotlib.pyplot as plt

THREADS = [1, 2, 4, 6, 8, 10, 12, 14, 16, 32]

LABELS = {
    "pc_static_block": "static (block)",
    "pc_static_cyclic": "static (cyclic) [task size : 10]",
    "pc_dynamic": "dynamic [task size : 10]"
}

use_perf = "--perf" in sys.argv

with open("results.json", "r") as f:
    data = json.load(f)

header = "| Thread number | " + " | ".join(str(t) for t in THREADS) + " |"
separator = "|" + "-" * (len("Thread number") + 2) + "|" + "|".join(["----"] * len(THREADS)) + "|"

plot_data = {}

rows = []
for key, label in LABELS.items():
    y_values = []
    row = f"| {label} "
    for t in THREADS:
        raw_val = data.get(key, {}).get(str(t))
        if raw_val is not None:
            if use_perf:
                val = 1_000 / raw_val
                row += f"| {val:.3f} "
                y_values.append(val)
            else:
                val = raw_val
                row += f"| {val} "
                y_values.append(val)
        else:
            row += "| "
            y_values.append(None)
    row += "|"
    rows.append(row)
    plot_data[label] = y_values

print(header)
print(separator)
for row in rows:
    print(row)

plt.figure(figsize=(10, 6))
for label, y_values in plot_data.items():
    x = [t for t, y in zip(THREADS, y_values) if y is not None]
    y = [y for y in y_values if y is not None]
    plt.plot(x, y, marker='o', label=label)

plt.xlabel("Number of threads")
ylabel = "Performance (1/ms)" if use_perf else "Execution time (ms)"
plt.ylabel(ylabel)
plt.title("Benchmark Results" + (" - Performance" if use_perf else " - Execution Time"))
plt.xticks(THREADS)
plt.grid(True, linestyle="--", alpha=0.5)
plt.legend()
plt.tight_layout()

output_file = "media/performance.png" if use_perf else "media/exec_time.png"
plt.savefig(output_file)

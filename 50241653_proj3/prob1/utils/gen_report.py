import json
import matplotlib.pyplot as plt
import os

# Configuration
THREADS = [1, 2, 4, 6, 8, 10, 12, 14, 16]
SCHED_LABELS = {
    "static_default": "Static (default)",
    "dynamic_default": "Dynamic (default)",
    "static_10": "Static (chunk size = 10)",
    "dynamic_10": "Dynamic (chunk size = 10)"
}

# Crée le dossier media s’il n’existe pas
os.makedirs("media", exist_ok=True)

# Charge les résultats du fichier JSON
with open("results.json", "r") as f:
    results = json.load(f)

def generate_table_and_plot(use_perf=False):
    header = "| Scheduling Type | " + " | ".join(str(t) for t in THREADS) + " |"
    separator = "|" + "-" * 17 + "|" + "|".join(["----"] * len(THREADS)) + "|"

    rows = []
    plot_data = {}

    for key, label in SCHED_LABELS.items():
        y_values = []
        row = f"| {label} "
        for t in THREADS:
            val = results.get(key, {}).get(str(t))
            if val is not None:
                display_val = f"{(1000 / val):.3f}" if use_perf else f"{val}"
                row += f"| {display_val} "
                y_values.append(1000 / val if use_perf else val)
            else:
                row += "| "
                y_values.append(None)
        row += "|"
        rows.append(row)
        plot_data[label] = y_values

    # Affiche le tableau Markdown
    print("\n" + ("📊 Performance Table (1/ms)" if use_perf else "⏱️ Execution Time Table (ms)"))
    print(header)
    print(separator)
    for row in rows:
        print(row)

    # Génére le graphique
    plt.figure(figsize=(10, 6))
    for label, y_values in plot_data.items():
        x = [t for t, y in zip(THREADS, y_values) if y is not None]
        y = [y for y in y_values if y is not None]
        plt.plot(x, y, marker='o', label=label)

    plt.xlabel("Number of threads")
    ylabel = "Performance (1/ms)" if use_perf else "Execution time (ms)"
    plt.ylabel(ylabel)
    plt.title("Benchmark Results - " + ("Performance" if use_perf else "Execution Time"))
    plt.xticks(THREADS)
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.legend()
    plt.tight_layout()

    output_file = "media/performance.png" if use_perf else "media/exec_time.png"
    plt.savefig(output_file)
    print(f"✅ Graph saved to: {output_file}\n")

# Génère les deux tableaux + graphes
generate_table_and_plot(use_perf=False)
generate_table_and_plot(use_perf=True)

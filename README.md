# Sorting Algorithm Benchmark

A web-based tool to benchmark 7 sorting algorithms and compare their real execution times.

## Algorithms Included

| Algorithm      | Time Complexity | Space Complexity |
|----------------|-----------------|------------------|
| Bubble Sort    | O(n²)           | O(1)             |
| Selection Sort | O(n²)           | O(1)             |
| Insertion Sort | O(n²)           | O(1)             |
| Merge Sort     | O(n log n)      | O(n)             |
| Quick Sort     | O(n log n)*     | O(log n)         |
| Counting Sort  | O(n + k)        | O(k)             |
| Radix Sort     | O(nk)           | O(n + k)         |

## Project Structure

```
sorting-benchmark/
├── BenchmarkServer.java   ← Java backend (HTTP server + sorting algorithms)
├── index.html             ← Frontend (Chart.js bar chart + results table)
└── README.md
```

## How to Run

### 1. Start the Java backend

```bash
javac BenchmarkServer.java
java BenchmarkServer
```

You should see: `Benchmark server running at http://localhost:8080`

### 2. Open the frontend

Open `index.html` directly in your browser (no web server needed).

### 3. Run a benchmark

- Type comma-separated numbers, OR
- Enter a count and click **↺ Random** to generate a random array
- Click **▶ Run Benchmark**

## How the Frontend ↔ Backend Connection Works

```
Browser (index.html)
    │
    │  POST http://localhost:8080/benchmark
    │  Body: { "numbers": [38, 27, 43, ...] }
    │
    ▼
BenchmarkServer.java (port 8080)
    │  Runs all 7 sorts, times each with System.nanoTime()
    │
    │  Response: { "Bubble Sort": 12.4, "Merge Sort": 0.3, ... }
    ▼
Browser renders bar chart + ranked results table
```

The Java backend uses the built-in `com.sun.net.httpserver` — **no external libraries or Maven needed**.

## Tips for Best Results

- Use **1,000–10,000** numbers to see meaningful timing differences
- The O(n²) algorithms (Bubble, Selection, Insertion) will be dramatically slower at large n
- Counting Sort and Radix Sort excel when the value range is bounded

## GitHub Commit Strategy

```
git init
git add README.md                         # docs: add README
git add BenchmarkServer.java              # feat: add Java HTTP server skeleton
# (add bubble, selection, insertion sort) # feat: add O(n²) sorting algorithms
# (add merge, quick sort)                 # feat: add O(n log n) sorting algorithms
# (add counting, radix sort)              # feat: add linear sorting algorithms
# (add /benchmark endpoint)              # feat: wire up benchmark HTTP endpoint
git add index.html                        # feat: add frontend HTML shell
# (add Chart.js bar chart)               # feat: add bar chart visualization
# (add results table)                    # feat: add ranked results table
# (add random generator)                 # feat: add random array generator
```

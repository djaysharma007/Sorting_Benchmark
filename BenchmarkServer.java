import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class BenchmarkServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/benchmark", new BenchmarkHandler());
        server.createContext("/", new CORSHandler());
        server.start();
        System.out.println("Benchmark server running at http://localhost:8080");
        System.out.println("Open index.html in your browser to use the app.");
    }

    // ─── Sorting Algorithms ───────────────────────────────────────────────────

    static int[] bubbleSort(int[] arr) {
        int[] a = arr.clone();
        int n = a.length;
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++)
                if (a[j] > a[j + 1]) { int t = a[j]; a[j] = a[j+1]; a[j+1] = t; }
        return a;
    }

    static int[] selectionSort(int[] arr) {
        int[] a = arr.clone();
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++)
                if (a[j] < a[minIdx]) minIdx = j;
            int t = a[minIdx]; a[minIdx] = a[i]; a[i] = t;
        }
        return a;
    }

    static int[] insertionSort(int[] arr) {
        int[] a = arr.clone();
        int n = a.length;
        for (int i = 1; i < n; i++) {
            int key = a[i], j = i - 1;
            while (j >= 0 && a[j] > key) { a[j + 1] = a[j]; j--; }
            a[j + 1] = key;
        }
        return a;
    }

    static int[] mergeSort(int[] arr) {
        int[] a = arr.clone();
        mergeSortHelper(a, 0, a.length - 1);
        return a;
    }

    static void mergeSortHelper(int[] a, int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;
            mergeSortHelper(a, l, m);
            mergeSortHelper(a, m + 1, r);
            merge(a, l, m, r);
        }
    }

    static void merge(int[] a, int l, int m, int r) {
        int[] left = Arrays.copyOfRange(a, l, m + 1);
        int[] right = Arrays.copyOfRange(a, m + 1, r + 1);
        int i = 0, j = 0, k = l;
        while (i < left.length && j < right.length)
            a[k++] = (left[i] <= right[j]) ? left[i++] : right[j++];
        while (i < left.length) a[k++] = left[i++];
        while (j < right.length) a[k++] = right[j++];
    }

    static int[] quickSort(int[] arr) {
        int[] a = arr.clone();
        quickSortHelper(a, 0, a.length - 1);
        return a;
    }

    static void quickSortHelper(int[] a, int low, int high) {
        if (low < high) {
            int pi = partition(a, low, high);
            quickSortHelper(a, low, pi - 1);
            quickSortHelper(a, pi + 1, high);
        }
    }

    static int partition(int[] a, int low, int high) {
        int pivot = a[high], i = low - 1;
        for (int j = low; j < high; j++)
            if (a[j] < pivot) { i++; int t = a[i]; a[i] = a[j]; a[j] = t; }
        int t = a[i + 1]; a[i + 1] = a[high]; a[high] = t;
        return i + 1;
    }

    static int[] countingSort(int[] arr) {
        if (arr.length == 0) return arr.clone();
        int[] a = arr.clone();
        int max = Arrays.stream(a).max().getAsInt();
        int min = Arrays.stream(a).min().getAsInt();
        int range = max - min + 1;
        int[] count = new int[range];
        for (int v : a) count[v - min]++;
        int idx = 0;
        for (int i = 0; i < range; i++)
            while (count[i]-- > 0) a[idx++] = i + min;
        return a;
    }

    static int[] radixSort(int[] arr) {
        if (arr.length == 0) return arr.clone();
        int[] a = arr.clone();
        int max = Arrays.stream(a).max().getAsInt();
        for (int exp = 1; max / exp > 0; exp *= 10)
            countByDigit(a, exp);
        return a;
    }

    static void countByDigit(int[] a, int exp) {
        int n = a.length;
        int[] output = new int[n];
        int[] count = new int[10];
        for (int v : a) count[(v / exp) % 10]++;
        for (int i = 1; i < 10; i++) count[i] += count[i - 1];
        for (int i = n - 1; i >= 0; i--) {
            int d = (a[i] / exp) % 10;
            output[--count[d]] = a[i];
        }
        System.arraycopy(output, 0, a, 0, n);
    }

    // ─── HTTP Handler ─────────────────────────────────────────────────────────

    static class BenchmarkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Read body
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes());

            // Parse JSON array manually: {"numbers":[1,2,3,...]}
            int[] numbers = parseNumbers(body);

            if (numbers == null || numbers.length == 0) {
                String err = "{\"error\":\"No numbers provided\"}";
                exchange.sendResponseHeaders(400, err.length());
                exchange.getResponseBody().write(err.getBytes());
                exchange.getResponseBody().close();
                return;
            }

            // Run benchmarks
            Map<String, Double> results = new LinkedHashMap<>();
            results.put("Bubble Sort",    time(() -> bubbleSort(numbers)));
            results.put("Selection Sort", time(() -> selectionSort(numbers)));
            results.put("Insertion Sort", time(() -> insertionSort(numbers)));
            results.put("Merge Sort",     time(() -> mergeSort(numbers)));
            results.put("Quick Sort",     time(() -> quickSort(numbers)));
            results.put("Counting Sort",  time(() -> countingSort(numbers)));
            results.put("Radix Sort",     time(() -> radixSort(numbers)));

            // Build JSON response
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Double> e : results.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(e.getKey()).append("\":").append(e.getValue());
                first = false;
            }
            json.append("}");

            byte[] resp = json.toString().getBytes();
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.getResponseBody().close();
        }

        double time(Runnable fn) {
            long start = System.nanoTime();
            fn.run();
            return Math.round((System.nanoTime() - start) / 1_000.0) / 1_000.0; // ms, 3 decimal places
        }

        int[] parseNumbers(String json) {
            try {
                int start = json.indexOf('[');
                int end = json.lastIndexOf(']');
                if (start == -1 || end == -1) return new int[0];
                String inner = json.substring(start + 1, end).trim();
                if (inner.isEmpty()) return new int[0];
                String[] parts = inner.split(",");
                int[] nums = new int[parts.length];
                for (int i = 0; i < parts.length; i++)
                    nums[i] = Integer.parseInt(parts[i].trim());
                return nums;
            } catch (Exception e) {
                return new int[0];
            }
        }
    }

    static class CORSHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            String msg = "Server is running. POST to /benchmark";
            exchange.sendResponseHeaders(200, msg.length());
            exchange.getResponseBody().write(msg.getBytes());
            exchange.getResponseBody().close();
        }
    }
}

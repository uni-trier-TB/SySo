import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ParallelCounter {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            System.out.println("Usage: java ParallelCounter <folderPath> <regex>");
            return;
        }

        String folder = args[0];
        String regex = args[1];

        // Verschiedene Thread-Anzahlen testen
        int[] threadCounts = {1, 2, 4, 8};

        for (int numThreads : threadCounts) {
            long startTime = System.nanoTime();
            long totalLines = countLinesWithThreads(folder, regex, numThreads);
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            System.out.println("Threads: " + numThreads + " | Lines: " + totalLines + " | Dauer: " + durationMs + " ms");
        }
    }

    // Zeilen einer einzelnen Datei zählen
    public static long countLines(String fileName) {
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.count();
        } catch (IOException e) {
            System.err.println("Kann Datei nicht lesen: " + fileName);
            return 0;
        }
    }

    // Alle Dateien in Ordner mit Threads zählen
    public static long countLinesWithThreads(String folderPath, String regex, int numThreads) throws IOException, InterruptedException {
        Pattern pattern = Pattern.compile(regex);
        List<Path> files;
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            files = paths.filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.toString()).matches())
                    .toList();
        }

        AtomicLong totalLines = new AtomicLong(0);
        List<Thread> threads = new ArrayList<>();

        // Threads erzeugen, aber noch nicht starten
        for (Path file : files) {
            Thread t = new Thread(() -> totalLines.addAndGet(countLines(file.toString())));
            threads.add(t);
        }

        // Threads in Batches starten, um die Anzahl gleichzeitiger Threads zu begrenzen
        for (int i = 0; i < threads.size(); i += numThreads) {
            int end = Math.min(i + numThreads, threads.size());
            List<Thread> batch = threads.subList(i, end);

            // Batch starten
            for (Thread t : batch) t.start();
            // Auf Batch warten
            for (Thread t : batch) t.join();
        }

        return totalLines.get();
    }
}

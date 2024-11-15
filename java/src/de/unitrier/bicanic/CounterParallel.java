package de.unitrier.bicanic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CounterParallel {

    public static void main(String[] args) throws IOException {
        // Beispielwerte für die Anzahl der Threads und das Verzeichnis
        int threadCount = 4; // Anzahl der Threads (z. B. 4 für ein Quad-Core-System)
        String folderPath = "C:\\Users\\admin\\Downloads\\linux-master\\linux-master\\tools";
        String regex = ".*\\.(c|h)";

        // Startzeit für die Messung
        long startTime = System.currentTimeMillis();

        // Ausführung des countLinesInAllFilesParallel mit angegebener Thread-Anzahl
        long totalLines = countLinesInAllFilesParallel(folderPath, regex, threadCount);
        System.out.println("Anzahl der Zeilen: " + totalLines);

        // Endzeit und Dauer
        long endTime = System.currentTimeMillis();
        System.out.println("Ausführungszeit: " + (endTime - startTime) + " Millisekunden");
    }

    // Methode zum Zählen der Zeilen in einer Datei
    public static long countLines(String fileName) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.count();
        }
    }

    // Methode zum Zählen der Zeilen in allen Dateien, unter Verwendung einer bestimmten Anzahl von Threads
    public static long countLinesInAllFilesParallel(String folderPath, String regex, int threadCount) throws IOException {
        Pattern pattern = Pattern.compile(regex);

        // Erstellung eines benutzerdefinierten ForkJoinPool mit der angegebenen Thread-Anzahl
        ForkJoinPool customThreadPool = new ForkJoinPool(threadCount);

        // Ausführung des Streams im benutzerdefinierten Pool
        try {
            return customThreadPool.submit(() -> {
                try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
                    return paths.filter(Files::isRegularFile)
                            .filter(path -> pattern.matcher(path.toString()).matches())
                            .parallel() // Parallelisieren des Streams
                            .mapToLong(path -> {
                                try {
                                    return countLines(path.toString());
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            }).sum();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            customThreadPool.shutdown();
        }
    }
}

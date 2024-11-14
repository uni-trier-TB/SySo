package de.unitrier.bicanic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.concurrent.ForkJoinPool;

public class Counterparallel {

    public static void main(String[] args) throws IOException {
        assert (args.length == 2);

        int Threads = 16;  // Anzahl der gewünschten parallelen Threads
        ForkJoinPool customThreadPool = new ForkJoinPool(Threads);

        long startTime = System.currentTimeMillis();
        long totalLines = customThreadPool.submit(() ->
                countLinesInAllFiles(args[0], args[1])
        ).join();
        long endTime = System.currentTimeMillis();

        System.out.println("Total Lines: " + totalLines);
        System.out.println("Time Taken: " + (endTime - startTime) / 1000.0 + " seconds");

        customThreadPool.shutdown();  // Schließt den benutzerdefinierten ForkJoinPool
    }

    public static long countLines(String fileName) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.count();
        }
    }

    public static long countLinesInAllFiles(String folderPath, String regex) throws IOException {
        Pattern pattern = Pattern.compile(regex);

        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.toString()).matches())
                    .parallel()  // Parallele Verarbeitung aktivieren
                    .mapToLong(path -> {
                        try {
                            return countLines(path.toString());
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .sum();
        }
    }
}
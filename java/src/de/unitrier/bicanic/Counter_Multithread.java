package de.unitrier.bicanic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.concurrent.ForkJoinPool;
import java.nio.charset.StandardCharsets;


public class Counter_Multithread {

    public static void main(String[] args) throws IOException, InterruptedException {

        assert (args.length == 3);

        // Assign number of parallel threads via third argument
        int numThreads = Integer.parseInt(args[2]);
        ForkJoinPool Threadpool = new ForkJoinPool(numThreads);

		    // start of timer with use of System.currentTimeMillies()
        long start = System.currentTimeMillis();
        long countLines = Threadpool.submit(() -> countLinesInAllFiles(args[0], args[1])).join();
        long end = System.currentTimeMillis();

        System.out.println("Total counted Lines: " + countLines + "\nTime Taken: " + (end - start)/1000.0 + " seconds");

        Threadpool.shutdown();

    }

    public static long countLines(String fileName) throws IOException {

        try (Stream<String> lines = Files.lines(Paths.get(fileName), StandardCharsets.ISO_8859_1)) {
            return lines.count();
        }

    }

    public static long countLinesInAllFiles(String folderPath, String regex) throws IOException {

        Pattern pattern = Pattern.compile(regex);

        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.toString()).matches())
                    .parallel()
                    .mapToLong(path -> {
                        try {
                            return countLines(path.toString());
                        } catch (IOException | UncheckedIOException e) {
                            if (e.getCause() instanceof java.nio.file.AccessDeniedException) {
                                System.err.println("Zugriff verweigert auf: " + path + " - " + e.getMessage());
                            } else {
                                System.err.println("Fehler beim Lesen der Datei: " + path + " - " + e.getMessage());
                            }
                            return 0;
                        }
                    }).sum();
        }
    }

}

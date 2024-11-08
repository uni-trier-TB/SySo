package de.unitrier.bicanic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// Regular expression ".*\.txt$" matches any txt source files
// args[0] = "path" args[1] = "regex" args[2] = "number of threads"

public class Counter {

    public static void main(String[] args) throws IOException {
        assert (args.length == 3);

        String folderPath = args[0];
        String regex = args[1];
        int numThreads = Integer.parseInt(args[2]);

        // Measure start time
        long startTime = System.currentTimeMillis();

        // Use a custom ForkJoinPool with the specified number of threads
        ForkJoinPool customThreadPool = new ForkJoinPool(numThreads);
        long totalLines = customThreadPool.submit(() -> countLinesInAllFiles(folderPath, regex)).join();

        // Measure end time
        long endTime = System.currentTimeMillis();

        // Print results
        System.out.println("Total lines: " + totalLines);
        System.out.println("Execution time: " + (endTime - startTime) + " ms");

        customThreadPool.shutdown(); // Shutdown the pool
    }

    public static long countLines(String fileName) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.count();
        }
    }

    public static long countLinesInAllFiles(String folderPath, String regex) throws IOException {
        Pattern pattern = Pattern.compile(regex);

        // Use parallel stream within the custom ForkJoinPool
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths.parallel()
						.filter(Files::isRegularFile)
                        .filter(path -> pattern.matcher(path.toString()).matches())
                        .mapToLong(path -> {
                            // Print the current thread name and the file path
                            System.out.println("Thread " + Thread.currentThread().getName() + " processing file: " + path);
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
package de.unitrier.bicanic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// args[0] = "path" args[1] = "regex" args[2] = "number of threads"

public class Counter {

    public static void main(String[] args) throws IOException, InterruptedException {
        assert (args.length == 3);

        String folderPath = args[0];
        String regex = args[1];
        int numThreads = Integer.parseInt(args[2]);

        // Measure start time
        long startTime = System.currentTimeMillis();

        // Create a custom thread pool
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        long totalLines = countLinesInAllFiles(folderPath, regex, executor);

        // Shut down the executor and wait for all tasks to complete
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        // Measure end time
        long endTime = System.currentTimeMillis();

        // Print results
        System.out.println("Total lines: " + totalLines);
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
    }

    public static long countLines(String fileName) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.count();
        }
    }

    public static long countLinesInAllFiles(String folderPath, String regex, ExecutorService executor) throws IOException {
        Pattern pattern = Pattern.compile(regex);

        // Collect all matching file paths first to start parallel processing
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            List<Path> matchingPaths = paths.filter(Files::isRegularFile)
                                            .filter(path -> pattern.matcher(path.toString()).matches())
                                            .collect(Collectors.toList());

            // Use CompletableFutures to process each file asynchronously
            List<CompletableFuture<Long>> futures = matchingPaths.stream()
                    .map(path -> CompletableFuture.supplyAsync(() -> {
                        System.out.println("Thread " + Thread.currentThread().getName() + " processing file: " + path);
                        try {
                            return countLines(path.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }, executor))
                    .collect(Collectors.toList());

            // Sum up the results from each future
            return futures.stream()
                          .mapToLong(CompletableFuture::join)
                          .sum();
        }
    }
}

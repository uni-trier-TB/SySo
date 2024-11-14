package de.unitrier.bicanic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Counter {

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: java Counter <folderPath> <regex> <numThreads>");
			System.exit(1);
		}

		try {
			long startTime = System.currentTimeMillis();
			long totalLines = countLinesInAllFilesUsingThreads(args[0], args[1], Integer.parseInt(args[2]));
			long endTime = System.currentTimeMillis();
			System.out.println("Total lines: " + totalLines);
			System.out.println("Execution time: " + (endTime - startTime) + " milliseconds");
		} catch (IOException | InterruptedException | ExecutionException e) {
			System.err.println("Error occurred: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static long countLines(String fileName) throws IOException {
		try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
			return lines.count();
		}
	}

	public static long countLinesInAllFiles(String folderPath, String regex) throws IOException {
		Pattern pattern = Pattern.compile(regex);
		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			return paths.filter(Files::isRegularFile).filter(path -> pattern.matcher(path.toString()).matches())
					.mapToLong(path -> {
						try {
							return countLines(path.toString());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}).sum();
		}
	}

	public static long countLinesInAllFilesUsingThreads(String folderPath, String regex, int numThreads)
			throws IOException, InterruptedException, ExecutionException {
		Pattern pattern = Pattern.compile(regex);
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);

		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			List<Callable<Long>> tasks = paths.filter(Files::isRegularFile)
					.filter(path -> pattern.matcher(path.toString()).matches())
					.map(path -> (Callable<Long>) () -> countLines(path.toString()))
					.collect(Collectors.toList());

			List<Future<Long>> results = executor.invokeAll(tasks);
			long totalLines = 0;
			for (Future<Long> result : results) {
				totalLines += result.get();
			}
			return totalLines;
		} finally {
			executor.shutdown();
		}
	}
}


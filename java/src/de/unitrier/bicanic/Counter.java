package de.unitrier.bicanic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Counter {

	public static void main(String[] args) throws IOException {
		assert (args.length == 2);
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "3");
		System.out.println(countLinesInAllFiles(args[0], args[1]));
	}

	public static long countLines(String fileName) throws IOException {
		try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
			return lines.count();
		}
	}

	public static long countLinesInAllFiles(String folderPath, String regex) throws IOException {
		long startTime = System.nanoTime();
		long sum;
		Pattern pattern = Pattern.compile(regex);
		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			sum = paths.filter(Files::isRegularFile).filter(path -> pattern.matcher(path.toString()).matches())
					.mapToLong(path -> {
						try {
							return countLines(path.toString());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					})
					.parallel() //Java Magie
					.sum();
		}
		long endTime = System.nanoTime();
		System.out.println("Laufzeit: " + ((endTime - startTime) / 1_000_000) + " ms");
		return sum;
	}
}

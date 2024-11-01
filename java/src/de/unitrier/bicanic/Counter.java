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
		System.out.println(countLinesInAllFiles(args[0], args[1]));
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

}

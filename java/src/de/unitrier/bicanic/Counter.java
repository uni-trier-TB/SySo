package de.unitrier.bicanic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Counter {

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length != 3) {
			System.err.println("Usage: java Counter <folderPath> <regex> <numThreads>");
			System.exit(1);
		}

		String folderPath = args[0];
		String regex = args[1];
		int numThreads = Integer.parseInt(args[2]); // Anzahl Threads

		// Zeitmessung
		long startTime = System.nanoTime();
		long totalLines = countLinesInAllFiles(folderPath, regex, numThreads);
		long endTime = System.nanoTime();

		long duration = (endTime - startTime) / 1_000_000;
		System.out.println("Anzahl von Zeilen: " + totalLines);
		System.out.println("Mit " + numThreads + " Threads: " + duration + " ms");
	}

	public static long countLines(String fileName) throws IOException {
		try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
			return lines.count();
		}
	}

	public static long countLinesInAllFiles(String folderPath, String regex, int numThreads) throws IOException, InterruptedException {
		Pattern pattern = Pattern.compile(regex);

		List<Path> files;
		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			files = paths.filter(Files::isRegularFile)
					.filter(path -> pattern.matcher(path.toString()).matches())
					.toList();
		}

		// List von threads
		List<LineCounterThread> threads = new ArrayList<>();

		// Thread für jede Datei starten
		for (Path file : files) {
			LineCounterThread thread = new LineCounterThread(file);
			threads.add(thread);
			thread.start();
		}


		long totalLines = 0;
		for (LineCounterThread thread : threads) {
			thread.join();
			totalLines += thread.getLineCount();
		}

		return totalLines;
	}
}


class LineCounterThread extends Thread {
	private final Path file;
	private long lineCount = 0;

	public LineCounterThread(Path file) {
		this.file = file;
	}

	@Override
	public void run() {
		try {
			lineCount = Counter.countLines(file.toString());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public long getLineCount() {
		return lineCount;
	}
}

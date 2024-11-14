package de.unitrier.bicanic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class LineCounterParallel {

    // static List<Future<Integer>> futures = new ArrayList<>();
    // private static int fileCounter = 0;
    //private static long lineCounter = 0;

    public static String folderPath;
    public static String regrex;

    private static final int maxConcurrentThreads = 128; // Maximale Anzahl an parallelen Threads

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        assert (args.length == 2);

        folderPath = args[0];
        regrex = args[1];

        // Wir lassen das Program jeweils 4 mal für jeweils 1 bis maxThreads laufen.
        for(int i = 8; i<= maxConcurrentThreads; i=i+4){
            for(int j = 1; j<=4; j++){
                countController(i, j);
            }
        }

    }

    public static void countController(int maxThreads, int currentRun) throws ExecutionException, InterruptedException, IOException {

        List<Future<Integer>> futures = new ArrayList<>();
        int fileCounter = 0;
        int lineCounter = 0;
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        countLinesInAllFiles(executor, futures);
        for (Future<Integer> future : futures) {
            lineCounter += future.get();
            fileCounter++;
        }
        executor.shutdown();

        // Berechnung der benötigten Zeit
        long endTime = System.currentTimeMillis();
        System.out.println("Anzahl Threads: " + maxThreads + "\tDurchlauf Nr.: " + currentRun);
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("File counter: " + fileCounter + "\tLine counter: " + lineCounter);
        System.out.println(" ");


    }

    public static void countLinesInAllFiles(ExecutorService executor, List<Future<Integer>> futures) throws IOException {
        Pattern pattern = Pattern.compile(regrex);
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {


            paths.filter(Files::isRegularFile).filter(path -> pattern.matcher(path.toString()).matches())
                    .forEach(path -> {

                        Callable<Integer> task = new CounterTask(path.toString());
                        futures.add(executor.submit(task));

                    });


        }
    }

}

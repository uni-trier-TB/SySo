package de.unitrier.bicanic;


import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ParallelCounter {

    public static void main(String[] args) throws IOException, InterruptedException {
        assert (args.length == 3);
        String folderPath = args[0];
        String regex = args[1];
        int numThreads = Integer.parseInt(args[2]);

        long startTime = System.currentTimeMillis();
        System.out.println(countLinesInAllFiles(folderPath, regex, numThreads));
        long endTime = System.currentTimeMillis();
        System.out.println("Execution Time: " + (endTime - startTime) + " ms");
    }

    public static long countLines(String fileName) throws IOException{
        try(Stream<String> lines = Files.lines((Paths.get(fileName)))) {
            return lines.count();
        }
    }

    public static long countLinesInAllFiles(String folderPath, String regex, int numThreads) throws IOException, InterruptedException {
        Pattern pattern = Pattern.compile(regex);
        List<Path> filesProcess = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(path.toString()).matches())
                    .forEach(filesProcess::add);
        }

        List<Thread> threads = new ArrayList<>();

        AtomicLong totalLineCount = new AtomicLong(0);

        int chunkSize = (int) Math.ceil((double) filesProcess.size() / numThreads);

        for(int i = 0; i < numThreads; i++){
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, filesProcess.size());
            List<Path> subList = filesProcess.subList(start, end);

            Thread thread = new Thread(() -> {
                long localCount = 0;
                for(Path path : subList){
                    try{
                        localCount += countLines(path.toString());
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
                totalLineCount.addAndGet(localCount);
            });

            threads.add(thread);
            thread.start();
        }

        for(Thread thread : threads){
            thread.join();
        }

        return totalLineCount.get();
    }


}

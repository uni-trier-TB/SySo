package de.unitrier.bicanic;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class CounterTask implements Callable<Integer> {

    private String path;

    public CounterTask(String path) {
        this.path = path;
    }

    @Override
    public Integer call() throws Exception {
        List<Charset> charsets = Arrays.asList(StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1, StandardCharsets.US_ASCII);
        try (Stream<String> lines = Files.lines(Paths.get(path.toString()))) {
            for (Charset charset : charsets) {
                return (int) lines.count();
            }
        }catch(Exception e){
            System.out.println("Dateicodierung konnte nicht erkannt werden. Datei: " + path.toString());
        }
        return 0;
    }
}

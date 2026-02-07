package ore.forge;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Profiler {
    public static final Profiler INSTANCE = new Profiler();
    private final List<Float> frameTimes;
    private final List<Integer> frameRates;

    private Profiler() {
        frameTimes = new ArrayList<>(864_000);
        frameRates = new ArrayList<>(864_000);
    }

    public void log(float timeMillis, int fps) {
        frameTimes.add(timeMillis);
        frameRates.add(fps);
    }

    /**
     * Writes a CSV to ./profiler-dumps/ with columns:
     * index,time_ms,fps
     */
    public void dumpToFile() {
        // Snapshot so you don’t risk concurrent modification if log() is called while dumping
        final List<Float> times;
        final List<Integer> fpss;

        synchronized (this) {
            times = new ArrayList<>(frameTimes);
            fpss = new ArrayList<>(frameRates);
        }

        int n = Math.min(times.size(), fpss.size());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String fileName = "profile-" + LocalDateTime.now().format(fmt) + ".csv";

        Path dir = Paths.get("profiler-dumps");
        Path file = dir.resolve(fileName);

        try {
            Files.createDirectories(dir);
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                w.write("index,time_ms,fps");
                w.newLine();

                for (int i = 0; i < n; i++) {
                    // Locale.ROOT prevents commas as decimal separators in some locales
                    String line = String.format(Locale.ROOT, "%d,%.6f,%d", i, times.get(i), fpss.get(i));
                    w.write(line);
                    w.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write profiler CSV to " + file.toAbsolutePath(), e);
        }
    }

}


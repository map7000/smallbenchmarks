package ru.mfilatov.smallbenchmarks;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class FileHashBenchmark {
    @Param({"SHA-256", "SHA3-256"})
    public String algorithm;
    private final static String fileName = "test.tmp";
    private final static FileHashBenchmark bench = new FileHashBenchmark();


    static {
        bench.createFile(fileName, 50 * 1024 * 1024);
    }
    @SneakyThrows
    public static void createFile(String fileName, Integer size) {
        Path fullPath = Paths.get(fileName);
        Files.deleteIfExists(fullPath);
        Files.createFile(fullPath);

        byte[] arr = new byte[size];
        new Random().nextBytes(arr);

        Files.write(fullPath, arr);
    }

    @SneakyThrows
    public String javaCoreHash(String filename, String algorithm){
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(Files.readAllBytes(Paths.get(filename)));
        byte[] digest = md.digest();
        String s = Base64.getEncoder().encodeToString(digest);
        StringBuffer sb = new StringBuffer();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        md.reset();
        return sb.toString();
    }

    @SneakyThrows
    public static String openSslHash(String fileName, String algorithm){
        return new ProcessExecutor().command("openssl", "dgst", "-r","-"+algorithm, fileName)
                .readOutput(true).execute()
                .outputUTF8().substring(0,64);
    }

    @Benchmark
    public void javaCoreHashJmh() {
        bench.javaCoreHash(fileName, algorithm);
    }

    @Benchmark
    public void openSslHashJmh(){
        bench.openSslHash(fileName, algorithm);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FileHashBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}

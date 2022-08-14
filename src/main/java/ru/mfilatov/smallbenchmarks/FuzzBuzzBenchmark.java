package ru.mfilatov.smallbenchmarks;

import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class FuzzBuzzBenchmark {
    @Param({"100", "1000", "1000000", "1000000000"})
    public int size;

    public static OutputStream out = OutputStream.nullOutputStream();
    public static FuzzBuzzBenchmark fuzzBuzzBenchmark = new FuzzBuzzBenchmark();
    final static byte[] fuzz = "Fuzz".getBytes();
    final static byte[] buzz = "Buzz".getBytes();

    @SneakyThrows
    public void fuzzBuzzCounter(int size, OutputStream out) {
        byte count3 = 0;
        byte count5 = 0;
        for (int i = 1; i <= size; i++) {
            count3++;
            count5++;
            if (count3 == 3) {
                count3 = 0;
                out.write(fuzz);
            }
            if (count5 == 5) {
                out.write(buzz);
                count5 = 0;
            }
            if (count5 != 0 && count3 != 0) out.write(i);
            out.write('\n');
        }
    }

    @SneakyThrows
    public void fuzzBuzzModulu(int size, OutputStream out) {
        boolean mod3;
        boolean mod5;
        for (int i = 1; i <= size; i++) {
            mod3 = i % 3 == 0;
            mod5 = i % 5 == 0;
            if (mod3) out.write(fuzz);
            if (mod5) out.write(buzz);
            if (!mod3 || !mod5) out.write(i);
            out.write('\n');
        }
    }

    @Benchmark
    public void fuzzBuzzCounterJmh() {
        fuzzBuzzBenchmark.fuzzBuzzCounter(size, out);
    }

    @Benchmark
    public void fuzzBuzzModuluJmh() {
        fuzzBuzzBenchmark.fuzzBuzzModulu(size, out);
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FuzzBuzzBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}

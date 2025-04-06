package org.example;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException, IOException {
        long startTimeSeq = System.nanoTime();
        // Виконання для звичайної версії
        GeneticPathFinder.main(args);
        long endTimeSeq = System.nanoTime();
        long durationSeq = (endTimeSeq - startTimeSeq) / 1_000_000;
        System.out.println("Execution Time (Sequential): " + durationSeq + " ms");

        long startTimeParallel = System.nanoTime();
        // Виконання для розпаралеленої версії
        ParallelGeneticPathFinder.main(args);
        long endTimeParallel = System.nanoTime();
        long durationParallel = (endTimeParallel - startTimeParallel) / 1_000_000;
        System.out.println("Execution Time (Parallel): " + durationParallel + " ms");

        // Обчислення прискорення
        double speedup = (double) durationSeq / durationParallel;
        System.out.println("Speedup: " + speedup);
    }

}
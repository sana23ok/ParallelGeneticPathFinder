package org.example;

import java.io.IOException;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException, IOException {
        // Вимірювання часу для ShortestPathGA (без паралельності)
        long startTimeGA = System.nanoTime();
        ShortestPathGA.run();
        long endTimeGA = System.nanoTime();
        long durationGA = endTimeGA - startTimeGA;

        // Вимірювання часу для ShortestPathGAParallel (з паралельністю)
        long startTimeGAParallel = System.nanoTime();
        ShortestPathGAParallel.run();
        long endTimeGAParallel = System.nanoTime();
        long durationGAParallel = endTimeGAParallel - startTimeGAParallel;

        // Виведення результатів
        System.out.println("Time for ShortestPathGA: " + durationGA + " ns");
        System.out.println("Time for ShortestPathGAParallel: " + durationGAParallel + " ns");

        // Обчислення прискорення
        if (durationGA != 0) {
            double speedup = (double) durationGA / durationGAParallel;
            System.out.println("Speedup: " + speedup);
        } else {
            System.out.println("Time for ShortestPathGA is too small to calculate speedup.");
        }


//        long startTimeSeq = System.nanoTime();
//        // Виконання для звичайної версії
//        GeneticPathFinder.main(args);
//        long endTimeSeq = System.nanoTime();
//        long durationSeq = (endTimeSeq - startTimeSeq) / 1_000_000;
//        System.out.println("Execution Time (Sequential): " + durationSeq + " ms");
//
//        long startTimeParallel = System.nanoTime();
//        // Виконання для розпаралеленої версії
//        ParallelGeneticPathFinder.main(args);
//        long endTimeParallel = System.nanoTime();
//        long durationParallel = (endTimeParallel - startTimeParallel) / 1_000_000;
//        System.out.println("Execution Time (Parallel): " + durationParallel + " ms");
//
//        // Обчислення прискорення
//        double speedup = (double) durationSeq / durationParallel;
//        System.out.println("Speedup: " + speedup);
    }

}
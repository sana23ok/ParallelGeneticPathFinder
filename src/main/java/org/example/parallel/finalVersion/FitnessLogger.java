package org.example.parallel.finalVersion;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FitnessLogger {
    public static void saveFitnessHistoryToCSV(Map<Integer, List<Integer>> fitnessHistoryByIsland, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Заголовок
            writer.append("Generation,Island,Fitness\n");

            for (Map.Entry<Integer, List<Integer>> entry : fitnessHistoryByIsland.entrySet()) {
                int islandId = entry.getKey();
                List<Integer> fitnessValues = entry.getValue();

                for (int generation = 0; generation < fitnessValues.size(); generation++) {
                    writer.append(String.format("%d,%d,%d\n", generation, islandId, fitnessValues.get(generation)));
                }
            }

            System.out.println("Saved to: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package pl.uncertainflowshopsolver.testdata;

import pl.agh.bo.algo.FlowShop;
import pl.agh.bo.algo.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowshopParser {

    public static FlowShop parseFlowShop(String text) {
        if (validateText(text)) {
            String[] lines = text.split("\\r?\\n");

            String[] firstLine = lines[0].split("\\s+");
            int numberOfJobs = Integer.parseInt(firstLine[0]);
            int numberOfMachines = Integer.parseInt(firstLine[1]);

            List<Task> taskList = new ArrayList<Task>(numberOfJobs);

            for (int i = 1; i < lines.length; i++) {
                taskList.add(new Task(parseLine(lines[i]), i));
            }
            return new FlowShop(taskList);
        } else {
            return null;
        }
    }

    public static FlowShop parseFlowShop(BufferedReader input) {
        try {
            Map<Integer, List<Integer>> taskMap = new HashMap<Integer, List<Integer>>();
            String[] firstLine = input.readLine().split("\\s+");
            int numberOfJobs = Integer.parseInt(firstLine[0]);
            int numberOfMachines = Integer.parseInt(firstLine[1]);

            for (int i = 0; i<numberOfJobs; i++) {
                taskMap.put(i, new ArrayList<Integer>());
            }

            String line = null;
            while ((line = input.readLine()) != null) {
                String[] taskOnMachine = line.split(" ");
                for (int i = 0; i<taskOnMachine.length; i++) {
                    taskMap.get(i).add(Integer.parseInt(taskOnMachine[i]));
                }
            }

            List<Task> taskList = new ArrayList<Task>(numberOfJobs);
            for (int i = 0; i<numberOfJobs; i++){
                taskList.add(new Task(taskMap.get(i), i+1));
            }

            return new FlowShop(taskList);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean validateText(String text) {
        return true;
    }

    private static List<Integer> parseLine(String line) {
        String[] times = line.split("\\s+");

        List<Integer> integerTimes = new ArrayList<Integer>();
        for (String time : times) {
            integerTimes.add(Integer.valueOf(time));
        }

        return integerTimes;
    }
}

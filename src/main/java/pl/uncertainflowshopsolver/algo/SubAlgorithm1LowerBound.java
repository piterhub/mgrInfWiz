package pl.uncertainflowshopsolver.algo;

import pl.uncertainflowshopsolver.flowshop.FlowShop;
import pl.uncertainflowshopsolver.flowshop.Task;
import pl.uncertainflowshopsolver.testdata.FlowshopParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by PKUBICKI on 18.04.2016.
 */
public class SubAlgorithm1LowerBound {

    /**
     * Calculates lower bound for the deterministic Flow Shop
     * @param flowShop the {@link FlowShop} with certain task list
     */
    public static Integer calculateLowerBoundOfCMax(FlowShop flowShop) {

        //System.out.println(flowShop.toString());

        int lowerBound = 0;

        for (int machine = 0; machine < flowShop.getMachineCount(); machine++) {

            int sum_of_task_in_row = 0;
            int old_sum_of_predecessors = 0;
            int old_sum_of_successors = 0;

            for (Task task: flowShop.getTaskList()) {
                sum_of_task_in_row += task.getTimeList().get(machine);

                int new_sum_of_predecessors = 0;
                for (int j = 0; j < machine; j++) {
                    new_sum_of_predecessors += task.getTimeList().get(j);
                }
                final boolean initializationIsNeeded = task == flowShop.getTaskList().get(0);

                if (initializationIsNeeded)
                    old_sum_of_predecessors = new_sum_of_predecessors;
                else
                    old_sum_of_predecessors = new_sum_of_predecessors < old_sum_of_predecessors ? new_sum_of_predecessors : old_sum_of_predecessors;

                int new_sum_of_successors = 0;
                for (int l = machine + 1; l < flowShop.getMachineCount(); l++) {
                    new_sum_of_successors += task.getTimeList().get(l);
                }
                if (initializationIsNeeded)
                    old_sum_of_successors = new_sum_of_successors;
                else
                    old_sum_of_successors = new_sum_of_successors < old_sum_of_successors ? new_sum_of_successors : old_sum_of_successors;

            }

            int lowerBound_machine = old_sum_of_predecessors + sum_of_task_in_row + old_sum_of_successors;
            lowerBound = lowerBound < lowerBound_machine ? lowerBound_machine : lowerBound;
        }

        return lowerBound;
    }

    // Tmp main
    public static void main(String[] args) {

        final String PATH = "resources/certainFlowShop_hinduskizjutuba.txt";
        final String PATH2 = "resources/new_2_certainFlowShop.txt";
        final String PATH3 = "resources/Marek Sobolewski_FlowShop.txt";

        FlowShop flowShop = null;

        try {
            flowShop = FlowshopParser.parseFlowShop(new BufferedReader(new FileReader(PATH)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(flowShop != null)
            System.out.println(calculateLowerBoundOfCMax(flowShop));  //result should be 266

        try {
            flowShop = FlowshopParser.parseFlowShop(new BufferedReader(new FileReader(PATH2)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(flowShop != null)
            System.out.println(calculateLowerBoundOfCMax(flowShop));  //result should be

        try {
            flowShop = FlowshopParser.parseFlowShop(new BufferedReader(new FileReader(PATH3)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(flowShop != null)
            System.out.println(calculateLowerBoundOfCMax(flowShop));  //result should be


    }
}

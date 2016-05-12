package pl.uncertainflowshopsolver.algorithm;

import pl.uncertainflowshopsolver.flowshop.FlowShop;
import pl.uncertainflowshopsolver.flowshop.Task;

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
        System.out.println(calculateLowerBoundOfCMax(new FlowShop()));  //result should be 266
    }
}

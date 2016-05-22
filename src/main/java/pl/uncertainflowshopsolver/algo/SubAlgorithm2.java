package pl.uncertainflowshopsolver.algo;

import pl.uncertainflowshopsolver.flowshop.FlowShop;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.flowshop.Task;
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;

import java.util.ArrayList;

/**
 * @author Piotr Kubicki, created on 18.04.2016.
 */
public class SubAlgorithm2 {

//    public static Object[] solveGreedy(FlowShopWithUncertainty uncertainFlowShop, boolean lowerBound)
//    {
//
//    }

    /**
     * Solves max{p}(C_max(p,pi) - min{pi'}C_max(p,pi') with greedy approach, when uncertain version of Flow Shop is given.
     * It doesn't  change uncertainFlowShop, i.e. given FlowShop permutation is constant (changes only inside NEH).
     *
     * @param uncertainFlowShop - the {@link FlowShopWithUncertainty}
     * @param lowerBound - should calculate C_max,LB like described in "Evolutionary algo for min-max regret flow shop problem" (Cwik, Józefczyk - 2015).
     *                   When null, then both will be calculated.
     * @param printDebug boolean, true if there should be matrix result on console
     * @return result of given above optimization problem
     */
    public static Object[] solveGreedy(FlowShopWithUncertainty uncertainFlowShop, Boolean lowerBound, boolean printDebug) {//TODO PKU in the future can refactor from Boolean to e.g. Enum
        int taskCount = uncertainFlowShop.getTaskCount();
        int machineCount = uncertainFlowShop.getTasks().get(0).getLowerTimeList().size();
        int[][] matrix = new int[taskCount][machineCount];

        FlowShop certainFlowShop = new FlowShop(new ArrayList<Task>());

        for (int t = 0; t < taskCount; t++) {
            TaskWithUncertainty uncertainTask = uncertainFlowShop.getTask(t);

            //we need it to specify operational times to certain scenario:
            //UncertainTask -> CertainTask
            Task certainTask = new Task(new ArrayList<>(uncertainTask.getLowerTimeList()), uncertainTask.getOriginalPosition());
            certainFlowShop.getTaskList().add(certainTask);

            for (int m = 0; m < machineCount; m++) {

                int sum = 0;
                sum += uncertainTask.getUpperTimeList().get(m);      //do sumy dodajemy zawsze górne p_ij,
                                                            // na dolne zmieniamy poniżej, jak obecne przegra porównanie z dłuższym
                if (t > 0) { // Rows 1..n-1
                    if (m == 0) {
                        sum += matrix[t - 1][m];
                        //
                        certainTask.setTimeOfOperation(m, uncertainTask.getUpperTimeList().get(m));
                        //
                    } else {
                        if (matrix[t - 1][m] > matrix[t][m - 1])
                        {
                            sum += matrix[t - 1][m];
                            //ustaw dla tego certainTask, że czas wykonania na poprzedniej maszynie był lower
                            certainTask.setTimeOfOperation(m-1, uncertainTask.getLowerTimeList().get(m-1));
                            //ten węzeł będzie teraz należał do ścieżki krytycznej:
                            certainTask.setTimeOfOperation(m, uncertainTask.getUpperTimeList().get(m));
                        }
                        else
                        {
                            sum += matrix[t][m - 1];
                            //ustaw dla poprzedniego certainTask, że czas wykonania na tej maszynie był lower
                            final Task previousCertainTask = certainFlowShop.getTaskList().get(t - 1);
                            previousCertainTask.setTimeOfOperation(m, uncertainFlowShop.getTask(t-1).getLowerTimeList().get(m));
                            //ten węzeł będzie teraz należał do ścieżki krytycznej:
                            certainTask.setTimeOfOperation(m, uncertainTask.getUpperTimeList().get(m));
                        }
                    }
                } else { // Row 0
                    if (m > 0) {
                        sum += matrix[t][m - 1];
                    }
                    certainTask.setTimeOfOperation(m, uncertainTask.getUpperTimeList().get(m));
                }
                matrix[t][m] = sum;
            }
        }

        // Print (for debug only)
        if (printDebug) {
            System.out.println("Matrix given by SubAlgorithm2: ");
            for (int m = 0; m < machineCount; m++) {
                for (int t = 0; t < taskCount; t++) {
                    System.out.print(matrix[t][m] + " ");
                }
                System.out.println();
            }
            System.out.println();
        }

        final int max_po_p_Cmax = matrix[taskCount - 1][machineCount - 1];

        if(lowerBound == null)
        {
            Object[] result = new Object[2];
            result[0] = max_po_p_Cmax - NehAlgorithm.solve(certainFlowShop, true);
            result[1] = max_po_p_Cmax - SubAlgorithm1LowerBound.calculateLowerBoundOfCMax(certainFlowShop);
            return result;
        }
        else if(lowerBound) {
            Object[] result = new Object[1];
            result[0] = max_po_p_Cmax - NehAlgorithm.solve(certainFlowShop, true);
            return result;
        }
        else {
            Object[] result = new Object[1];
            result[0] = max_po_p_Cmax - SubAlgorithm1LowerBound.calculateLowerBoundOfCMax(certainFlowShop);
            return result;
        }
    }


    /**
     * Temporary main for testing.
     * @param args - args
     */
    public static void main(String[] args)
    {
        FlowShopWithUncertainty uncertainFlowShop = new FlowShopWithUncertainty();
        final Object[] result = solveGreedy(uncertainFlowShop, true, true);
        int x = (int) result[0];
        System.out.println(x);
        final Object[] result2 = solveGreedy(uncertainFlowShop, false, true);
        int y = (int) result2[0];
        System.out.println(y);
        final Object[] result3 = solveGreedy(uncertainFlowShop, null, true);
        x = (int) result3[0];
        y = (int) result3[1];
        System.out.println(x);
        System.out.println(y);
    }
}

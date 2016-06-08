package pl.uncertainflowshopsolver.algo;

import pl.uncertainflowshopsolver.flowshop.FlowShop;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.flowshop.Task;
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;
import pl.uncertainflowshopsolver.testdata.UncertainFlowShopParser;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Piotr Kubicki, created on 18.04.2016.
 */
public class SubAlgorithm2 {

//    public static Object[] solveGreedy(FlowShopWithUncertainty uncertainFlowShop, boolean lowerBound)
//    {
//
//    }

    /**
     * Solves z(pi) = max{p}(C_max(p,pi) - min{pi'}C_max(p,pi') with greedy approach, when uncertain version of Flow Shop is given.
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
        FlowShop[][] wcsMatrix = new FlowShop[taskCount][machineCount];

        FlowShop certainFlowShop = new FlowShop(new ArrayList<Task>());

        for (int t = 0; t < taskCount; t++) {

            TaskWithUncertainty uncertainTask = uncertainFlowShop.getTask(t);

            for (int m = 0; m < machineCount; m++) {

                if (t > 0) { // Rows 1..n-1
                    if (m == 0) {   // Col 0
                        List<Task> helperTasks = new ArrayList<>();
                        for (int task = 0; task <= t; task++) {
                            helperTasks.add(new Task(Arrays.asList((double)uncertainFlowShop.getTasks().get(task).getUpperTimeList().get(0)),task));
                        }
                        wcsMatrix[t][m] = new FlowShop(helperTasks);
                    } else {    // Cols 1..n-1

                        //P1: Do scenariusza z komórki powyżej dopisujemy nowe zadanie ustalając w nim wszystkie czasy minimalne oprócz ostatniej operacji:
                        List<Task> helperTasks = new ArrayList<>();
                        final List<Task> taskList = wcsMatrix[t - 1][m].getTaskList();
                        for (int i = 0; i < taskList.size(); i++) {
                            helperTasks.add(new Task(taskList.get(i).getTimeList(), i));
                        }
                        List<Double> helperTimeOfOperationsList = new ArrayList<>();
                        final List<Integer> lowerTimeListOfCorrespondingUncertainTask = uncertainFlowShop.getTasks().get(t).getLowerTimeList().subList(0, m);
                        for (Integer operation : lowerTimeListOfCorrespondingUncertainTask)
                        {
                            helperTimeOfOperationsList.add((double)operation);
                        }
                        helperTimeOfOperationsList.add((double)uncertainFlowShop.getTasks().get(t).getUpperTimeList().get(m));
                        helperTasks.add(new Task(helperTimeOfOperationsList, t));
                        FlowShop P1 = new FlowShop(helperTasks);

                        //P2: do scenariusza z komórki po lewej (tego co w poprzednim kroku powstał) dopisujemy nową maszynę tak, że na ostatniej operacji czas maksymalny na pozostałych minimalny
                        List<Task> helperTasks2 = new ArrayList<>();
                        final List<Task> taskList2 = wcsMatrix[t][m - 1].getTaskList();
//                        helperTasks2.addAll(taskList2);
                        for (int i = 0; i < taskList2.size(); i++) {
                            helperTasks2.add(new Task(taskList2.get(i).getTimeList(), i));
                        }

                        for (int task = 0; task < helperTasks2.size()-1; task++) {
                            Task helperTask = helperTasks2.get(task);
                            helperTask.addOperation((double)uncertainFlowShop.getTasks().get(task).getLowerTimeList().get(m));
                        }
                        helperTasks2.get(helperTasks2.size()-1).addOperation((double)uncertainFlowShop.getTasks().get(helperTasks2.size()-1).getUpperTimeList().get(m));
                        FlowShop P2 = new FlowShop(helperTasks2);

                        final double resultForP1 = P1.makeSpan() - SubAlgorithm1LowerBound.calculateLowerBoundOfCMax(P1);
                        final double resultForP2 = P2.makeSpan() - SubAlgorithm1LowerBound.calculateLowerBoundOfCMax(P2);

                        if (resultForP1 > resultForP2)  //shouldPreviousTaskOnThisMachineCreateACriticalPath
                        {
                            wcsMatrix[t][m] = P1;

                            if(t == taskCount-1 && m == machineCount-1)
                            {
                                Object[] result = new Object[2];
                                result[0] = resultForP1;
                                result[1] = resultForP1;
                                return result;
                            }
                        }
                        else    //shouldThisTaskOnThePreviousMachineCreateACriticalPath
                        {
                            wcsMatrix[t][m] = P2;

                            if(t == taskCount-1 && m == machineCount-1)
                            {
                                Object[] result = new Object[2];
                                result[0] = resultForP2;
                                result[1] = resultForP2;
                                return result;
                            }
                        }
                    }
                } else { // Row 0
                    if (m > 0) {    // Cols 1..n-1
                        List<Double> helperOperations = new ArrayList<>();
                        for (int machine = 0; machine <= m; machine++) {
                            helperOperations.add((double)uncertainFlowShop.getTasks().get(0).getUpperTimeList().get(machine));
                        }
                        wcsMatrix[t][m] = new FlowShop(Arrays.asList(new Task(helperOperations,0)));
                    }
                    else    // Col 0
                    {
                        wcsMatrix[t][m] = new FlowShop(Arrays.asList(new Task(Arrays.asList((double)uncertainFlowShop.getTasks().get(0).getUpperTimeList().get(0)), 0)));//todo do pominięcia
                    }
                }

            }
        }

        // Print (for debug only)
        if (printDebug) {
            System.out.println("Matrix given by SubAlgorithm2: ");
            for (int t = 0; t < taskCount; t++) {
                for (int m = 0; m < machineCount; m++) {
                    System.out.print(wcsMatrix[t][m]);
                }
                System.out.println();
            }
            System.out.println();
        }

//        System.out.println();
//        System.out.println(certainFlowShop.toString());

//        final int max_po_p_Cmax = wcsMatrix[taskCount - 1][machineCount - 1];

//        if(lowerBound == null)
//        {
//            Object[] result = new Object[2];
//            result[0] = max_po_p_Cmax - NehAlgorithm.solve(certainFlowShop, true);  //z_LB
//            result[1] = max_po_p_Cmax - SubAlgorithm1LowerBound.calculateLowerBoundOfCMax(certainFlowShop); //z_UB. z_UB > z_LB!
//            return result;
//        }
//        else if(lowerBound) {
//            Object[] result = new Object[1];
//            result[0] = max_po_p_Cmax - NehAlgorithm.solve(certainFlowShop, true);
//            return result;
//        }
//        else {
//            Object[] result = new Object[1];
//            result[0] = max_po_p_Cmax - SubAlgorithm1LowerBound.calculateLowerBoundOfCMax(certainFlowShop);
//            return result;
//        }

        return null;
    }


//    private static List<Task> getSmallTaskList(FlowShopWithUncertainty uncertainFlowShop, int t, int m) {
//
//
//    }


    /**
     * Temporary main for testing.
     * @param args - args
     */
    public static void main(String[] args)
    {
//        FlowShopWithUncertainty uncertainFlowShop = new FlowShopWithUncertainty();
//        final Object[] result = solveGreedy(uncertainFlowShop, true, true);
//        int x = (int) result[0];
//        System.out.println(x);
//        final Object[] result2 = solveGreedy(uncertainFlowShop, false, true);
//        int y = (int) result2[0];
//        System.out.println(y);
//        final Object[] result3 = solveGreedy(uncertainFlowShop, null, true);
//        x = (int) result3[0];
//        y = (int) result3[1];
//        System.out.println(x);
//        System.out.println(y);

        final String PATH = "resources/new_1.txt";

        FlowShopWithUncertainty flowShopWithUncertainty = null;

        try {
            flowShopWithUncertainty = UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(flowShopWithUncertainty.toString());

        if(flowShopWithUncertainty != null)
        {
            final Object[] result4 = solveGreedy(flowShopWithUncertainty, null, true);
            double x = (double) result4[0];
            double y = (double) result4[1];
            System.out.println(x);
            System.out.println(y);
        }
    }
}

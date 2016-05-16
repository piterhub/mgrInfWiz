package pl.uncertainflowshopsolver.algo;

import pl.uncertainflowshopsolver.flowshop.FlowShop;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.flowshop.Task;
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;
import pl.uncertainflowshopsolver.testdata.InstanceGenerator;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author Piotr Kubicki, created on 07.05.2016.
 */
public class MIH {

    private FlowShopWithUncertainty uncertainFlowShop;

    public MIH(FlowShopWithUncertainty uncertainFlowShop) {
        this.uncertainFlowShop = uncertainFlowShop;
    }

    public Object[] solve(boolean lowerBound, boolean printDebug) {
        //1. The main part -> determinization of uncertain flow shop and solve it with NEH. That's because we measure it's time.
        long startTime = System.currentTimeMillis();
        final FlowShop determinedFlowShop = getDeterminedFlowShop();
        final FlowShop flowShop = NehAlgorithm.solve(determinedFlowShop);
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        //2. The evaluation part. We are getting quality of solution due to min-max regret function of cost. It's not the essential part of MIH, so we don't measure time here.
        final List<Integer> taskOrder = flowShop.getTaskOrder();

        HashMap<Integer, TaskWithUncertainty> lookupHashMap = new HashMap<>();
        for (TaskWithUncertainty uncertainTask: uncertainFlowShop.getTasks()) {
            lookupHashMap.put(uncertainTask.getOriginalPosition(),uncertainTask);
        }

        TaskWithUncertainty dummyHelperTask = new TaskWithUncertainty(Collections.<Integer>emptyList(), Collections.<Integer>emptyList(), 0);
        List<TaskWithUncertainty> newUncertainTasks = createPrefilledList(uncertainFlowShop.getTaskCount(), dummyHelperTask);
        int index = 0;
        for (Integer i : taskOrder) {
            newUncertainTasks.set(index, lookupHashMap.get(i));
            index++;
        }

        uncertainFlowShop = new FlowShopWithUncertainty(newUncertainTasks);

        final Object[] resultsInside = SubAlgorithm2.solveGreedy(uncertainFlowShop, null, printDebug);
        final int resultOfLowerBound = (int) resultsInside[0];
        final int resultOfUpperBound = (int) resultsInside[1];

        //3. The result of evaluation + measured time we give back.
        Object[] result = new Object[3];
        result[0] = resultOfLowerBound;
        result[1] = resultOfUpperBound;  //It will be in seconds

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
        otherSymbols.setDecimalSeparator(',');
        NumberFormat formatter = new DecimalFormat("#0.00");
        formatter.setGroupingUsed(false);
        result[2] = formatter.format(elapsedTime / 1000d);  //It will be in seconds

        return result;
    }

    /**
     * Helper method to become determined version of flow shop, where each task has times of executions equals to middle of intervals, i.e. p_ij = (p_ij{lower}+p_ij{upper})/2
     *
     * @return determined {@link FlowShop} as described above
     */
    private FlowShop getDeterminedFlowShop() {
        final List<TaskWithUncertainty> uncertainFlowShopTasks = uncertainFlowShop.getTasks();
        List<Task> taskList = new ArrayList<>();
        for (int i = 0; i < uncertainFlowShopTasks.size(); i++) {
            List<Integer> certainTimeList = new ArrayList<>();
            final List<Integer> uncertainTaskUpperTimeList = uncertainFlowShopTasks.get(i).getUpperTimeList();
            final List<Integer> uncertainTaskLowerTimeList = uncertainFlowShopTasks.get(i).getLowerTimeList();
            for (int j = 0; j < uncertainTaskLowerTimeList.size(); j++) {
                certainTimeList.add((uncertainTaskUpperTimeList.get(j) + uncertainTaskLowerTimeList.get(j)) / 2);
            }
            Task certainTask = new Task(certainTimeList, i);
            taskList.add(certainTask);
        }
        return new FlowShop(taskList);
    }

    public static <T> List<T> createPrefilledList(int size, T item) {
        ArrayList<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(item);
        }
        return list;
    }

    public static void main(String[] args)
    {
        InstanceGenerator instanceGenerator = new InstanceGenerator(3, 6);
        final FlowShopWithUncertainty uncertainFlowShopInstance = instanceGenerator.generateUncertainFlowShopInstance(0, 100, 50);
//        uncertainFlowShopInstance.toFile("qpa1.txt");
        MIH mih = new MIH(uncertainFlowShopInstance);
        final Object[] result = mih.solve(true, false);
        System.out.println("MIH solution: ");
        System.out.println(result[0].toString());
    }
}

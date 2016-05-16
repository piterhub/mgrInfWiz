package pl.uncertainflowshopsolver.flowshop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Piotr Kubicki, created on 18.04.2016.
 */
public class FlowShopConfig {

    // Taillard

    public static final int TEST_MACHINE = 5;

    public static final int TEST_JOBS = 4;

    private int machineCount, jobsCount;

    private FlowShop state;

    public FlowShopConfig(int machines, int jobs, FlowShop state) {
        this.machineCount = machines;
        this.jobsCount = jobs;
        this.state = state;
    }

    /**
     * If no arguments generates simple instance of flow shop problem.
     * Results should be 3,1,2.
     */
    public FlowShopConfig() {
        this.machineCount = TEST_MACHINE;
        this.jobsCount = TEST_JOBS;

//        List<Task> taskList = new ArrayList<Task>();
//        Integer[] task1 = {5, 9, 8, 10, 1};
//        taskList.add(new Task(Arrays.asList(task1), 1));
//        Integer[] task2 = {9, 3, 10, 1, 8};
//        taskList.add(new Task(Arrays.asList(task2), 2));
//        Integer[] task3 = {9, 4, 5, 8, 6};
//        taskList.add(new Task(Arrays.asList(task3), 3));
//        Integer[] task4 = {4, 8, 8, 7, 2};
//        taskList.add(new Task(Arrays.asList(task4), 4));

        //instance taken from http://new.zsd.ict.pwr.wroc.pl/lab.php?zid=NEH
        //result should be: makespan - 47, scheduling - 3,1,5,4,6,2
        List<Task> taskList = new ArrayList<Task>();
        Integer[] task1 = {1, 5, 7};
        taskList.add(new Task(Arrays.asList(task1), 1));
        Integer[] task2 = {4, 5, 3};
        taskList.add(new Task(Arrays.asList(task2), 2));
        Integer[] task3 = {1, 4, 8};
        taskList.add(new Task(Arrays.asList(task3), 3));
        Integer[] task4 = {7, 3, 9};
        taskList.add(new Task(Arrays.asList(task4), 4));
        Integer[] task5 = {3, 6, 9};
        taskList.add(new Task(Arrays.asList(task5), 5));
        Integer[] task6 = {4, 7, 6};
        taskList.add(new Task(Arrays.asList(task6), 6));

        state = new FlowShop(taskList);
    }

    public FlowShop getState() {
        return state;
    }

    public int getMachineCount() {
        return machineCount;
    }

    public int getJobsCount() {
        return jobsCount;
    }
}

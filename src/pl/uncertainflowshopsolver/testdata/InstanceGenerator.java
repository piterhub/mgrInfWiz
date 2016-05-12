package pl.uncertainflowshopsolver.testdata;

import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Piotr Kubicki, created on 26.04.2016.
 */
public class InstanceGenerator {

    private static int m;
    private static int n;
    Random r;

    /**
     * Constructor for generating the {@link FlowShopWithUncertainty}
     * @param m - machine count.
     * @param n - task count.
     */
    public InstanceGenerator(int m, int n)
    {
        this.m = m;
        this.n = n;
        r = new Random();
    }

    public FlowShopWithUncertainty generateUncertainFlowShopInstance (int lowerBoundOfLowerInterval, int upperBoundOfLowerInterval, int widthOfUncertaintyInterval)
    {
        if (lowerBoundOfLowerInterval >= upperBoundOfLowerInterval) {
            throw new IllegalArgumentException("upperBoundOfLowerInterval must be greater than lowerBoundOfLowerInterval");
        }

        List<TaskWithUncertainty> taskWithUncertaintyList = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            List<Integer> lowerTimeList = new ArrayList<>();
            List<Integer> upperTimeList = new ArrayList<>();

            for (int j = 0; j < m; j++) {
                final int lower_p_ij = r.nextInt((upperBoundOfLowerInterval - lowerBoundOfLowerInterval) + 1) + lowerBoundOfLowerInterval;
                lowerTimeList.add(lower_p_ij);

                final int upper_p_ij = r.nextInt(widthOfUncertaintyInterval+ 1) + lower_p_ij;
                upperTimeList.add(upper_p_ij);
            }
            TaskWithUncertainty uncertainTask = new TaskWithUncertainty(lowerTimeList, upperTimeList, i);
            taskWithUncertaintyList.add(uncertainTask);
        }

        return new FlowShopWithUncertainty(taskWithUncertaintyList);
    }

    public static void main(String[] args)
    {
        InstanceGenerator generator = new InstanceGenerator(3,90);
        final FlowShopWithUncertainty uncertainFlowShopInstance = generator.generateUncertainFlowShopInstance(0, 100, 50);
        System.out.println(uncertainFlowShopInstance.toString());

        String fileName = "testFile.txt";
        uncertainFlowShopInstance.toFile(fileName);
//
//        final int number = uncertainFlowShopInstance.getM() * uncertainFlowShopInstance.getN();
//        final int factorial = partOfFactorial(number, Math.round((float)number / 1.005f));
//        System.out.println(factorial);
    }
}

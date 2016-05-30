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
    public static final Random random = new Random();

    /**
     * Constructor for generating the {@link FlowShopWithUncertainty}
     * @param m - machine count.
     * @param n - task count.
     */
    public InstanceGenerator(int m, int n)
    {
        this.m = m;
        this.n = n;
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
                final int lower_p_ij = getLowerP_ij(lowerBoundOfLowerInterval, upperBoundOfLowerInterval);
                lowerTimeList.add(lower_p_ij);

                final int upper_p_ij = getUpperP_ij(widthOfUncertaintyInterval, lower_p_ij);
                upperTimeList.add(upper_p_ij);
            }
            TaskWithUncertainty uncertainTask = new TaskWithUncertainty(lowerTimeList, upperTimeList, i);
            taskWithUncertaintyList.add(uncertainTask);
        }

        return new FlowShopWithUncertainty(taskWithUncertaintyList);
    }

    private int getUpperP_ij(int widthOfUncertaintyInterval, int lower_p_ij) {
        int randomResult;
        while((randomResult = random.nextInt(widthOfUncertaintyInterval+ 1) + lower_p_ij) == 0){}
        return randomResult;
    }

    private int getLowerP_ij(int lowerBoundOfLowerInterval, int upperBoundOfLowerInterval) {
        int randomResult;
        while((randomResult = random.nextInt((upperBoundOfLowerInterval - lowerBoundOfLowerInterval) + 1) + lowerBoundOfLowerInterval) == 0){}
        return randomResult;
    }

    public static void main(String[] args)
    {
        InstanceGenerator generator = new InstanceGenerator(3,50);

        for (int i = 3; i < 11; i++) {
            FlowShopWithUncertainty uncertainFlowShopInstance = generator.generateUncertainFlowShopInstance(0, 100, 50);
            String fileName = i + "_[n50, m3, K100, C50].txt";
            uncertainFlowShopInstance.toFile(fileName, null);
        }

//        final FlowShopWithUncertainty uncertainFlowShopInstance2 = generator.generateUncertainFlowShopInstance(0, 100, 50);
//        System.out.println(uncertainFlowShopInstance2.toString());

//        String fileName2 = "testFile2.txt";
//        uncertainFlowShopInstance2.toFile(fileName2, null);
//
//        final int number = uncertainFlowShopInstance.getM() * uncertainFlowShopInstance.getN();
//        final int factorial = partOfFactorial(number, Math.round((float)number / 1.005f));
//        System.out.println(factorial);
    }
}

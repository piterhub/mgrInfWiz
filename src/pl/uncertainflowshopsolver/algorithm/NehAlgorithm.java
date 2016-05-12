package pl.uncertainflowshopsolver.algorithm;

import pl.uncertainflowshopsolver.flowshop.FlowShop;
import pl.uncertainflowshopsolver.flowshop.FlowShopConfig;

/**
 * Created by PKUBICKI on 17.04.2016.
 */
public final class NehAlgorithm {

    /**
     * Solves Flow Shop problem with NEH algorithm.
     * It should always print the same result.
     * @param config FlowShop problem config
     */
    public static void solve(FlowShopConfig config) {

        System.out.println(config.getState().toString());

        // Sort task by sum of it's time on each machine
        FlowShop result = config.getState().sortDescending();
        solve(result);
    }

    /**
     * Solves Flow Shop problem with NEH algorithm.
     * It should always print the same result.
     */
    public static FlowShop solve(FlowShop flowShop) {

//        System.out.println(flowShop.toString());

        // Sort task by sum of it's time on each machine
        FlowShop result = flowShop.sortDescending();

        // Try to find best place for each next task.
        // If the time for current task is better - swap it.
        for (int i=1; i<result.getTaskCount();i++) {
            int bestIndex = i, minSpan = result.makeSpan(i);
            for (int j=0; j<i; j++) {
                FlowShop swapped = result.move(i,j);
                // Remember better solution
                if (swapped.makeSpan(i)<minSpan) {
                    bestIndex = j;
                    minSpan = swapped.makeSpan(i);
                }
            }
            // Swap better solution
            if (bestIndex!=i) {
                result = result.move(i, bestIndex);
            }
        }

//        System.out.println(result.toString());
//        System.out.println("Makespan calculated for Neh solution: " + result.makeSpan());

        return result;
    }

    public static Integer solve(FlowShop flowShop, boolean isResultAsMakespanNecessary) {

        if(!isResultAsMakespanNecessary)
            throw new UnsupportedOperationException();

//        System.out.println("NEH - initial FlowShop:");
//        System.out.println(flowShop.toString());

        // Sort task by sum of it's time on each machine
        FlowShop currentFlowShop = flowShop.sortDescending();

        // Try to find best place for each next task.
        // If the time for current task is better - swap it.
        for (int i=1; i<currentFlowShop.getTaskCount();i++) {
            int bestIndex = i, minSpan = currentFlowShop.makeSpan(i);
            for (int j=0; j<i; j++) {
                FlowShop swapped = currentFlowShop.move(i,j);
                // Remember better solution
                if (swapped.makeSpan(i)<minSpan) {
                    bestIndex = j;
                    minSpan = swapped.makeSpan(i);
                }
            }
            // Swap better solution
            if (bestIndex!=i) {
                currentFlowShop = currentFlowShop.move(i, bestIndex);
            }
        }

//        System.out.println("NEH - solution FlowShop:");
//        System.out.println(currentFlowShop.toString());
        final Integer result = currentFlowShop.makeSpan();
//        System.out.println("Makespan calculated for Neh solution: " + result + "\n");

        return result;
    }

    // Tmp main
    public static void main(String[] args) {
        solve(new FlowShopConfig());
    }

}

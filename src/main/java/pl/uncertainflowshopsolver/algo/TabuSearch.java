package pl.uncertainflowshopsolver.algo;

import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.config.TSConfiguration;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;
import pl.uncertainflowshopsolver.gui.GUIController;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;

import java.util.*;

import static pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty.swapTwoTasksGivingNewFlowShopWithUncertainty;
import static pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher.EndingReason;

/**
 * @author Piotr Kubicki, created on 31.05.2016.
 */
public class TabuSearch {

    private static final Random random = new Random();
    private static final double MAX_TIME_OF_EXECUTION_IN_SECONDS = 300;

    private AlgorithmEventDispatcher eventDispatcher;
    private ConfigurationProvider configurationProvider;

    private TSConfiguration configuration;
    private volatile boolean running;   //volatile to avoid "visibility" problem, when the updates of one thread are not visible to other threads.

    private FlowShopWithUncertainty uncertainFlowShop;
    private TabuList mTabuList;
    private double globalMinimum;
    private int iterationWithoutImprovementCount;
    private int iterationWithoutImprovementForDiversificationPurposeCount;


    public TabuSearch(FlowShopWithUncertainty uncertainFlowShop)
    {
        this.uncertainFlowShop = uncertainFlowShop;
    }

    public TabuSearch(GUIController guiController) {
        this.configurationProvider = guiController;
        this.eventDispatcher = new AlgorithmEventDispatcher(guiController);
    }

    public TabuSearch()
    {
    }

    public void start() {
        prepareConfiguration();
        running = true;
        eventDispatcher.dispatchAlgorithmStarted();
        mTabuList = new TabuList(configuration.getUncertainFlowShop().getTaskCount(), configuration.getLengthOfTabuList());
        solve();
    }

    public void stop() {
        running = false;
    }

    private void prepareConfiguration() {
        configuration = configurationProvider.getTSConfiguration();
    }

    private void solve()
    {
        long startTime = System.currentTimeMillis();
        uncertainFlowShop = configuration.getUncertainFlowShop();

        /***********Hybridisation**************/
        MIH mih = new MIH(uncertainFlowShop);
        final Object[] mihResult = mih.solve(false, false);
        System.out.println("(TS) MIH UB result: " + (double)mihResult[1]);
        FlowShopWithUncertainty helperFlowShop = (FlowShopWithUncertainty) mihResult[3];

        final Object[] result1 = SubAlgorithm2.solveGreedy(uncertainFlowShop, null, false);
        final Object[] result2 = SubAlgorithm2.solveGreedy(helperFlowShop, null, false);
        Object[] result;
        if((double)result1[1] <= (double)result2[1])
        {
            result = result1;
        }
        else
        {
            result = result2;
        }
        /**************************************/

        /***********Global minimum**************/
        globalMinimum = (double) result[1];
        double globalMinimumForLowerBound = (double) result[0];
        FlowShopWithUncertainty uncertainFlowShop_for_minimum = uncertainFlowShop.clone();
        uncertainFlowShop_for_minimum.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);
        uncertainFlowShop_for_minimum.setLowerBoundOfMinMaxRegretOptimalization(globalMinimumForLowerBound);
        /***************************************/

        int iterations = 0;
        int lastImprovementIteration = 0;
        int lastImprovementIterationInTermOfDiversification = 0;

        long midTime_2 = System.currentTimeMillis();
        double elapsedTime_delta1 = (midTime_2 - startTime);
        long midTime_3 = 0L;
        double elapsedTime_delta3 = 0d;

        while (running){

            /***********Measure time and dispatch progress**************/
            midTime_2 = System.currentTimeMillis(); //it is not bug! this line is important!
            if(midTime_3 != 0L)
            {
                double elapsedTime_delta2 = midTime_2 - midTime_3;
                elapsedTime_delta3 += elapsedTime_delta2;
            }
            eventDispatcher.dispatchIterationUpdated(iterations, uncertainFlowShop_for_minimum);
            midTime_3= System.currentTimeMillis();
            /***********************************************************/

            iterations++;
            iterationWithoutImprovementCount = iterations - lastImprovementIteration;
            iterationWithoutImprovementForDiversificationPurposeCount = iterations - lastImprovementIterationInTermOfDiversification;

            if(isDiversificationNeeded()) {
                setBestUncertainFlowShopAfterDiversification();
                lastImprovementIterationInTermOfDiversification = iterations;
            }

            FlowShopWithUncertainty bestNeighbour = getBestNeighbour(uncertainFlowShop);
            double currentValue = bestNeighbour.getUpperBoundOfMinMaxRegretOptimalization();  //upper bound

            uncertainFlowShop = bestNeighbour.clone();
            if (globalMinimum > currentValue) {
                globalMinimum = currentValue;
                uncertainFlowShop_for_minimum = bestNeighbour.clone();
                uncertainFlowShop_for_minimum.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);
                uncertainFlowShop_for_minimum.setLowerBoundOfMinMaxRegretOptimalization((double) SubAlgorithm2.solveGreedy(uncertainFlowShop, true, false)[0]);
                lastImprovementIteration = iterations;
                lastImprovementIterationInTermOfDiversification = iterations;
            }

            // stop conditions
            if (configuration.getMaxIterationsAsStopCriterion() != 0 && iterations > configuration.getMaxIterationsAsStopCriterion()) {
                break;
            }
            if (configuration.getMaxIterationsWithoutImprovementAsStopCriterion() != 0 &&
                    iterationWithoutImprovementCount > configuration.getMaxIterationsWithoutImprovementAsStopCriterion()) {
                break;
            }

            long stopTime = System.currentTimeMillis();
            double elapsedLastPeriodOfTime = (stopTime - midTime_3) / 1000d; //in seconds
            double elapsedTime_delta4 = elapsedTime_delta3 + elapsedTime_delta1;
            double elapsedTime = (elapsedLastPeriodOfTime + elapsedTime_delta4) / 1000d; //in seconds
            elapsedTime = Math.floor(elapsedTime * 100) / 100;  //display a truncated 3.545555555 to 3.54, but rounded to 3.55.

            if (elapsedTime > MAX_TIME_OF_EXECUTION_IN_SECONDS) {
                System.out.println("\n TS MAX_TIME_OF_EXECUTION_IN_SECONDS reached. \n");
                break;
            }
        }
        long stopTime = System.currentTimeMillis();
        double elapsedLastPeriodOfTime = (stopTime - midTime_3) / 1000d; //in seconds
        double elapsedTime_delta4 = elapsedTime_delta3 + elapsedTime_delta1;
        double elapsedTime = (elapsedLastPeriodOfTime + elapsedTime_delta4) / 1000d; //in seconds
        elapsedTime = Math.floor(elapsedTime * 100) / 100;  //display a truncated 3.545555555 to 3.54, but rounded to 3.55.
        uncertainFlowShop_for_minimum.setElapsedTime(elapsedTime);

        // Last update
        eventDispatcher.dispatchIterationUpdated(iterations, uncertainFlowShop_for_minimum);

        if (iterations >= configuration.getMaxIterationsAsStopCriterion()) {
            eventDispatcher.dispatchAlgorithmEnded(EndingReason.ALL_ITERATIONS, elapsedTime, uncertainFlowShop_for_minimum);
        } else if (running) {
            eventDispatcher.dispatchAlgorithmEnded(EndingReason.WITHOUT_PROGRESS, elapsedTime, uncertainFlowShop_for_minimum);
        } else {
            eventDispatcher.dispatchAlgorithmEnded(EndingReason.CANCELLED, elapsedTime, uncertainFlowShop_for_minimum);
        }

    }

    private void setBestUncertainFlowShopAfterDiversification() {
        System.out.println("\nShuffle!");
//        for (TaskWithUncertainty task : uncertainFlowShop.getTasks())
//        {
//            System.out.print(" " + task.getOriginalPosition() + " ");
//        }
//        System.out.println();

        FlowShopWithUncertainty minimumHelperFlowShop = uncertainFlowShop.clone();
        minimumHelperFlowShop.setUpperBoundOfMinMaxRegretOptimalization(-1d);
        for (int i = 0; i < minimumHelperFlowShop.getTaskCount(); i++) {
            FlowShopWithUncertainty newFlowShop = minimumHelperFlowShop.clone();
            Collections.shuffle(newFlowShop.getTasks());
            double resultForNewValue = getObjectiveFunctionValue(newFlowShop);
            if(minimumHelperFlowShop.getUpperBoundOfMinMaxRegretOptimalization() == -1 || resultForNewValue < minimumHelperFlowShop.getUpperBoundOfMinMaxRegretOptimalization())
            {
                minimumHelperFlowShop=newFlowShop.clone();
                minimumHelperFlowShop.setUpperBoundOfMinMaxRegretOptimalization(resultForNewValue);
//                System.out.println("New UpperBoundOfMinMaxRegretOptimalization of minimumHelperFlowShop: " + minimumHelperFlowShop.getUpperBoundOfMinMaxRegretOptimalization());
            }
        }
        uncertainFlowShop = minimumHelperFlowShop.clone();
        uncertainFlowShop.setUpperBoundOfMinMaxRegretOptimalization(minimumHelperFlowShop.getUpperBoundOfMinMaxRegretOptimalization());

//        System.out.println("\nIs :");
//        for (TaskWithUncertainty task : uncertainFlowShop.getTasks())
//        {
//            System.out.print(" " + task.getOriginalPosition() + " ");
//        }
//        System.out.println();
    }

    private boolean isDiversificationNeeded() {
        if(iterationWithoutImprovementForDiversificationPurposeCount >= configuration.getMaxIterationsWithoutImprovementForDiversificationPurpose())
            return true;
        else
            return false;
    }

    private FlowShopWithUncertainty getBestNeighbour(FlowShopWithUncertainty uncertainFlowShop) {
        if(isAllNeighborhoodCalculated())   //classical flow shop
        {
            List<Integer> taskOrder = new ArrayList<>();
            for (TaskWithUncertainty taskWithUncertainty : uncertainFlowShop.getTasks())
            {
                taskOrder.add(taskWithUncertainty.getOriginalPosition());
            }

            SortedMap<Double, FlowShopWithUncertainty> notTabuNeighbours = new TreeMap<>();    //todo lepiej zamienic na bestFlowShopTillNow, jeśli aspiration criteria się jakoś sensownie nie zmieni lub nie dojdzie pamięć długoterminowa

            double bestResultTillNow = -1;
            int indexOfTabuTask1 = -1;
            int indexOfTabuTask2 = -1;

            for (int i = 0; i < uncertainFlowShop.getTaskCount()-1; i++)
            {
                for(int j = i+1; j < uncertainFlowShop.getTaskCount(); j++)
                {
                    if(isMoveNotTabuOrFulfillAspirationCriteria(taskOrder, i, j, uncertainFlowShop))
                    {
                        final FlowShopWithUncertainty neighbour = swapTwoTasksGivingNewFlowShopWithUncertainty(uncertainFlowShop, i, j);
                        final double resultForNeighbour = getObjectiveFunctionValue(neighbour);
//                        neighbour.setUpperBoundOfMinMaxRegretOptimalization(resultForNeighbour);//todo lepiej zamienic na bestFlowShopTillNow, jeśli aspiration criteria się jakoś sensownie nie zmieni lub nie dojdzie pamięć długoterminowa
                        notTabuNeighbours.put(resultForNeighbour, neighbour);
                        if(resultForNeighbour < bestResultTillNow || bestResultTillNow == -1)
                        {
                            bestResultTillNow = resultForNeighbour;
                            indexOfTabuTask1 = i;
                            indexOfTabuTask2 = j;
                        }
                    }
                }
            }
            mTabuList.addTabuMove(indexOfTabuTask1, indexOfTabuTask2);
            final FlowShopWithUncertainty bestNeighbour = notTabuNeighbours.get(notTabuNeighbours.firstKey());
            bestNeighbour.setUpperBoundOfMinMaxRegretOptimalization(notTabuNeighbours.firstKey());
            return bestNeighbour;
        }
        else //probabilistic flow shop
        {
            return null;
        }
    }

    private boolean isMoveNotTabuOrFulfillAspirationCriteria(List<Integer> taskOrder, int i, int j, FlowShopWithUncertainty uncertainFlowShop) {
        return (!mTabuList.isMoveTabu(taskOrder.get(i), taskOrder.get(j)) ||
                willNewResultBetterThanGlobalBest(i, j, uncertainFlowShop) ||
                isMaxAspirationIterationsWithoutImprovementReached());
    }

    private boolean isMaxAspirationIterationsWithoutImprovementReached() {
        return configuration.getIterationsWithoutImprovementAsAdditionalAspirationCriterion() > 1 &&
                iterationWithoutImprovementCount > configuration.getIterationsWithoutImprovementAsAdditionalAspirationCriterion();
    }

    private boolean willNewResultBetterThanGlobalBest(int i, int j, FlowShopWithUncertainty uncertainFlowShop) {
        return getObjectiveFunctionValue(swapTwoTasksGivingNewFlowShopWithUncertainty(uncertainFlowShop, i, j)) < globalMinimum;
    }

    /**
     * When user wants to calculate whole neighborhood and gives a big parameter "size of neighborhood".
     * It would be then classical Tabu Search. Otherwise - it would be probabilistic one.
     *
     * @return true if whole neighborhood
     */
    private boolean isAllNeighborhoodCalculated() {
        return configuration.getSizeOfNeighborhood() >= getMaxSizeOfNeighborhoodForGivenFlowShop();
    }

    private int getMaxSizeOfNeighborhoodForGivenFlowShop() {
        final Integer taskCount = configuration.getUncertainFlowShop().getTaskCount();
        return taskCount * (taskCount - 1) / 2;
    }

    public void setEventDispatcher(AlgorithmEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public double getObjectiveFunctionValue(FlowShopWithUncertainty uncertainFlowShop){
        return (double) SubAlgorithm2.solveGreedy(uncertainFlowShop,false,false)[0];
    }

    private class TabuList {
        private int mLengthOfTabuList;
        int [][] tabuList ;

        public TabuList(int size, int pLengthOfTabuList){
            this.tabuList = new int[size][size]; //city 0 is not used here, but left for simplicity
            this.mLengthOfTabuList = pLengthOfTabuList;
        }

        public void addTabuMove(int indexOfTask1, int indexOfTask2){ //tabus the swap operation
            if(indexOfTask1 == -1 || indexOfTask2 == -1)
                throw new IllegalArgumentException("indexOfTask1 or indexOfTask2 == -1");
            decrementTabu();
            tabuList[indexOfTask1][indexOfTask2]+= mLengthOfTabuList; // długość listy TABU
            tabuList[indexOfTask2][indexOfTask1]+= mLengthOfTabuList;
        }

        public void decrementTabu(){
            for(int i = 0; i<tabuList.length; i++){
                for(int j = 0; j<tabuList.length; j++){
                    tabuList[i][j]-=tabuList[i][j]<=0?0:1;
                }
            }
        }

        public boolean isMoveTabu(int indexOfTask1, int indexOfTask2){
            return tabuList[indexOfTask1][indexOfTask2] != 0 || tabuList[indexOfTask2][indexOfTask1] !=0;
        }
    }
}

package pl.uncertainflowshopsolver.algo;

import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.config.TSPConfiguration;
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

    private AlgorithmEventDispatcher eventDispatcher;
    private ConfigurationProvider configurationProvider;

    private TSPConfiguration configuration;
    private volatile boolean running;   //volatile to avoid "visibility" problem, when the updates of one thread are not visible to other threads.

    private FlowShopWithUncertainty uncertainFlowShop;
    private TabuList mTabuList;
    private int globalMinimum;


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
        configuration = configurationProvider.getTSPConfiguration();
    }

    private void solve()
    {
        long startTime = System.currentTimeMillis();
        uncertainFlowShop = configuration.getUncertainFlowShop();

        /***********Global minimum**************/
        final Object[] result = SubAlgorithm2.solveGreedy(uncertainFlowShop, null, false);
        globalMinimum = (int) result[1];
        int globalMinimumForLowerBound = (int) result[0];
        FlowShopWithUncertainty uncertainFlowShop_for_minimum = uncertainFlowShop.clone();
        uncertainFlowShop_for_minimum.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);
        uncertainFlowShop_for_minimum.setLowerBoundOfMinMaxRegretOptimalization(globalMinimumForLowerBound);
        /*************************/

        int valueBefore = globalMinimum;
        FlowShopWithUncertainty uncertainFlowShop_for_valueBefore = uncertainFlowShop.clone();
        uncertainFlowShop_for_valueBefore.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);     //TODO workaround. Może lepiej w clone dać kopiowanie tych fieldów
        uncertainFlowShop_for_valueBefore.setLowerBoundOfMinMaxRegretOptimalization(globalMinimumForLowerBound);

        int iterations = 0;
        int lastImprovementIteration = 0;

//        if (lastImprovementIteration % 10 == 0)
//            eventDispatcher.dispatchIterationUpdated(iterations, uncertainFlowShop_for_minimum);

        long midTime_2 = System.currentTimeMillis();
        double elapsedTime_delta1 = (midTime_2 - startTime);
        long midTime_3 = 0L;
        double elapsedTime_delta3 = 0d;

        while (running/* && initialTemperature > configuration.getEndTemperature() && iterations < configuration.getMaxNumberOfIterations()*/){

            midTime_2 = System.currentTimeMillis(); //it is not bug! this line is important!

            if(midTime_3 != 0L)
            {
                double elapsedTime_delta2 = midTime_2 - midTime_3;
                elapsedTime_delta3 += elapsedTime_delta2;
            }

            if (lastImprovementIteration % 10 == 0)
                eventDispatcher.dispatchIterationUpdated(iterations, uncertainFlowShop_for_minimum);

            midTime_3= System.currentTimeMillis();

            iterations++;

            FlowShopWithUncertainty bestNeighbour = getBestNeighbour(uncertainFlowShop);

            final FlowShopWithUncertainty neighbour = uncertainFlowShop.getNeighbourAndEvaluateIt(1.0);
            final Object[] resultInside = SubAlgorithm2.solveGreedy(neighbour, null, false);
            int currentValue = (int) resultInside[1];  //upper bound

            if (valueBefore >= currentValue) {
                valueBefore = currentValue;
                uncertainFlowShop_for_valueBefore = neighbour.clone();
                if (globalMinimum > currentValue) {
                    globalMinimum = currentValue;
                    uncertainFlowShop_for_minimum = neighbour.clone();
                    uncertainFlowShop_for_minimum.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);
                    uncertainFlowShop_for_minimum.setLowerBoundOfMinMaxRegretOptimalization((int) resultInside[0]);
                }
            } else {
                int delta = currentValue - valueBefore;
                double probability = Math.exp(-delta / initialTemperature);
                double zeroToOne = random.nextInt(1001) / 1000.0;

                if (zeroToOne <= probability) {
                    valueBefore = currentValue;
                    uncertainFlowShop_for_valueBefore = neighbour.clone();
                } else {
                    uncertainFlowShop = uncertainFlowShop_for_valueBefore.clone();
                    uncertainFlowShop.setUpperBoundOfMinMaxRegretOptimalization(uncertainFlowShop_for_valueBefore.getUpperBoundOfMinMaxRegretOptimalization());
                }
            }

            // stop conditions
            if (configuration.getMaxIterationsAsStopCriterion() != 0 && iterations > configuration.getMaxIterationsAsStopCriterion()) {
                break;
            }
//            if (configuration.getMaxIterationsWithoutImprovement() != 0 &&
//                    iterations - lastImprovementIteration > configuration.getMaxIterationsWithoutImprovement()) {
//                break;
//            }
        }
        long stopTime = System.currentTimeMillis();
        double elapsedLastPeriodOfTime = (stopTime - midTime_3) / 1000d; //in seconds
        double elapsedTime_delta4 = elapsedTime_delta3 + elapsedTime_delta1;
        double elapsedTime = (elapsedLastPeriodOfTime + elapsedTime_delta4) / 1000d; //in seconds
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

    private FlowShopWithUncertainty getBestNeighbour(FlowShopWithUncertainty uncertainFlowShop) {
        if(isAllNeighborhoodCalculated())   //classical flow shop
        {
            List<Integer> taskOrder = new ArrayList<>();
            for (TaskWithUncertainty taskWithUncertainty : uncertainFlowShop.getTasks())
            {
                taskOrder.add(taskWithUncertainty.getOriginalPosition());
            }

            SortedMap<Integer, FlowShopWithUncertainty> notTabuNeighbours = new TreeMap<>();
            for (int i = 0; i < uncertainFlowShop.getTaskCount()-1; i++)
            {
                for(int j = i+1; j < uncertainFlowShop.getTaskCount(); j++)
                {
                    if(isMoveNotTabuOrFulfillAspirationCriteria(taskOrder, i, j, uncertainFlowShop))
                    {
                        final FlowShopWithUncertainty neighbour = swapTwoTasksGivingNewFlowShopWithUncertainty(uncertainFlowShop, i, j);
                        final int resultForNeighbour = getObjectiveFunctionValue(neighbour);
                        notTabuNeighbours.put(resultForNeighbour, neighbour);
                    }
                }
            }
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
        return !mTabuList.isMoveTabu(taskOrder.get(i), taskOrder.get(j)) || (getObjectiveFunctionValue(swapTwoTasksGivingNewFlowShopWithUncertainty(uncertainFlowShop, i, j)) < globalMinimum);
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


    private Integer getTaskCount() {
        return uncertainFlowShop.getTaskCount();
    }

    public void setEventDispatcher(AlgorithmEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public int getObjectiveFunctionValue(FlowShopWithUncertainty uncertainFlowShop){
        return (int) SubAlgorithm2.solveGreedy(uncertainFlowShop,false,false)[0];
    }

    //swaps two cities
    public static int[] swapOperator(int city1, int city2, FlowShopWithUncertainty solution) {
        TaskWithUncertainty temp = solution.getTask(city1);
        solution.setTaskOrder();
        solution[city1] = solution[city2];
        solution[city2] = temp;
        return solution;
    }

    private class TabuList {
        private int mLengthOfTabuList;
        int [][] tabuList ;

        public TabuList(int size, int pLengthOfTabuList){
            this.tabuList = new int[size][size]; //city 0 is not used here, but left for simplicity
            this.mLengthOfTabuList = pLengthOfTabuList;
        }

        public void addTabuMove(int city1, int city2){ //tabus the swap operation
            tabuList[city1][city2]+= mLengthOfTabuList; // długość listy TABU
            tabuList[city2][city1]+= mLengthOfTabuList;
        }

        public void decrementTabu(){
            for(int i = 0; i<tabuList.length; i++){
                for(int j = 0; j<tabuList.length; j++){
                    tabuList[i][j]-=tabuList[i][j]<=0?0:1;
                }
            }
        }

        public boolean isMoveTabu(int city1, int city2){
            return tabuList[city1][city2] != 0 || tabuList[city2][city1] !=0;
        }
    }
}

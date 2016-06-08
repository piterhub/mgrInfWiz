package pl.uncertainflowshopsolver.algo;

import pl.uncertainflowshopsolver.algo.init.SolutionInitializer;
import pl.uncertainflowshopsolver.algo.util.SimulatedAnnealingConfigurationUtil;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;
import pl.uncertainflowshopsolver.gui.GUIController;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.testdata.InstanceGenerator;

import java.util.*;

import static pl.uncertainflowshopsolver.algo.util.SimulatedAnnealingConfigurationUtil.*;
import static pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty.swapRandomlyTwoTasks;
import static pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher.*;

/**
 * @author Piotr Kubicki, created on 24.04.2016.
 */
public class SimulatedAnnealing {

    private static final Random random = new Random();

    private AlgorithmEventDispatcher eventDispatcher;
    private ConfigurationProvider configurationProvider;

    private SAConfiguration configuration;
    private volatile boolean running;   //volatile to avoid "visibility" problem, when the updates of one thread are not visible to other threads.

    private FlowShopWithUncertainty uncertainFlowShop;

    private double mInitialTemperature; //for notice purpose only!
    private int iterationWithoutImprovementForDiversificationPurposeCount;

    public SimulatedAnnealing(FlowShopWithUncertainty uncertainFlowShop)
    {
        this.uncertainFlowShop = uncertainFlowShop;
    }

    public SimulatedAnnealing(GUIController guiController) {
        this.configurationProvider = guiController;
        this.eventDispatcher = new AlgorithmEventDispatcher(guiController);
    }

    public SimulatedAnnealing()
    {
    }

    public void start() {
        prepareConfiguration();
        running = true;
        eventDispatcher.dispatchAlgorithmStarted();
        solve2();
    }

    public void stop() {
        running = false;
    }

    private void prepareConfiguration() {
        configuration = configurationProvider.getSAConfiguration();
        //TODO nie będzie inicjalizera, ale będzie sposób generowania sąsiedztwa
//        try {
//            initializer = configuration.getWayToGenerateNeighborhoodEnum().newInstance();
//        } catch (InstantiationException | IllegalAccessException e) {
//            throw new RuntimeException("Can't create solution initializer", e);
//        }
    }

    private void solve2()
    {
        long startTime = System.currentTimeMillis();
        uncertainFlowShop = configuration.getUncertainFlowShop();

        /***********Hybridisation**************/
        MIH mih = new MIH(uncertainFlowShop);
        final Object[] mihResult = mih.solve(false, false);
        System.out.println("MIH UB result: " + (double)mihResult[1]);
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
        /*************************/

        /***********Global minimum**************/

        double globalMinimum = (double) result[1];
        double globalMinimumForLowerBound = (double) result[0];
        FlowShopWithUncertainty uncertainFlowShop_for_minimum = uncertainFlowShop.clone();
        uncertainFlowShop_for_minimum.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);
        uncertainFlowShop_for_minimum.setLowerBoundOfMinMaxRegretOptimalization(globalMinimumForLowerBound);
        /*************************/

        double valueBefore = globalMinimum;
        FlowShopWithUncertainty uncertainFlowShop_for_valueBefore = uncertainFlowShop.clone();
        uncertainFlowShop_for_valueBefore.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);     //TODO workaround. Może lepiej w clone dać kopiowanie tych fieldów
        uncertainFlowShop_for_valueBefore.setLowerBoundOfMinMaxRegretOptimalization(globalMinimumForLowerBound);

        int iterations = 0;
        int lastImprovementIteration = 0;
        int lastImprovementIterationInTermOfDiversification = 0;

        /*************************/
        FlowShopWithUncertainty tempFlowShopForBenAmeurAlgoPurpose = uncertainFlowShop.clone(); //przepięcie
        ArrayList<FlowShopWithUncertainty> tempGenerationStates = new ArrayList<>();
        while (tempGenerationStates.size() < configuration.getSamplesCardinality()) {
            FlowShopWithUncertainty curState = tempFlowShopForBenAmeurAlgoPurpose.getNeighbour(1.0);    //przepięcie
            final Object[] results = SubAlgorithm2.solveGreedy(curState, false, false);
            curState.setUpperBoundOfMinMaxRegretOptimalization((double)results[0]);
            tempGenerationStates.add(curState);
            tempFlowShopForBenAmeurAlgoPurpose = curState.clone();  //przepięcie
        }
        double initialTemperature =
                SimulatedAnnealingConfigurationUtil.calculateFromDesiredProbability(
                        configuration.getDesiredInitialAcceptanceProbability(),
                        tempGenerationStates,
                        configuration.getErrorThreshold());
        System.out.printf(
                "Using init temp %.2f (target init acceptance prob %.3f)\n",
                initialTemperature, configuration.getDesiredInitialAcceptanceProbability());
        /*************************/
        mInitialTemperature = initialTemperature;

        long midTime_2 = System.currentTimeMillis();
        double elapsedTime_delta1 = (midTime_2 - startTime);
        long midTime_3 = 0L;
        double elapsedTime_delta3 = 0d;

        while (running){

            midTime_2 = System.currentTimeMillis(); //it is not bug! this line is important!

            if(midTime_3 != 0L)
            {
                double elapsedTime_delta2 = midTime_2 - midTime_3;
                elapsedTime_delta3 += elapsedTime_delta2;
            }

//            if (/*lastImprovementIteration*/iterations % 10 == 0) TODO coś tu nie gra
            eventDispatcher.dispatchIterationUpdated(iterations, uncertainFlowShop_for_minimum);

            midTime_3= System.currentTimeMillis();
            iterations++;
            iterationWithoutImprovementForDiversificationPurposeCount = iterations - lastImprovementIterationInTermOfDiversification;

            if(isDiversificationNeeded()) {
                setBestUncertainFlowShopAfterDiversification();
                lastImprovementIterationInTermOfDiversification = iterations;
                System.out.println("\nThe temp was: " + initialTemperature);
                initialTemperature = mInitialTemperature;
                System.out.println("\nThe temp is now: " + initialTemperature);
            }

            for (int i = 0; i < configuration.getEpocheLength(); i++) {

                final FlowShopWithUncertainty neighbour = uncertainFlowShop.getNeighbour(1.0);
                final Object[] resultInside = SubAlgorithm2.solveGreedy(neighbour, null, false);
                double currentValue = (double) resultInside[1];  //upper bound

                if (valueBefore >= currentValue) {
                    valueBefore = currentValue;
                    uncertainFlowShop_for_valueBefore = neighbour.clone();
                    if (globalMinimum > currentValue) {
                        globalMinimum = currentValue;
                        uncertainFlowShop_for_minimum = neighbour.clone();
                        uncertainFlowShop_for_minimum.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);
                        uncertainFlowShop_for_minimum.setLowerBoundOfMinMaxRegretOptimalization((double) resultInside[0]);
                        lastImprovementIteration = iterations;
                        lastImprovementIterationInTermOfDiversification = iterations;
                    }
                } else {
                    double delta = currentValue - valueBefore;
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
            }
            initialTemperature = configuration.getDecayRate() * initialTemperature;

            // stop conditions
            if (configuration.getMaxNumberOfIterations() != 0 && iterations > configuration.getMaxNumberOfIterations()) {
                System.out.println("End temperature: " + initialTemperature);
                break;
            }
            if (configuration.getMaxIterationsWithoutImprovementAsStopCriterion() != 0 &&
                    iterations - lastImprovementIteration > configuration.getMaxIterationsWithoutImprovementAsStopCriterion()) {
                break;
            }
        }
        long stopTime = System.currentTimeMillis();
        double elapsedLastPeriodOfTime = (stopTime - midTime_3) / 1000d; //in seconds
        double elapsedTime_delta4 = elapsedTime_delta3 + elapsedTime_delta1;
        double elapsedTime = (elapsedLastPeriodOfTime + elapsedTime_delta4) / 1000d; //in seconds
        uncertainFlowShop_for_minimum.setElapsedTime(elapsedTime);

        // Last update
        eventDispatcher.dispatchIterationUpdated(iterations, uncertainFlowShop_for_minimum);

        if (iterations >= configuration.getMaxNumberOfIterations()) {
            eventDispatcher.dispatchAlgorithmEnded(EndingReason.ALL_ITERATIONS, elapsedTime, uncertainFlowShop_for_minimum, mInitialTemperature);
        } else if (running) {
            eventDispatcher.dispatchAlgorithmEnded(EndingReason.WITHOUT_PROGRESS, elapsedTime, uncertainFlowShop_for_minimum, mInitialTemperature);
        } else {
            eventDispatcher.dispatchAlgorithmEnded(EndingReason.CANCELLED, elapsedTime, uncertainFlowShop_for_minimum, mInitialTemperature);
        }

    }

    private boolean isDiversificationNeeded() {
        if(iterationWithoutImprovementForDiversificationPurposeCount >= configuration.getMaxIterationsWithoutImprovementForDiversificationPurpose())
            return true;
        else
            return false;
    }

    private void setBestUncertainFlowShopAfterDiversification() {
        System.out.println("\nShuffle! \nWas :");
        for (TaskWithUncertainty task : uncertainFlowShop.getTasks())
        {
            System.out.print(" " + task.getOriginalPosition() + " ");
        }
        System.out.println();

        FlowShopWithUncertainty minimumHelperFlowShop = uncertainFlowShop.clone();
        minimumHelperFlowShop.setUpperBoundOfMinMaxRegretOptimalization(-1d);
        for (int i = 0; i < minimumHelperFlowShop.getTaskCount(); i++) {
            FlowShopWithUncertainty newFlowShop = minimumHelperFlowShop.clone();
            Collections.shuffle(newFlowShop.getTasks());
            double resultForNewValue = (double) SubAlgorithm2.solveGreedy(uncertainFlowShop,false,false)[0];
            if(minimumHelperFlowShop.getUpperBoundOfMinMaxRegretOptimalization() == -1 || resultForNewValue < minimumHelperFlowShop.getUpperBoundOfMinMaxRegretOptimalization())
            {
                minimumHelperFlowShop=newFlowShop.clone();
                minimumHelperFlowShop.setUpperBoundOfMinMaxRegretOptimalization(resultForNewValue);
                System.out.println("New UpperBoundOfMinMaxRegretOptimalization of minimumHelperFlowShop: " + minimumHelperFlowShop.getUpperBoundOfMinMaxRegretOptimalization());
            }
        }
        uncertainFlowShop = minimumHelperFlowShop.clone();
        uncertainFlowShop.setUpperBoundOfMinMaxRegretOptimalization(minimumHelperFlowShop.getUpperBoundOfMinMaxRegretOptimalization());

        System.out.println("\nIs :");
        for (TaskWithUncertainty task : uncertainFlowShop.getTasks())
        {
            System.out.print(" " + task.getOriginalPosition() + " ");
        }
    }

    public static void main(String[] args) {
//        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(new FlowShopWithUncertainty());
//        Object[] result = simulatedAnnealing.solveSA(true, true);
//        System.out.println("SA solution: ");
//        System.out.println(resu[0].toString());
//        System.out.println("SA result: " + result[1]);

//        InstanceGenerator instanceGenerator = new InstanceGenerator(3, 6);
//        final FlowShopWithUncertainty uncertainFlowShopInstance = instanceGenerator.generateUncertainFlowShopInstance(0, 100, 50);
////        uncertainFlowShopInstance.toFile("qpa1.txt");
//        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(uncertainFlowShopInstance);
//        Object[] result = simulatedAnnealing.solveSA(true, false);
//        System.out.println("SA solution: ");
//        System.out.println(result[0].toString());
//        System.out.println("SA result: " + result[1]);
//
//
//        SimulatedAnnealing simulatedAnnealing2 = new SimulatedAnnealing(uncertainFlowShopInstance);
//        Object[] result2 = simulatedAnnealing2.solveSA(true, false);
//        System.out.println("SA solution: ");
//        System.out.println(result2[0].toString());
//        System.out.println("SA result: " + result2[1]);
//
//        System.out.println(Arrays.equals((double[])result[3], (double[]) result2[3]));
    }

    public void setEventDispatcher(AlgorithmEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }
}

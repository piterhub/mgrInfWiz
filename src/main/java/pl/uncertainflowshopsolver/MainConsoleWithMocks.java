package pl.uncertainflowshopsolver;

import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.config.TSPConfiguration;
import pl.uncertainflowshopsolver.config.impl.TSPConfigurationImpl;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventListener;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.config.impl.SAConfigurationImpl;
import pl.uncertainflowshopsolver.testdata.UncertainFlowShopParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Piotr Kubicki, created on 22.05.2016.
 */
public class MainConsoleWithMocks {


    public static final String PATH_TO_RESOURCES = "C:/Users/pkubicki/IdeaProjects/mgrInfWiz/resources";
    public static String PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP = "resources/1_[n50, m3, K100, C50].txt";

    /**
     * Use this to run without GUI
     * Change mocks to set configuration
     */
    public static void main(String[] args) throws FileNotFoundException {

        /**
         * Strojenie alpha:
         */
        int HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE = 5;
        int HOW_MANY_INSTANCES = 5;
        int HOW_MANY_ALFAS= 5;
        double[] CHECKED_ALFAS = {0.975, 0.980, 0.985, 0.990, 0.995};

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd-HH.mm-ss");
        final String timestamp = simpleDateFormat.format(new Date());
        PrintWriter pw = new PrintWriter(new File(PATH_TO_RESOURCES + "/STROJENIE_" + timestamp +".csv"));
        StringBuilder sb = new StringBuilder();

        final ConfigurationProviderMock configurationProviderMock = new ConfigurationProviderMock();
        final EventDispatcherMock eventDispatcher = new EventDispatcherMock(null);

        for (int instance = 1; instance <= HOW_MANY_INSTANCES; instance++) {

            FlowShopWithUncertainty uncertainFlowShopInstance;
            try {
                uncertainFlowShopInstance = UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

//            System.out.println(uncertainFlowShopInstance.toString() + "\n");

            for (int repetition = 0; repetition < HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE; repetition++) {

                SimulatedAnnealing SAAlgorithm = new SimulatedAnnealing();
                SAAlgorithm.setConfigurationProvider(configurationProviderMock);
                SAAlgorithm.setEventDispatcher(eventDispatcher);
                SAAlgorithm.start();

                while(!eventDispatcher.algorithmIsEnded){}


//                configurationProviderMock.handleChangeOfAlpha();
//
//
//                MIH mih = new MIH(uncertainFlowShopInstance);
//                measureMIHLowerBound(mih, sb);
////            measureMIHUpperBound(mih, sb);
//
//                SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(uncertainFlowShopInstance);
////            measureSALowerBound(simulatedAnnealing, sb);
//                measureSAUpperBound(simulatedAnnealing, sb);
            }
            PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP = changePathToFileForNextRun(instance);
            System.out.println(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP);
        }



        pw.write(sb.toString());
        pw.close();
        System.out.println("done!");



        int MINIMUM_INSTANCE = 26;
        int MAXIMUM_INSTANCE = 50;
        int DELTA_OF_TASK_COUNT_BETWEEN_EACH_TEST_INSTANCE = 2;
//

//        for (int i = 0; i < HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE; i++) {
//
//        }

//        try {
//            MIH mih = new MIH(UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP));
//            mih.solve(true, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//
//        TabuSearch TSAlgorithm = new TabuSearch();
//        TSAlgorithm.setConfigurationProvider(new ConfigurationProviderMock());
//        TSAlgorithm.setEventDispatcher(new EventDispatcherMock(null));
//        TSAlgorithm.start();
    }

    private static String changePathToFileForNextRun(int index) {
        return PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP.substring(0, 10) + String.valueOf(index+1) + PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP.substring(11);
    }


    private static class ConfigurationProviderMock implements ConfigurationProvider {

        private double desiredInitialAcceptanceProbability = 0.95;
        private double decayRate = 0.9925;//0.9995;
        private int maxNumberOfIterations = 10000;
        private int maxIterationsWithoutImprovementForDiversificationPurpose = 1001;
        private int maxIterationsWithoutImprovementAsStopCriterion = 5000;
        private int epocheLength = 5;
        private int samplesCardinality = 5000;  //10000

        //exp(-80/(1163*(0.9995)^10000)) = 0.00003637018534505723
        //exp(-80/(1163*(0.99995)^80000)) = 0.023375839279601084

        public void handleChangeOfAlpha(double newValue)
        {
            this.decayRate = newValue;
            System.out.println("\nConfigurationProviderMock#decayRate: " + this.decayRate + "\n");
        }

        public void handleChangeOfDesiredInitialAcceptanceProbability(double newValue)
        {
            this.desiredInitialAcceptanceProbability = newValue;
        }

        @Override
        public SAConfiguration getSAConfiguration() {
            try {
                return SAConfigurationImpl.newBuilder()
                        .withDesiredInitialAcceptanceProbability(desiredInitialAcceptanceProbability)
                        .withEpocheLength(epocheLength)
                        .withDecayRate(decayRate)
                        .withEndTemperature(0.5)
                        .withErrorThreshold(0.0001)
                        .withSamplesCardinality(samplesCardinality)
                        .withMaxNumberOfIterations(maxNumberOfIterations)
                        .withUncertainFlowshop(UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP))
                        .withMaxIterationsWithoutImprovementForDiversificationPurpose(maxIterationsWithoutImprovementForDiversificationPurpose)
                        .withMaxIterationsWithoutImprovementAsStopCriterion(maxIterationsWithoutImprovementAsStopCriterion)
                        .build();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public TSPConfiguration getTSPConfiguration() {
//            final WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum = initializerNameClassMap.get(initializerChoiceBox.getValue());
            try {
                return TSPConfigurationImpl.newBuilder()
                        .withSizeOfNeighborhood(1225)
                        .withLengthOfTabuList(50)
                        .withIterationsWithoutImprovementAsAdditionalAspirationCriterion(10)
                        .withMaxIterationsWithoutImprovementForDiversificationPurpose(250)
                        .withMaxIterationsAsStopCriterion(15000)
                        .withMaxIterationsWithoutImprovementAsStopCriterion(1000)
    //                    .withWayToGenerateNeighborhood(wayToGenerateNeighborhoodEnum)
                        .withUncertainFlowshop(UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP))
                        .build();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class EventDispatcherMock extends AlgorithmEventDispatcher {

        private int bestSolutionIteration = -1;
        private FlowShopWithUncertainty bestSolution;
        public boolean algorithmIsEnded = false;

        private boolean solutionIsBest(FlowShopWithUncertainty flowShop) {
            return bestSolutionIteration == -1 || bestSolution.getUpperBoundOfMinMaxRegretOptimalization() > flowShop.getUpperBoundOfMinMaxRegretOptimalization();
        }

        public EventDispatcherMock(AlgorithmEventListener eventListener) {
            super(eventListener);
        }

        @Override
        public void dispatchIterationUpdated(int iteration, FlowShopWithUncertainty flowShop) {

            if (solutionIsBest(flowShop)) {
                bestSolutionIteration = iteration;
                bestSolution = flowShop;

//                System.out.println("\n\nBest solution updated: ");
                System.out.println("Iteration: " + iteration +
                        " \nLowerBound Of MinMaxRegret Optimalization: " + flowShop.getLowerBoundOfMinMaxRegretOptimalization() +
                        " \nUpperBound Of MinMaxRegret Optimalization: " + flowShop.getUpperBoundOfMinMaxRegretOptimalization());

            }
        }

        @Override
        public void dispatchAlgorithmEnded(EndingReason reason, double elapsedTime, FlowShopWithUncertainty flowShopWithUncertainty, double initialTemperature) {
//            Date date = new Date();
//            System.out.println("Algorithm ended." /*Elapsed time " + elapsedTime + ". Now is " + date.toString()*/);

            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
            otherSymbols.setDecimalSeparator(',');
            NumberFormat formatter = new DecimalFormat("#0.00");
            formatter.setGroupingUsed(false);
            System.out.println("The initial temperature was: " + formatter.format(initialTemperature) + "\nElapsed time: " + formatter.format(elapsedTime) + " seconds");
            System.out.println("Result for LB:" + flowShopWithUncertainty.getLowerBoundOfMinMaxRegretOptimalization() + "");
            System.out.println("Result for UB:" + flowShopWithUncertainty.getUpperBoundOfMinMaxRegretOptimalization() + "");
            flowShopWithUncertainty.toString();
            algorithmIsEnded = true;
        }

        @Override
        public void dispatchAlgorithmEnded(final EndingReason reason, final double elapsedTime, final FlowShopWithUncertainty flowShopWithUncertainty) {
            System.out.println("Result for LB:" + flowShopWithUncertainty.getLowerBoundOfMinMaxRegretOptimalization() + "");
            System.out.println("Result for UB:" + flowShopWithUncertainty.getUpperBoundOfMinMaxRegretOptimalization() + "");
            System.out.println("Elapsed time[s]:" + elapsedTime);
            flowShopWithUncertainty.toString();
        }

        @Override
        public void dispatchAlgorithmStarted() {
            Date date = new Date();
            System.out.println("Algorithm started. Now is " + date.toString());
            algorithmIsEnded = false;
        }

    }

}

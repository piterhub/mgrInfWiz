package pl.uncertainflowshopsolver;

import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.config.TSPConfiguration;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventListener;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.config.impl.SAConfigurationImpl;
import pl.uncertainflowshopsolver.testdata.UncertainFlowShopParser;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Piotr Kubicki, created on 22.05.2016.
 */
public class MainConsoleWithMocks {

//    private static final String PATH = "resources/11_uncertainFlowShop_05.10-14.38-13.858.txt";
//    private static final String PATH = "resources/2_[n50, m3, K100, C50].txt";
//    private static final String PATH = "resources/3_[n50, m3, K100, C50].txt";
    private static final String PATH = "resources/5_[n50, m3, K100, C50].txt";

    /**
     * Use this to run without GUI
     * Change mocks to set configuration
     */
    public static void main(String[] args) {

//        int HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE = 10;
//        int DELTA_OF_TASK_COUNT_BETWEEN_EACH_TEST_INSTANCE = 5;
//        int MINIMUM_INSTANCE = 10;
//        int MAXIMUM_INSTANCE = 100;
//
//        for (int i = 0; i < HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE; i++) {
//
//        }
        SimulatedAnnealing SAAlgorithm = new SimulatedAnnealing();
        SAAlgorithm.setConfigurationProvider(new ConfigurationProviderMock());
        SAAlgorithm.setEventDispatcher(new EventDispatcherMock(null));
        SAAlgorithm.start();
    }


    private static class ConfigurationProviderMock implements ConfigurationProvider {

        private double desiredInitialAcceptanceProbability = 0.95;
        private double decayRate = 0.9995;
        private int epocheLength = 5;
        private int maxNumberOfIterations = 10000;
        private int samplesCardinality = 5000;  //10000

        //exp(-80/(1163*(0.9995)^10000)) = 0.00003637018534505723
        //exp(-80/(1163*(0.99995)^80000)) = 0.023375839279601084

        public void handleChangeOfDesiredInitialAcceptanceProbability(double newValue)
        {
            this.desiredInitialAcceptanceProbability = newValue;
        }

        @Override
        public SAConfiguration getSAConfiguration() {
            try {
                samplesCardinality = 10000;
                return SAConfigurationImpl.newBuilder()
                        .withDesiredInitialAcceptanceProbability(desiredInitialAcceptanceProbability)
                        .withEpocheLength(epocheLength)
                        .withDecayRate(decayRate)
                        .withEndTemperature(0.5)
                        .withErrorThreshold(0.0001)
                        .withSamplesCardinality(samplesCardinality)
                        .withMaxNumberOfIterations(maxNumberOfIterations)
    //                .withCutOffEnergyLevel(cutOffEnergyLevelDoubleTextBox.getValue())
    //                    .withSolutionInitializerClass(solutionInitializerClass)
                        .withUncertainFlowshop(UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH))
                        .build();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public TSPConfiguration getTSPConfiguration() {
            return null;
        }
    }

    private static class EventDispatcherMock extends AlgorithmEventDispatcher {

        private int bestSolutionIteration = -1;
        private FlowShopWithUncertainty bestSolution;

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
        }

        @Override
        public void dispatchAlgorithmStarted() {
            Date date = new Date();
            System.out.println("Algorithm started. Now is " + date.toString());
        }

    }

}

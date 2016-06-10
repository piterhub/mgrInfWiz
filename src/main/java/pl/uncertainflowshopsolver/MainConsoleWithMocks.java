package pl.uncertainflowshopsolver;

import pl.uncertainflowshopsolver.algo.MIH;
import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.algo.TabuSearch;
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
    public static final String PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP1 = "resources/1_[n50, m3, K100, C50].txt";
    public static double SA_RESULT;

    /**
     * Use this to run without GUI
     * Change mocks to set configuration
     */
    public static void main(String[] args) throws FileNotFoundException {

//        tuneAlphaAndDesiredInitialAcceptanceProbability();

        FlowShopWithUncertainty flowShopWithUncertainty;

        try {
            flowShopWithUncertainty = UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ConfigurationProviderMock configurationProviderMock = new ConfigurationProviderMock();
        configurationProviderMock.handleChangeOfUncertainFlowShop(flowShopWithUncertainty);

        TabuSearch TSAlgorithm = new TabuSearch();
        TSAlgorithm.setConfigurationProvider(configurationProviderMock);
        TSAlgorithm.setEventDispatcher(new EventDispatcherMock(null));
        TSAlgorithm.start();
    }

    /**
     * Strojenie alfa i P_0.
     * Notice: 5 instances x 5 repetitions (runs) x 5 values of parameter x 2 parameters it takes about 10h.
     * Results are written to .csv file, which is a base to choose the proper combination of alpha and P_0.
     *
     * @throws FileNotFoundException
     */
    private static void tuneAlphaAndDesiredInitialAcceptanceProbability() throws FileNotFoundException {
        /**
         * Strojenie alpha:
         */
        int HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE = 5;//5;
        int HOW_MANY_INSTANCES = 5;//5;
        double[] CHECKED_ALFAS = {0.975, 0.980, 0.985, 0.990, 0.995};

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM-HH.mm-ss");
        String timestamp = simpleDateFormat.format(new Date());
        PrintWriter pw = new PrintWriter(new File(PATH_TO_RESOURCES + "/STROJENIE_alfa_" + timestamp +".csv"));
        StringBuilder sb = new StringBuilder();

        final ConfigurationProviderMock configurationProviderMock = new ConfigurationProviderMock();
        final EventDispatcherMock eventDispatcher = new EventDispatcherMock(null);

        for (int instance = 1; instance <= HOW_MANY_INSTANCES; instance++) {

            sb.append(instance);
            sb.append(';');

            FlowShopWithUncertainty uncertainFlowShopInstance;
            try {
                uncertainFlowShopInstance = UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            configurationProviderMock.handleChangeOfUncertainFlowShop(uncertainFlowShopInstance);

            for (int repetition = 0; repetition < HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE; repetition++) {

                sb.append(repetition+1);
                sb.append(';');

                for (int alfa = 0; alfa < CHECKED_ALFAS.length; alfa++) {

                    configurationProviderMock.handleChangeOfAlpha(CHECKED_ALFAS[alfa]);

                    SimulatedAnnealing SAAlgorithm = new SimulatedAnnealing();
                    SAAlgorithm.setConfigurationProvider(configurationProviderMock);
                    SAAlgorithm.setEventDispatcher(eventDispatcher);
                    SAAlgorithm.start();

                    while(!eventDispatcher.algorithmIsEnded){}

                    sb.append(SA_RESULT);
                    sb.append(';');
                }

                sb.append('\n');
                if(repetition < HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE-1)
                {
                    sb.append(instance);
                    sb.append(';');
                }
            }
            PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP = changePathToFileForNextRun(instance);
            System.out.println(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP);

            try {
                Thread.sleep(240000);//pause 4 min for cool down the processor
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        pw.write(sb.toString());
        pw.close();
        System.out.println("done 1!");

        /**
         * Strojenie P_0:
         */
        double[] CHECKED_P_0 = {0.75, 0.80, 0.85, 0.90, 0.95};

        timestamp = simpleDateFormat.format(new Date());
        pw = new PrintWriter(new File(PATH_TO_RESOURCES + "/STROJENIE_P0_" + timestamp +".csv"));
        sb = new StringBuilder();
        PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP = PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP1;

        final ConfigurationProviderMock configurationProviderMock2 = new ConfigurationProviderMock();
        final EventDispatcherMock eventDispatcher2 = new EventDispatcherMock(null);

        for (int instance = 1; instance <= HOW_MANY_INSTANCES; instance++) {

            sb.append(instance);
            sb.append(';');

            FlowShopWithUncertainty uncertainFlowShopInstance;
            try {
                uncertainFlowShopInstance = UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            configurationProviderMock.handleChangeOfUncertainFlowShop(uncertainFlowShopInstance);

            for (int repetition = 0; repetition < HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE; repetition++) {

                sb.append(repetition+1);
                sb.append(';');

                for (int pZero = 0; pZero < CHECKED_P_0.length; pZero++) {

                    configurationProviderMock.handleChangeOfDesiredInitialAcceptanceProbability(CHECKED_P_0[pZero]);

                    SimulatedAnnealing SAAlgorithm = new SimulatedAnnealing();
                    SAAlgorithm.setConfigurationProvider(configurationProviderMock2);
                    SAAlgorithm.setEventDispatcher(eventDispatcher2);
                    SAAlgorithm.start();

                    while(!eventDispatcher.algorithmIsEnded){}

                    sb.append(SA_RESULT);
                    sb.append(';');
                }

                sb.append('\n');
                if(repetition < HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE-1)
                {
                    sb.append(instance);
                    sb.append(';');
                }
            }
            PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP = changePathToFileForNextRun(instance);
            System.out.println(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP);

            try {
                Thread.sleep(240000);//pause 4 min for cool down the processor
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        pw.write(sb.toString());
        pw.close();
        System.out.println("done 2!");
    }

    private static String changePathToFileForNextRun(int index) {
        return PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP.substring(0, 10) + String.valueOf(index+1) + PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP.substring(11);
    }


    private static class ConfigurationProviderMock implements ConfigurationProvider {

        private double desiredInitialAcceptanceProbability = 0.95;
        private double decayRate = 0.995;
        private int maxNumberOfIterations = 10000;
        private int maxIterationsWithoutImprovementForDiversificationPurpose = 1001;
        private int maxIterationsWithoutImprovementAsStopCriterion = 5000;
        private int epocheLength = 5;
        private int samplesCardinality = 5000;  //10000
        private FlowShopWithUncertainty uncertainFlowShop;

        //exp(-80/(1163*(0.9995)^10000)) = 0.00003637018534505723
        //exp(-80/(1163*(0.99995)^80000)) = 0.023375839279601084

        public void handleChangeOfAlpha(double newValue)
        {
            this.decayRate = newValue;
        }

        public void handleChangeOfUncertainFlowShop(FlowShopWithUncertainty uncertainFlowShop)
        {
            this.uncertainFlowShop = uncertainFlowShop;
        }

        public void handleChangeOfDesiredInitialAcceptanceProbability(double newValue)
        {
            this.desiredInitialAcceptanceProbability = newValue;
        }

        @Override
        public SAConfiguration getSAConfiguration() {
            return SAConfigurationImpl.newBuilder()
                    .withDesiredInitialAcceptanceProbability(desiredInitialAcceptanceProbability)
                    .withEpocheLength(epocheLength)
                    .withDecayRate(decayRate)
                    .withEndTemperature(0.5)
                    .withErrorThreshold(0.0001)
                    .withSamplesCardinality(samplesCardinality)
                    .withMaxNumberOfIterations(maxNumberOfIterations)
                    .withUncertainFlowshop(uncertainFlowShop)
                    .withMaxIterationsWithoutImprovementForDiversificationPurpose(maxIterationsWithoutImprovementForDiversificationPurpose)
                    .withMaxIterationsWithoutImprovementAsStopCriterion(maxIterationsWithoutImprovementAsStopCriterion)
                    .build();
        }

        @Override
        public TSPConfiguration getTSPConfiguration() {
//            final WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum = initializerNameClassMap.get(initializerChoiceBox.getValue());
            return TSPConfigurationImpl.newBuilder()
                    .withSizeOfNeighborhood(1225)
                    .withLengthOfTabuList(50)
                    .withIterationsWithoutImprovementAsAdditionalAspirationCriterion(10)
                    .withMaxIterationsWithoutImprovementForDiversificationPurpose(250)
                    .withMaxIterationsAsStopCriterion(maxNumberOfIterations)
                    .withMaxIterationsWithoutImprovementAsStopCriterion(1000)
//                    .withWayToGenerateNeighborhood(wayToGenerateNeighborhoodEnum)
                    .withUncertainFlowshop(uncertainFlowShop)
                    .build();
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
//                System.out.println("Iteration: " + iteration +
//                        " \nLowerBound Of MinMaxRegret Optimalization: " + flowShop.getLowerBoundOfMinMaxRegretOptimalization() +
//                        " \nUpperBound Of MinMaxRegret Optimalization: " + flowShop.getUpperBoundOfMinMaxRegretOptimalization());

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
            SA_RESULT = flowShopWithUncertainty.getUpperBoundOfMinMaxRegretOptimalization();
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

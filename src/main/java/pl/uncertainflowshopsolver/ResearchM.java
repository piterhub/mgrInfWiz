package pl.uncertainflowshopsolver;

import pl.uncertainflowshopsolver.algo.MIH;
import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.algo.TabuSearch;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.config.TSPConfiguration;
import pl.uncertainflowshopsolver.config.impl.SAConfigurationImpl;
import pl.uncertainflowshopsolver.config.impl.TSPConfigurationImpl;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventListener;
import pl.uncertainflowshopsolver.testdata.InstanceGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Piotr Kubicki, created on 09.06.2016.
 */
public class ResearchM {

    /**
     * ENVIRONMENT AND RESEARCH SPECIFICATION:
     */
    private static final String PATH_TO_RESOURCES = "C:/Users/pkubicki/IdeaProjects/mgrInfWiz/resources";
    private static final String PREFIX_PATH_TO_FILE = "/BADANIA_m_";
//    public static String PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP = "resources/1_[n50, m3, K100, C50].txt";
    private static final int HOW_MANY_INSTANCES = 10;
    private static final int HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE = 5;
    private static final int MINIMUM_INSTANCE = 3;
    private static final int MAXIMUM_INSTANCE = 15;
    private static final int DELTA_OF_MACHINE_COUNT_BETWEEN_EACH_TEST_INSTANCE = 10;
    private static final int N = 10; //task count
    private static final int C = 50;  //machines count
    private static final int K = 100;
    private static final int LOWER_BOUND_OF_LOWER_INTERVAL = 0;

    /**
     * RESULTS:
     */
    private static double SA_RESULT;
    private static double TS_RESULT;
    private static double ELAPSED_TIME;

    public static void main(String[] args) throws FileNotFoundException {

        NumberFormat formatter = new DecimalFormat("#0.00");
        formatter.setGroupingUsed(false);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM-HH.mm-ss");
        String timestamp = simpleDateFormat.format(new Date());
        PrintWriter pw = new PrintWriter(new File(PATH_TO_RESOURCES + PREFIX_PATH_TO_FILE + timestamp +".csv"));
//        StringBuilder sb = new StringBuilder();

        ConfigurationProviderMock configurationProviderMock = new ConfigurationProviderMock();
        EventDispatcherMock eventDispatcher = new EventDispatcherMock(null);

        for (int M = MINIMUM_INSTANCE; M <= MAXIMUM_INSTANCE; M+= DELTA_OF_MACHINE_COUNT_BETWEEN_EACH_TEST_INSTANCE) {

            pw.print(M);
            pw.print(";");

            double z_MIH_Average = 0;
            double z_SA_Average = 0;
            double z_TS_Average = 0;
            double t_MIH_Average = 0;
            double t_SA_Average = 0;
            double t_TS_Average = 0;

            for (int instance = 1; instance <= HOW_MANY_INSTANCES; instance++) {

                InstanceGenerator instanceGenerator = new InstanceGenerator(M, N);
                final FlowShopWithUncertainty uncertainFlowShopInstance = instanceGenerator.generateUncertainFlowShopInstance(LOWER_BOUND_OF_LOWER_INTERVAL, K, C);
                System.out.println("\nNew uncertain Flow Shop instance: \n" + uncertainFlowShopInstance.toString());
                configurationProviderMock.handleChangeOfUncertainFlowShop(uncertainFlowShopInstance);

                for (int repetition = 1; repetition <= HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE; repetition++) {

                    MIH mih = new MIH(uncertainFlowShopInstance);
                    final Object[] result = mih.solve(true, false);
                    System.out.println("MIH solution: ");
                    System.out.println("LB " + result[0].toString());
                    System.out.println("UB " + result[1].toString());
                    System.out.println("Time: " + result[2].toString());

                    z_MIH_Average += (double) result[1];
                    t_MIH_Average += (double) result[2];

                    SimulatedAnnealing SAAlgorithm = new SimulatedAnnealing();
                    SAAlgorithm.setConfigurationProvider(configurationProviderMock);
                    SAAlgorithm.setEventDispatcher(eventDispatcher);
                    SAAlgorithm.start();

                    while(!eventDispatcher.algorithmIsEnded){}

                    z_SA_Average += SA_RESULT;
                    t_SA_Average += ELAPSED_TIME;

                    TabuSearch TSAlgorithm = new TabuSearch();
                    TSAlgorithm.setConfigurationProvider(configurationProviderMock);
                    TSAlgorithm.setEventDispatcher(eventDispatcher);
                    TSAlgorithm.start();

                    z_TS_Average += TS_RESULT;
                    t_TS_Average += ELAPSED_TIME;
                }
            }
            z_MIH_Average = z_MIH_Average /(HOW_MANY_INSTANCES * HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE);
            t_MIH_Average = t_MIH_Average /(HOW_MANY_INSTANCES * HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE);

            z_SA_Average = z_SA_Average /(HOW_MANY_INSTANCES * HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE);
            t_SA_Average = t_SA_Average /(HOW_MANY_INSTANCES * HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE);

            z_TS_Average = z_TS_Average /(HOW_MANY_INSTANCES * HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE);
            t_TS_Average = t_TS_Average /(HOW_MANY_INSTANCES * HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE);

            pw.print(formatter.format(z_MIH_Average));
            pw.print(";");
            pw.print(formatter.format(z_SA_Average));
            pw.print(";");
            pw.print(formatter.format(z_TS_Average));
            pw.print(";");
            pw.print(formatter.format(t_MIH_Average));
            pw.print(";");
            pw.print(formatter.format(t_SA_Average));
            pw.print(";");
            pw.print(formatter.format(t_TS_Average));
            pw.print(";");
            pw.print("\n");

            System.out.println("*********************");
            System.out.println("***    Row " + M + "    *****");
            System.out.println("*********************");
        }

//        pw.write(sb.toString());
        pw.close();
        System.out.println("done!");

    }

//    private static String changePathToFileForNextRun(int index) {
//        return PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP.substring(0, 10) + String.valueOf(index+1) + PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP.substring(11);
//    }

    private static class ConfigurationProviderMock implements ConfigurationProvider {

        private double desiredInitialAcceptanceProbability = 0.95;
        private double decayRate = 0.975;
        private int maxNumberOfIterations = 10000;
        private int maxIterationsWithoutImprovementForDiversificationPurpose = 1001;
        private int maxIterationsWithoutImprovementAsStopCriterion = 5000;
        private int epocheLength = 5;
        private int samplesCardinality = 5000;  //10000
        private FlowShopWithUncertainty uncertainFlowShop = null;

        //exp(-80/(1163*(0.9995)^10000)) = 0.00003637018534505723
        //exp(-80/(1163*(0.99995)^80000)) = 0.023375839279601084

        public void handleChangeOfUncertainFlowShop(FlowShopWithUncertainty uncertainFlowShop)
        {
            this.uncertainFlowShop = uncertainFlowShop;
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
            }
        }

        @Override
        public void dispatchAlgorithmEnded(EndingReason reason, double elapsedTime, FlowShopWithUncertainty flowShopWithUncertainty, double initialTemperature) {
//            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
//            otherSymbols.setDecimalSeparator(',');
            NumberFormat formatter = new DecimalFormat("#0.00");
            formatter.setGroupingUsed(false);
            System.out.println("\nThe initial temperature was: " + formatter.format(initialTemperature) + "\nElapsed SA time: " + formatter.format(elapsedTime) + " seconds");
            System.out.println("Result SA for LB:" + flowShopWithUncertainty.getLowerBoundOfMinMaxRegretOptimalization() + "");
            System.out.println("Result SA for UB:" + flowShopWithUncertainty.getUpperBoundOfMinMaxRegretOptimalization() + "");
//            flowShopWithUncertainty.toString();
            SA_RESULT = flowShopWithUncertainty.getUpperBoundOfMinMaxRegretOptimalization();
            ELAPSED_TIME = elapsedTime;
            algorithmIsEnded = true;
            bestSolutionIteration = -1;
            bestSolution = null;
        }

        @Override
        public void dispatchAlgorithmEnded(final EndingReason reason, final double elapsedTime, final FlowShopWithUncertainty flowShopWithUncertainty) {
//            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
//            otherSymbols.setDecimalSeparator(',');
            NumberFormat formatter = new DecimalFormat("#0.00");
            formatter.setGroupingUsed(false);
            System.out.println("Result TS for LB:" + flowShopWithUncertainty.getLowerBoundOfMinMaxRegretOptimalization() + "");
            System.out.println("Result TS for UB:" + flowShopWithUncertainty.getUpperBoundOfMinMaxRegretOptimalization() + "");
            System.out.println("Elapsed TS time[s]:" + formatter.format(elapsedTime));
//            flowShopWithUncertainty.toString();
            TS_RESULT = flowShopWithUncertainty.getUpperBoundOfMinMaxRegretOptimalization();
            ELAPSED_TIME = elapsedTime;
            algorithmIsEnded = true;
            bestSolutionIteration = -1;
            bestSolution = null;
        }

        @Override
        public void dispatchAlgorithmStarted() {
            Date date = new Date();
            System.out.println("\nAlgorithm started. Now is " + date.toString());
            algorithmIsEnded = false;
        }

    }

}

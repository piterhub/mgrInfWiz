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
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventListener;
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
import java.util.List;
import java.util.Locale;

/**
 * @author Piotr Kubicki, created on 09.06.2016.
 */
public class ResearchN {


    public static final String PATH_TO_RESOURCES = "C:/Users/pkubicki/IdeaProjects/mgrInfWiz/resources";
    public static String PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP = "resources/6_[n50, m3, K100, C50].txt";
    public static double SA_RESULT;
    public static double TS_RESULT;
    public static double ELAPSED_TIME;
//    public static boolean nowSAIsWorking = true;

    public static void main(String[] args) throws FileNotFoundException {

        NumberFormat formatter = new DecimalFormat("#0.00");
        formatter.setGroupingUsed(false);

        int HOW_MANY_INSTANCES = 5;
        int HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE = 3;
        int MINIMUM_INSTANCE = 5;
        int MAXIMUM_INSTANCE = 50;
        int DELTA_OF_TASK_COUNT_BETWEEN_EACH_TEST_INSTANCE = 5;

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM-HH.mm-ss");
        String timestamp = simpleDateFormat.format(new Date());
        PrintWriter pw = new PrintWriter(new File(PATH_TO_RESOURCES + "/BADANIA_n_" + timestamp +".csv"));
//        StringBuilder sb = new StringBuilder();

        ConfigurationProviderMock configurationProviderMock = new ConfigurationProviderMock();
        EventDispatcherMock eventDispatcher = new EventDispatcherMock(null);

        for (int n = MINIMUM_INSTANCE; n <= MAXIMUM_INSTANCE; n+=DELTA_OF_TASK_COUNT_BETWEEN_EACH_TEST_INSTANCE) {

            pw.print(n);    //TODO which approach is better? With or w/o StringBuilder?
            pw.print(";");

            double z_MIH_Average = 0;
            double z_SA_Average = 0;
            double z_TS_Average = 0;
            double t_MIH_Average = 0;
            double t_SA_Average = 0;
            double t_TS_Average = 0;

            for (int instance = 1; instance <= HOW_MANY_INSTANCES; instance++) {

                FlowShopWithUncertainty uncertainFlowShopInstance;
                try {
                    uncertainFlowShopInstance = UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                final List<TaskWithUncertainty> tasks = uncertainFlowShopInstance.getTasks();
                tasks.subList(n, tasks.size()).clear();
                System.out.println("\nNew uncertain Flow Shop instance: \n" + uncertainFlowShopInstance.toString());
                configurationProviderMock.handleChangeOfUncertainFlowShop(uncertainFlowShopInstance);

                for (int repetition = 1; repetition <= HOW_MANY_REPETITIONS_FOR_CONCRETE_INSTANCE; repetition++) {

                    MIH mih = new MIH(uncertainFlowShopInstance);
                    final Object[] result = mih.solve(true, false);
                    System.out.println("MIH solution: ");
                    System.out.println("LB " + result[0].toString());
                    System.out.println("UB " + result[1].toString());
                    System.out.println("Time: " + result[2].toString());

//                    sb.append(result[1].toString());
//                    sb.append(';');
//                    sb.append(result[2].toString());
//                    sb.append(';');
                    z_MIH_Average += (double) result[1];
                    t_MIH_Average += (double) result[2];

                    SimulatedAnnealing SAAlgorithm = new SimulatedAnnealing();
                    SAAlgorithm.setConfigurationProvider(configurationProviderMock);
                    SAAlgorithm.setEventDispatcher(eventDispatcher);
                    SAAlgorithm.start();

                    while(!eventDispatcher.algorithmIsEnded){}

//                    sb.append(SA_RESULT);
//                    sb.append(';');
//                    sb.append(ELAPSED_TIME);
//                    sb.append(';');
                    z_SA_Average += SA_RESULT;
                    t_SA_Average += ELAPSED_TIME;

                    TabuSearch TSAlgorithm = new TabuSearch();
                    TSAlgorithm.setConfigurationProvider(configurationProviderMock);
                    TSAlgorithm.setEventDispatcher(eventDispatcher);
                    TSAlgorithm.start();

//                    sb.append(TS_RESULT);
//                    sb.append(';');
//                    sb.append(ELAPSED_TIME);
//                    sb.append(';');
                    z_TS_Average += TS_RESULT;
                    t_TS_Average += ELAPSED_TIME;
                }

                PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP = changePathToFileForNextRun(instance);
                System.out.println("\n"+PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP);

//                try {
//                    Thread.sleep(120000);//pause 2 min for cool down the processor
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
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
            System.out.println("***    Row " + n + "    *****");
            System.out.println("*********************");
        }

//        pw.write(sb.toString());
        pw.close();
        System.out.println("done!");

    }

    private static String changePathToFileForNextRun(int index) {
        return PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP.substring(0, 10) + String.valueOf(index+1) + PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP.substring(11);
    }

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
